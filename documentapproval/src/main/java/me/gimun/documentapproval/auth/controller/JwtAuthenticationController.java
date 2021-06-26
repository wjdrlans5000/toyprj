package me.gimun.documentapproval.auth.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.auth.config.JwtTokenUtil;
import me.gimun.documentapproval.auth.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailService;

    //[2].로그인할 이메일과 패스워드를 사용하여 인증요청을 함
    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        final Account account = userDetailService.authenticateByEmailAndPassword
                (authenticationRequest.getEmail(), authenticationRequest.getPassword());
        //[4].사용자 인증에 성공하면 토큰을 생성한다.
        final String token = jwtTokenUtil.generateToken(account.getEmail());
        return ResponseEntity.ok(new JwtResponse(token));
    }

}

@Data
class JwtRequest {

    private String email;
    private String password;

}

@Data
@AllArgsConstructor
class JwtResponse {

    private String token;

}