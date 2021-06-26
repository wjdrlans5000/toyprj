package me.gimun.documentapproval.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    // 실제로 signWith에 해당하는 부분은 훨씬더 복잡한 Key를 byte형식으로 생성해서 줘야된다.
    // 원래 이 secret 키는 외부 노출되면 안됨
    private static final String secret = "jwtpassword";

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    //토큰으로 유저네임 확인
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String generateToken(String id) {
        return generateToken(id, new HashMap<>());
    }

    public String generateToken(String id, Map<String, Object> claims) {
        return doGenerateToken(id, claims);
    }

    private String doGenerateToken(String id, Map<String, Object> claims) {
        //[5].사용자 이메일로 토큰을 생성한다.
        return Jwts.builder()
                .setClaims(claims) //Payload(내용) 정보 하나하나를 각각 클레임 (claim)이라고 부른다 Claim은 key/value
                .setId(id)
                .setIssuedAt(new Date(System.currentTimeMillis())) //토큰발행된 시간
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000)) //토큰 만료시간 설정
                .signWith(SignatureAlgorithm.HS512, secret) //토큰을 탈취하지 못하도록 secret key로 Hash값을 추출해 비밀키로 복호화한 뒤 토큰의 뒤에 붙여줍니다.
                .compact(); //토큰생성
    }
    //토큰의 유저네임과 db에 조회한 유저네임이 같고 토큰이 만료되지 않은경우 true
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}
