package com.example.domains.entities.dtos;

import com.example.domains.entities.Actor;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ActorDTO {
	@JsonProperty("id")
	private int actorId;
	@JsonProperty("nombre")
	private String firstName;
	@JsonProperty("apellidos")
	private String lastName;

	public static Actor from(ActorDTO targer) {
		return new Actor(
				targer.getActorId(), 
				targer.getFirstName(), 
				targer.getLastName());
	}
	public static ActorDTO from(Actor targer) {
		return new ActorDTO(
				targer.getActorId(), 
				targer.getFirstName(), 
				targer.getLastName());
	}
}
