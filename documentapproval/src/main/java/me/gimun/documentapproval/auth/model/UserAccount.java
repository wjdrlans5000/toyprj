package me.gimun.documentapproval.auth.model;

import lombok.Getter;
import me.gimun.documentapproval.accounts.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserAccount extends User {
    private final Account account;

    public UserAccount(Account account, Collection<? extends GrantedAuthority> authorities) {
        super(account.getEmail(), account.getPassword(), authorities);
        this.account = account;
    }
}
