package me.gimun.documentapproval.auth.config;


import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.accounts.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//AuthenticationProvider 인터페이스는 화면에서 입력한 로그인 정보와 DB에서 가져온 사용자의 정보를 비교해주는 인터페이스이다.
//토큰으로 인증하는것은 아님
//토큰 인증 확인은 JwtRequestFilter에서 요청시마다 filter에서 토큰확인
//현재 프로젝트에서 사용하지 않는 클래스..
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        if(passwordEncoder.matches(account.getPassword(), password)) {
            throw new BadCredentialsException("UnAuthorized");
        }

        return new UsernamePasswordAuthenticationToken(email, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}