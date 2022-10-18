package com.example;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;

import com.example.domains.contracts.repositories.ActorRepository;
import com.example.domains.entities.Actor;
import com.example.domains.entities.dtos.ActorDTO;
import com.example.domains.entities.dtos.ActorShort;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	ActorRepository dao;
	
	@Override
	@Transactional
	public void run(String... args) throws Exception {
//		System.out.println("Hola mundo");
//		var actor = new Actor(208, "NUEVO", "Actor");
//		dao.save(actor);
//		dao.deleteById(206);
//		dao.findAll().forEach(System.out::println);
//		var item = dao.findById(205);
//		if(item.isEmpty())
//			System.out.println("No lo encuentro");
//		else {
//			var actor = item.get();
//			actor.setFirstName(actor.getFirstName().toUpperCase());
//			actor = dao.save(actor);
//			System.out.println(actor);
//		}
//		dao.findTop5ByFirstNameStartingWithOrderByLastNameDesc("p").forEach(System.out::println);
//		dao.findTop5ByFirstNameStartingWith("p", Sort.by("actorId")).forEach(System.out::println);
//		dao.findByActorIdGreaterThan(200).forEach(System.out::println);
//		dao.findConJPQL(200).forEach(System.out::println);
//		dao.findConSQL(200).forEach(System.out::println);
//		dao.findAll((root, query, builder) -> builder.greaterThan(root.get("actorId"), 200)).forEach(System.out::println);
//		dao.findAll((root, query, builder) -> builder.lessThan(root.get("actorId"), 10)).forEach(System.out::println);
//		var item = dao.findById(1);
//		if(item.isEmpty())
//			System.out.println("No lo encuentro");
//		else {
//			var actor = item.get();
//			System.out.println(actor);
//			actor.getFilmActors().forEach(f -> System.out.println(f.getFilm().getTitle()));
//		}
//		var actor = new Actor(208, null, "12345678z");
//		if(actor.isInvalid())
//			System.out.println(actor.getErrorsMessage());
//		else
//			dao.save(actor);
//		dao.findAll().forEach(f -> System.out.println(ActorDTO.from(f)));
//		var dto = new ActorDTO(0, null, "12345678z");
//		dao.save(ActorDTO.from(dto));
//		dao.findByActorIdGreaterThan(200).forEach(System.out::println);
//		dao.findByActorIdIsNotNull(ActorDTO.class).forEach(System.out::println);
		dao.findByActorIdIsNotNull(ActorShort.class).forEach(f -> System.out.println(f.getId() + " " + f.getNombre()));
	}

}
