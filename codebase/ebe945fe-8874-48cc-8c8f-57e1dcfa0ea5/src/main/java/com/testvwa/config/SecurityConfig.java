package com.testvwa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // VULNERABILITY: Disabled CSRF protection
        http.csrf().disable()
            .authorizeRequests()
                .antMatchers("/", "/login", "/register", "/api/**", "/uploads/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            .and()
            .logout()
                .permitAll()
            .and()
            // VULNERABILITY: Allowing frame options (clickjacking)
            .headers().frameOptions().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // VULNERABILITY: Hardcoded credentials and weak passwords
        auth.inMemoryAuthentication()
            .withUser("admin").password("admin123").roles("ADMIN")
            .and()
            .withUser("user").password("password").roles("USER")
            .and()
            .withUser("test").password("test").roles("USER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // VULNERABILITY: No password encoding
        return NoOpPasswordEncoder.getInstance();
    }
}
