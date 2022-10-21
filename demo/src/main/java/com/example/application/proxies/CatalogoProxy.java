package com.example.application.proxies;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.domains.entities.dtos.PelisDto;

@FeignClient(name = "CATALOGO-SERVICE")
public interface CatalogoProxy {
	@GetMapping(path = "/")
	String getCatalogo();
	
	@GetMapping(path = "/actuator/info")
	String getInfo();
	
	@GetMapping(path = "/v1/peliculas?mode=short")
	List<PelisDto> getPelis();
	@GetMapping(path = "/v1/peliculas?mode=details")
	List<PelisDto> getPelisConDetalle();
	
	@GetMapping(path = "/v1/peliculas/{id}?mode=short")
	PelisDto getPeli(@PathVariable int id);
	
	@PostMapping(path = "/v1/peliculas/{id}/like")
	String meGusta(@PathVariable int id);
	@PostMapping(path = "/v1/peliculas/{id}/like")
	String meGusta(@PathVariable int id, @RequestHeader(value = "Authorization", required = true) String authorization);
}
