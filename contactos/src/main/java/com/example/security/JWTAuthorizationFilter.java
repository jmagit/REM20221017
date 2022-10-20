package com.example.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTAuthorizationFilter extends OncePerRequestFilter {
	private final String HEADER = "Authorization";
	private final String PREFIX = "Bearer ";
	private String secret;

	public JWTAuthorizationFilter(String secret) {
		super();
		this.secret = secret;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			String authenticationHeader = request.getHeader(HEADER);
			if (authenticationHeader != null && authenticationHeader.startsWith(PREFIX)) {
		        KeyFactory kf = KeyFactory.getInstance("RSA");
		        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(secret));
		        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
				DecodedJWT token = JWT.require(Algorithm.RSA256(publicKey, null)).withIssuer("MicroserviciosJWT").build()
						.verify(authenticationHeader.substring(PREFIX.length()));
				List<GrantedAuthority> authorities = token.getClaim("roles").asList(String.class).stream()
						.map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						token.getClaim("usr").toString(), null, authorities);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch(JWTVerificationException ex) {
			response.sendError(403, ex.getMessage());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			response.sendError(500, ex.getMessage());
		} finally {
			chain.doFilter(request, response);
		}
	}
}
