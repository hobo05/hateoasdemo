package com.example;

import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by tcheng on 3/25/16.
 */
@RestController
@RequestMapping("/persons")
public class HateoasController {

    Map<Integer, Person> database = new HashMap<>();

    PersonAssembler assembler = new PersonAssembler();

    @Data
    @Builder
    public static class Person {
        private int id;
        private String name;
        private Integer age;
        private Person coworker;
    }

    @Data
    public static class PersonView extends ResourceSupport {
        private String identifier;
        private Integer coworkerAge;
    }


    public static class PersonAssembler extends ResourceAssemblerSupport<Person, PersonView> {
        public PersonAssembler() {
            super(HateoasController.class, PersonView.class);
        }

        @Override
        public PersonView toResource(Person entity) {

            PersonView personView = createResourceWithId(entity.id, entity);
            personView.setIdentifier(String.format("%s - %d", entity.getName(), entity.getAge()));

            // Set coworker age using old school JDK 7 way
//            if (entity.coworker != null && entity.coworker.age != null) {
//                personView.setCoworkerAge(entity.coworker.age);
//            }

            // Set age even if null
//            Optional.ofNullable(entity.coworker)
//                    .ifPresent(p -> personView.setCoworkerAge(p.age));

            // Use method reference
            getCoworker(entity)
                    .flatMap(this::getAge)
                    .ifPresent(personView::setCoworkerAge);

            personView.add(new Link("http://galleries.com", "gallery"));
            return personView;
        }

        private Optional<Person> getCoworker(Person person) {
            return Optional.ofNullable(person.coworker);
        }

        private Optional<Integer> getAge(Person person) {
            return Optional.ofNullable(person.age);
        }
    }

    @PostConstruct
    public void postContruct() {
        Person devi = Person.builder()
                .id(1)
                .name("Devi")
                .age(99)
                .build();
        Person mike = Person.builder()
                .id(2)
                .name("Mike")
                .age(100)
                .build();

        Person tim = Person.builder()
                .id(3)
                .name("Tim")
                .build();

        mike.setCoworker(devi);

        database.put(1, devi);
        database.put(2, mike);
        database.put(3, tim);
    }

    @RequestMapping("")
    public List<PersonView> getAll() {
        return assembler.toResources(database.values());
    }

    @RequestMapping("/{id}")
    public PersonView getPerson(@PathVariable("id") Integer id) {
        return assembler.toResource(database.get(id));
    }
}
