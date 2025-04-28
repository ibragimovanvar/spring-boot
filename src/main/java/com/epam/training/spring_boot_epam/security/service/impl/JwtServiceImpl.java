package com.epam.training.spring_boot_epam.security.service.impl;

import com.epam.training.spring_boot_epam.domain.Token;
import com.epam.training.spring_boot_epam.exception.AuthorizationException;
import com.epam.training.spring_boot_epam.repository.TokenDao;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.epam.training.spring_boot_epam.security.service.JwtService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final TokenDao tokenDao;

    @Override
    public String generateToken(UserDetails userDetails) {
        String token = buildToken(new HashMap<>(), userDetails);

        Token savedToken = new Token();
        savedToken.setToken(token);
        savedToken.setExpired(false);
        savedToken.setUsername(userDetails.getUsername());

        tokenDao.deleteAll(tokenDao.findByUsername(userDetails.getUsername()));

        tokenDao.save(savedToken);

        return savedToken.getToken();
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * getExpirationInSeconds()))
                .signWith(getSignKey(), Jwts.SIG.HS512)
                .compact();
    }

    private SecretKey getSignKey() {
        String SECRET_KEY = "eyJhbGciOiJIUzUxMiJ9eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkphdmFJblVzZSIsImV4cCIMTcaqNDQ2ODA4MCwiaWF0IjoxNzA0NDY4MDgwfQLtk5Easw";
        byte[] key = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(key);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new AuthorizationException("Token is expired. Please login again.");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new AuthorizationException("Invalid token");
        }
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private boolean isTokenNonExpired(String token) {
        return extractClaim(token, Claims::getExpiration).after(new Date());
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        Token userToken = tokenDao.findByUsernameAndTokenAndExpiredFalse(username, token)
                .orElseThrow(() -> new AuthorizationException("Please login first"));

        if(userToken.getExpired()){
            throw new AuthorizationException("Expired token");
        }

        return username.equals(userDetails.getUsername()) && isTokenNonExpired(token);
    }

    @Override
    public long getExpirationInSeconds() {
        return 3600L;
    }
}
