package com.github.cimsbioko.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.TokenRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.security.*;
import com.github.cimsbioko.server.service.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder, UserCache userCache) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserCache(userCache);
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public RoleMapper roleMapper() {
        return new RoleMapper();
    }

    @Bean
    public UserMapper userMapper() {
        return new UserMapper(roleMapper());
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        UserDetailsService service = new UserDetailsService();
        service.setUserMapper(userMapper());
        service.setUserRepository(userRepo);
        return service;
    }

    @Bean
    @Primary
    public PasswordEncoder delegatingPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Qualifier("fieldworkerPasswordEncoder")
    public PasswordEncoder fieldworkerPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @Bean
    RunAsUserAspect runAsUserAspect(UserDetailsService userDetailsService, UserCache userCache) {
        return new RunAsUserAspect(userDetailsService, userCache);
    }

    @Bean
    AuthSuccessHandler lastLoginHandler(UserRepository userRepo) {
        return new AuthSuccessHandler(userRepo, delegatingPasswordEncoder());
    }

    @Bean
    AuthenticationEntryPoint ajaxAwareSecurityEndpoint(ObjectMapper mapper) {
        return new AjaxAwareAuthenticationEntryPoint("/login", mapper);
    }

    @Bean
    TokenGenerator tokenGenerator() {
        return new SecureTokenGenerator(16);
    }

    @Bean
    public TokenHasher tokenHasher() {
        return new ShaTokenHasher();
    }

    @Bean
    TokenAuthenticationService tokenAuthService(DeviceRepository deviceRepo, UserRepository userRepo,
                                                TokenRepository tokenRepo, PermissionsService permService) {
        return new TokenAuthenticationServiceImpl(deviceRepo, userRepo, tokenRepo, tokenHasher(), permService);
    }

    @Bean
    TokenAuthenticationProvider tokenAuthProvider(TokenAuthenticationService tokenAuthService) {
        return new TokenAuthenticationProvider(tokenAuthService);
    }

    @Bean
    DeviceDetailsService deviceDetailsService(DeviceRepository deviceRepo, DeviceMapper deviceMapper) {
        return new DeviceDetailsService(deviceRepo, deviceMapper);
    }

    @Bean
    DeviceMapper deviceMapper() {
        return new DeviceMapper();
    }

    @Configuration
    @Order(1)
    public static class DeviceBasicAuthConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final TokenHasher tokenHasher;
        private final DeviceDetailsService deviceDetailsService;


        public DeviceBasicAuthConfigurationAdapter(TokenHasher tokenHasher, DeviceDetailsService deviceDetailsService) {
            this.tokenHasher = tokenHasher;
            this.deviceDetailsService = deviceDetailsService;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(deviceDetailsService).passwordEncoder(new SHAPasswordEncoder(tokenHasher));
        }

        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/device/**")
                    .headers().disable()
                    .csrf().disable()
                    .sessionManagement()
                    .sessionCreationPolicy(STATELESS)
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic()
                    .realmName("CIMS Device API");
        }
    }

    @Configuration
    @Order(2)
    public static class ApiTokenAuthConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final DaoAuthenticationProvider daoAuthProvider;
        private final TokenAuthenticationProvider tokenAuthProvider;

        public ApiTokenAuthConfigurationAdapter(DaoAuthenticationProvider daoAuthProvider,
                                                TokenAuthenticationProvider tokenAuthProvider) {
            this.daoAuthProvider = daoAuthProvider;
            this.tokenAuthProvider = tokenAuthProvider;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(daoAuthProvider)
                    .authenticationProvider(tokenAuthProvider)
                    .eraseCredentials(false);
        }

        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .headers().disable()
                    .csrf().disable()
                    .sessionManagement()
                    .sessionCreationPolicy(STATELESS)
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .addFilterBefore(new TokenAuthenticationFilter(), BasicAuthenticationFilter.class)
                    .httpBasic()
                    .realmName("CIMS API");
        }
    }

    @Configuration
    public static class FormWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        private final DaoAuthenticationProvider authProvider;
        private final AuthenticationSuccessHandler authSuccessHandler;
        private final AuthenticationEntryPoint authEntryPoint;

        public FormWebSecurityConfigurerAdapter(DaoAuthenticationProvider authProvider,
                                                AuthenticationSuccessHandler successHandler,
                                                AuthenticationEntryPoint entryPoint) {
            this.authProvider = authProvider;
            this.authSuccessHandler = successHandler;
            this.authEntryPoint = entryPoint;
        }

        protected void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(authProvider).eraseCredentials(false);
        }

        protected void configure(HttpSecurity http) throws Exception {
            http.regexMatcher("^((?!\\/api\\/).)*$")
                    .headers().disable()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico", "/error").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(authEntryPoint)
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .successHandler(authSuccessHandler)
                    .permitAll()
                    .and()
                    .logout()
                    .permitAll();
        }
    }
}
