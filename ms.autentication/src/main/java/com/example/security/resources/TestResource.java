package com.example.security.resources;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.security.dtos.AuthToken;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class TestResource {
	@Autowired
	PasswordEncoder passwordEncoder;

	@GetMapping("/solo-autenticados")
	@SecurityRequirement(name = "bearerAuth")
	public String get(@Parameter(hidden = true) @RequestHeader String authorization, Principal principal) {
		return "El usuario está autenticado\n  Usuario:" + principal.getName() + "\n  Authorization: " + authorization;
	}
	
	@GetMapping("/solo-admin")
	@SecurityRequirement(name = "bearerAuth")
	public String getAdmin() {
		return "El usuario es administrador";
	}
	
	@GetMapping("/password/encode")
	@SecurityRequirement(name = "bearerAuth")
	public String getPass(String pass) {
		return passwordEncoder.encode(pass);
	}
	@GetMapping("/password/validate")
	@SecurityRequirement(name = "bearerAuth")
	public String getVal(String pass, String cmp) {
		return passwordEncoder.matches(pass, cmp) ? "OK":"KO";
	}
	
	@Value("${jwt.secret}")
	private String SECRET;
	
	@GetMapping("/secreto")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public String getSecreto() {
		return SECRET;
	}
	
	@GetMapping("/hmac/encode")
	@PreAuthorize("permitAll")
	@SecurityRequirement(name = "bearerAuth")
	public String generateToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String token = JWT.create()
				.withIssuer("MicroserviciosJWT")
				.withClaim("name", "Hola Mundo")
				.withIssuedAt(new Date(System.currentTimeMillis())).withNotBefore(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60_000))
				.sign(Algorithm.HMAC256(SECRET));
		return token;
	}
	@GetMapping("/hmac/decode")
	@PreAuthorize("permitAll")
	@SecurityRequirement(name = "bearerAuth")
	public String decodeToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
		DecodedJWT rslt = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("MicroserviciosJWT").build()
				.verify(token);
		
		return rslt.getClaim("name").asString();
	}
}
