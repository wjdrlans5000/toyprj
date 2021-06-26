package me.gimun.documentapproval.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserDetailsService jwtUserDetailsService;


    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //AuthenticationManager : Authentication을 만들고 인증을 처리하는 인터페이스
        // AuthenticationManagerBuilder : 인증 객체를 만들 수 있도록 제공
        auth.jdbcAuthentication().dataSource(dataSource);
        auth
                .userDetailsService(jwtUserDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    //HttpSecurity 설정
    //Security Filter 로 들어온 이후에 Role 등을 이용하여 인증 적용 여부를 결정한다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests().antMatchers("/authenticate", "/api/account", "/docs/index.html").permitAll()
                .anyRequest().authenticated()
                .and()
                //- exceptionHandling을 위해서 실제 구현한 jwtAuthenticationEntryPoint을 넣어준다
                .exceptionHandling()
                //인증에 실패하여 401에러 발생시 jwtAuthenticationEntryPoint로 바인딩
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                //- Spring Security에서 Session을 생성하거나 사용하지 않도록 설정한다.
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //커스텀 필터를 UsernamePasswordAuthenticationFilter 보다 앞에서 실행되도록 설정
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
