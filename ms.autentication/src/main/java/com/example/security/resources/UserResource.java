package com.example.security.resources;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.security.dtos.AuthToken;
import com.example.security.dtos.BasicCredential;
import com.example.security.entities.Usuario;
import com.example.security.repositories.UsuarioRepositoy;
import com.netflix.eureka.registry.rule.AlwaysMatchInstanceStatusRule;

@RestController
//	@CrossOrigin(origins = "http://localhost:4200", allowCredentials="true", methods={RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS })
//	@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials="false")
public class UserResource {
	@Value("${jwt.key.private}")
	private String privateKeyContent;
	@Value("${jwt.key.public}")
	private String publicKeyContent;
	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;
	@Value("${jwt.expiracion.mim:10}")
	private int EXPIRES_IN_MINUTES;
	@Value("${jwt.refresh.factor:4}")
	private int REFRESH_FACTOR=4;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UsuarioRepositoy dao;
	// { "username": "adm@example.com", "password": "P@$$w0rd" }

	@PostConstruct
	private void inicializa() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        privateKey = (RSAPrivateKey)kf.generatePrivate(keySpecPKCS8);
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
	}
	
	@PostMapping(path = "/login", consumes = "application/json")
	public AuthToken loginJSON(@Valid @RequestBody BasicCredential credential)  {
		var item = dao.findById(credential.getUsername());
		if (item.isEmpty() || !item.get().isActive() || !passwordEncoder.matches(credential.getPassword(), item.get().getPassword()))
			return new AuthToken();
		return getAuthToken(item.get());
	}

	@PostMapping(path = "/refresh", consumes = "application/json")
	public AuthToken refresh(@Valid @RequestBody String token)  {
		DecodedJWT decode = JWT.require(Algorithm.RSA256(publicKey, null)).withSubject("refresh").build()
				.verify(token);
		var item = dao.findById(decode.getClaim("usr").asString());
		if (item.isEmpty() || !item.get().isActive())
			return new AuthToken();
		return getAuthToken(item.get());
	}

	private AuthToken getAuthToken(Usuario usr) {
		return new AuthToken(true, "Bearer " + getToken(usr), getRefreshToken(usr), usr.getNombre(), usr.getRoles().stream().map(s->s.substring(5)).toList(), EXPIRES_IN_MINUTES * 60);
	}

	private String getToken(Usuario usr) {
		String token = JWT.create()
				.withIssuer("MicroserviciosJWT")
				.withSubject("identity")
				.withClaim("usr", usr.getIdUsuario())
				.withArrayClaim("roles", usr.getRoles().toArray(new String[0]))
				.withIssuedAt(new Date(System.currentTimeMillis())).withNotBefore(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRES_IN_MINUTES * 60_000))
				.sign(Algorithm.RSA256(publicKey, privateKey));
		return token;
	}

	private String getRefreshToken(Usuario usr) {
		String token = JWT.create()
				.withIssuer("MicroserviciosJWT")
				.withSubject("refresh")
				.withClaim("usr", usr.getIdUsuario())
				.withIssuedAt(new Date(System.currentTimeMillis())).withNotBefore(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_FACTOR * EXPIRES_IN_MINUTES * 60_000))
				.sign(Algorithm.RSA256(publicKey, privateKey));
		return token;
	}

	/*
	 * /register (anonimo) 
	 * /changepassword 
	 * /profile (Authorization) (get, put) menos la contraseña 
	 * /users (Admin) (get, post, put, delete) + roles menos la contraseña
	 *
	 */
}
