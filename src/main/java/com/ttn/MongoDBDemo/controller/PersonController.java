package com.ttn.MongoDBDemo.controller;

import com.ttn.MongoDBDemo.collection.Person;
import com.ttn.MongoDBDemo.repository.PersonRepository;
import com.ttn.MongoDBDemo.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.List;

@RestController
@RequestMapping(path ="/api/person" )
public class PersonController {
    @Autowired
    private PersonService personService;
    @Autowired
    private PersonRepository personRepository;

    @PostMapping(path = "/")
    ResponseEntity<String> createPerson(@RequestBody Person person){
        String response = personService.addPerson(person);
        return new ResponseEntity<String>(response, HttpStatus.CREATED);
    }

    @GetMapping(path = "/")
    ResponseEntity<List<Person>> getPerson(@RequestParam("name") String name){
        List<Person> response = personService.getPerson(name);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping(path = "/age")
    ResponseEntity<List<Person>> getPersonByAge(@RequestParam("min") Integer min, @RequestParam("max") Integer max){
        List<Person> response = personService.getPersonByAgeBetween(min,max);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping(path = "/")
    @RolesAllowed("manager")
    ResponseEntity<String> deletePerson(@RequestParam("id") String id){
        String response = personService.removePerson(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/search")
    public Page<Person> searchPerson(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size
    ){
        Pageable pageable = PageRequest.of(page,size);
        return  personService.search(name,minAge,maxAge,city,pageable);
    }

    @GetMapping("/populationByCity")
    @RolesAllowed("user")
    public List<Document> getPopulationByCity() {
        return personService.getPopulationByCity();
    }


}
