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

    //[1].리퀘스트 바디로 사용자 이메일과 패스워드를 받아서 저장한다.
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