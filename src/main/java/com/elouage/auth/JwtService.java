package com.elouage.auth;

import com.elouage.utilisateur.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long dureeMs;

    public JwtService(@Value("${elouage.jwt.secret}") String secret,
                      @Value("$elouage.jwt.duree-minutes") long minutes){
        this.key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.dureeMs=minutes*60_000;
    }

    public String generer(Utilisateur u){
        Date now=new Date();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("role",u.getRole())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + dureeMs))
                .signWith(key)
                .compact();
    }

    public Claims lire(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

}
