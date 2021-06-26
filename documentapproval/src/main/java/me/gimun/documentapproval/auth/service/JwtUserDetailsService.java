package me.gimun.documentapproval.auth.service;

import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.accounts.AccountRepository;
import me.gimun.documentapproval.accounts.Role;
import me.gimun.documentapproval.auth.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    //UserDetailsService를 상속받아서 loadUserByUsername 오버라이딩
    //email로 계정 조회하여 권한 설정
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));
        if (email.equals("wjdrlans4000@naver.com")) {
            grantedAuthorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        }

        return new UserAccount(account, grantedAuthorities);
    }


    public Account authenticateByEmailAndPassword(String email, String password) {
        //[3].사용자 이메일로 db에서 계정정보를 조회한다.
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        //[3.1].사용자 이메일로 조회된 패스워드가 [2]에서 인증요청한 패스워드와 일치하는지를 확인한다. (passwordEncoder를 사용하여 인코딩된 패스워드가 일치하는지 확인)
        if(!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException("Password not matched");
        }

        return account;
    }

}
