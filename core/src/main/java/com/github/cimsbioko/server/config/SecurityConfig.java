package com.github.cimsbioko.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.security.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

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
    RunAsUserAspect runAsUserAspect(UserDetailsService userDetailsService) {
        return new RunAsUserAspect(userDetailsService);
    }

    @Bean
    AuthSuccessHandler lastLoginHandler(UserRepository userRepo) {
        return new AuthSuccessHandler(userRepo, delegatingPasswordEncoder());
    }

    @Bean
    AuthenticationEntryPoint ajaxAwareSecurityEndpoint(ObjectMapper mapper) {
        return new AjaxAwareAuthenticationEntryPoint("/login", mapper);
    }

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .headers().disable()
                    .csrf().disable()
                    .exceptionHandling()
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(STATELESS)
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic();
        }
    }

    @Configuration
    public static class FormWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        private final AuthenticationSuccessHandler authSuccessHandler;
        private final AuthenticationEntryPoint authEntryPoint;
        private final UserDetailsService detailsService;
        private final PasswordEncoder encoder;

        public FormWebSecurityConfigurerAdapter(AuthenticationSuccessHandler successHandler,
                                                AuthenticationEntryPoint entryPoint,
                                                UserDetailsService detailsService, PasswordEncoder encoder) {
            this.authSuccessHandler = successHandler;
            this.authEntryPoint = entryPoint;
            this.detailsService = detailsService;
            this.encoder = encoder;
        }

        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.eraseCredentials(false)
                    .userDetailsService(detailsService)
                    .passwordEncoder(encoder);
        }

        protected void configure(HttpSecurity http) throws Exception {
            http.headers().disable()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/css/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()
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
