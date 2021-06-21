package me.gimun.documentapproval.config;

import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.auth.model.UserAccount;
import org.assertj.core.util.Lists;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Account account = Account.builder()
                .email(customUser.username())
                .password("custom")
                .id(1)
                .build();
        UserAccount principal = new UserAccount(account,
                Lists.newArrayList(new SimpleGrantedAuthority(customUser.roles()))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
