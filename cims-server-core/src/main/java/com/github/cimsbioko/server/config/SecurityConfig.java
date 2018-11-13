package com.github.cimsbioko.server.config;

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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
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
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    RunAsUserAspect runAsUserAspect(UserDetailsService userDetailsService) {
        return new RunAsUserAspect(userDetailsService);
    }

    @Bean
    AuthSuccessHandler lastLoginHandler(UserRepository userRepo) {
        return new AuthSuccessHandler(userRepo, delegatingPasswordEncoder());
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
                    .antMatchers("/api/rest/mobiledb").hasAuthority("MOBILE_SYNC")
                    .antMatchers(POST, "/api/odk/formUpload").hasAuthority("ODK_FORM_UPLOAD")
                    .antMatchers(POST, "/api/odk/forms").hasAuthority("ODK_FORM_UPLOAD")
                    .antMatchers(GET, "/api/odk/forms").hasAuthority("ODK_FORM_LIST")
                    .antMatchers(GET, "/api/odk/formList").hasAuthority("ODK_FORM_LIST")
                    .antMatchers(GET, "/api/odk/forms/**").hasAuthority("ODK_FORM_DOWNLOAD")
                    .antMatchers(GET, "/api/odk/formList/**").hasAuthority("ODK_FORM_DOWNLOAD")
                    .antMatchers(POST, "/api/odk/submission").hasAuthority("ODK_SUBMISSION_UPLOAD")
                    .antMatchers(GET, "/api/odk/view/submissionList").hasAuthority("ODK_SUBMISSION_LIST")
                    .antMatchers(GET, "/api/odk/submissions/**").hasAuthority("ODK_SUBMISSION_LIST")
                    .antMatchers(GET, "/api/odk/view/downloadSubmission").hasAuthority("ODK_SUBMISSION_DOWNLOAD")
                    .antMatchers(GET, "/api/odk/submission/**").hasAuthority("ODK_SUBMISSION_DOWNLOAD")
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic();
        }
    }

    @Configuration
    public static class FormWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        private final AuthenticationSuccessHandler authSuccessHandler;
        private final UserDetailsService detailsService;
        private final PasswordEncoder encoder;

        public FormWebSecurityConfigurerAdapter(AuthenticationSuccessHandler successHandler, UserDetailsService detailsService, PasswordEncoder encoder) {
            this.authSuccessHandler = successHandler;
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
                    .antMatchers("/css/**", "/images/**", "/webjars/**", "favicon.ico").permitAll()
                    .anyRequest().authenticated()
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
