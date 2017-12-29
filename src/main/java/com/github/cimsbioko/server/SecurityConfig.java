package com.github.cimsbioko.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint entryPoint = new DigestAuthenticationEntryPoint();
        entryPoint.setRealmName("CIMS API");
        entryPoint.setKey("cims-dig-it");
        entryPoint.setNonceValiditySeconds(10);
        return entryPoint;
    }

    @Bean
    public DigestAuthenticationFilter digestAuthFilter(DigestAuthenticationEntryPoint entryPoint,
                                                       UserDetailsService userDetailsService) {
        DigestAuthenticationFilter filter = new DigestAuthenticationFilter();
        filter.setUserDetailsService(userDetailsService);
        filter.setAuthenticationEntryPoint(entryPoint);
        return filter;
    }

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        DigestAuthenticationFilter digestFilter;

        @Autowired
        DigestAuthenticationEntryPoint digestAuthEndpoint;

        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/api/**")
                    .headers().disable()
                    .csrf().disable()
                    .exceptionHandling()
                    .authenticationEntryPoint(digestAuthEndpoint)
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(STATELESS)
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .addFilterAfter(digestFilter, BasicAuthenticationFilter.class);
        }
    }

    @Configuration
    public static class FormWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        AuthenticationSuccessHandler authSuccessHandler;

        protected void configure(HttpSecurity http) throws Exception {
            http
                    .headers().disable()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/login.faces", "/resources/**", "logout.faces", "favicon.ico").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin()
                    .loginPage("/login.faces")
                    .loginProcessingUrl("/loginProcess")
                    .successHandler(authSuccessHandler)
                    .failureUrl("/login.faces?login_error=1")
                    .and()
                    .logout()
                    .logoutUrl("/logoutProcess");
        }
    }
}
