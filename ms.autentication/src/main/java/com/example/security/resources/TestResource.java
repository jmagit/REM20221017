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
	
	@Value("${jwt.key.private}")
	private String privateKeyContent;
	@Value("${jwt.key.public}")
	private String publicKeyContent;
	
	@GetMapping("/rsa/encode")
	@PreAuthorize("permitAll")
	@SecurityRequirement(name = "bearerAuth")
	public String generateToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        RSAPrivateKey privateKey = (RSAPrivateKey)kf.generatePrivate(keySpecPKCS8);
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

		String token = JWT.create()
				.withIssuer("MicroserviciosJWT")
				.withClaim("name", "Hola Mundo")
				.withIssuedAt(new Date(System.currentTimeMillis())).withNotBefore(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60_000))
				.sign(Algorithm.RSA256(publicKey, privateKey));
		DecodedJWT rslt = JWT.require(Algorithm.RSA256(publicKey, null)).withIssuer("MicroserviciosJWT").build()
				.verify(token);
		
		return token;
	}
	@GetMapping("/rsa/decode")
	@PreAuthorize("permitAll")
	@SecurityRequirement(name = "bearerAuth")
	public String decodeToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");

        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

		DecodedJWT rslt = JWT.require(Algorithm.RSA256(publicKey, null)).withIssuer("MicroserviciosJWT").build()
				.verify(token);
		
		return rslt.getClaim("name").asString();
	}
}
