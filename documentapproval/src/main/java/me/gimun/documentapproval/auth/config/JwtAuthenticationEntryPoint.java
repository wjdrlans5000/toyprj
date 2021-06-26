package me.gimun.documentapproval.auth.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/*
 * Response에 401이 떨어질만한 에러가 발생할 경우 AuthenticationEntryPoint로 떨어지기때문에 AuthenticationEntryPoint를 상속받아서
 * 401발생시의 응답을 설정
 * AuthenticationEntryPoint를 구현하여 인증에 실패한 사용자의 response에 HttpServletResponse.SC_UNAUTHORIZED를 담아주도록 구현한다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorized");
    }
}
