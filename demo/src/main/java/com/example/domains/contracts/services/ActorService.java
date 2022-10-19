package com.example.domains.contracts.services;

import java.util.List;

import com.example.domains.core.services.contracts.ProjectionDomainService;
import com.example.domains.entities.Actor;

public interface ActorService extends ProjectionDomainService<Actor, Integer> {
	void darPremios(List<String> premios);
}
