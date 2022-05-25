package com.prgrms.devcourse.configures;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/asserts/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/me").hasAnyRole("USER", "ADMIN")
            .antMatchers("/admin").access("hasRole('ADMIN') and isFullyAuthenticated()")
            .anyRequest().permitAll()
            .and()
            .formLogin()
            .defaultSuccessUrl("/")
            .permitAll();
        /**
         * 로그아웃 설정
         */
        http.logout()
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .clearAuthentication(true);
        /**
         * remember me 설정
         */
        http.rememberMe()
            .rememberMeParameter("remember-me")
            .tokenValiditySeconds(300);
//        /**
//         * HTTP 요청을 HTTPS 요청으로 리다이렉트
//         */
//        http.requiresChannel()
//            .antMatchers("/api/**").requiresSecure();
        http.anonymous()
            .principal("thisIsAnonymousUser")
            .authorities("ROLE_ANONYMOUS", "ROLE_UNKNOWN");
        /**
         * AccessDeniedException 예외에 대한 핸들러 설정
         */
        http.exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("user").password("{noop}user123").roles("USER")
            .and()
            .withUser("admin").password("{noop}admin123").roles("ADMIN")
            ;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, e) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            log.warn("{} is denied", principal, e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write("## ACCESS DENIED ##");
            response.getWriter().flush();
            response.getWriter().close();
        };

    }
}
