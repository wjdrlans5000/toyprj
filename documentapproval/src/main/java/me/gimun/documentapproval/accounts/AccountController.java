package me.gimun.documentapproval.accounts;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {
    final AccountRepository accountRepository;
    final PasswordEncoder encode;

    @PostMapping("/api/account")
    public String saveMember(@RequestBody AccountDto accountDto) {
        Account account =  accountRepository.save(Account.createAccount(accountDto.getEmail(), encode.encode(accountDto.getPassword())));
        return "success, id : " + account.getId();
    }

}

@Data
class AccountDto {
    private String email;
    private String password;
}