package com.authservice.security;

import com.authservice.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

	@Value("${app.jwtSecret}")
	private String jwtSecret;

	@Value("${app.jwtExpirationMs}")
	private int jwtExpirationMs;

	@Value("${app.jwtCookieName}")
	private String jwtCookieName;

	// Convert the string secret into a SecretKey
	private SecretKeySpec getSigningKey() {
		return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
	}

	public String generateJwtToken(UserDetailsImpl userPrincipal) {

		var roles = userPrincipal.getAuthorities().stream().map(a -> a.getAuthority()) // e.g. "ROLE_ADMIN"
				.toList();

		return Jwts.builder().setSubject(userPrincipal.getUsername()).claim("roles", roles).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	public String getJwtFromCookies(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;

		for (Cookie cookie : request.getCookies()) {
			if (jwtCookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
		String jwt = generateTokenFromUsername(userPrincipal.getUsername());
		return ResponseCookie.from(jwtCookieName, jwt).path("/").httpOnly(true).maxAge(jwtExpirationMs / 1000).build();
	}

	public ResponseCookie getCleanJwtCookie() {
		return ResponseCookie.from(jwtCookieName, "").path("/").httpOnly(true).maxAge(0).build();
	}

	public String generateTokenFromUsername(String username) {
		System.out.println("AUTH-SERVICE signing key = " + java.util.Arrays.toString(getSigningKey().getEncoded()));

		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		System.out.println("AUTH-SERVICE verifying key = " + java.util.Arrays.toString(getSigningKey().getEncoded()));

		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
