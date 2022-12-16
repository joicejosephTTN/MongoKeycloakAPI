package com.ttn.MongoDBDemo.service;

import com.ttn.MongoDBDemo.collection.Person;
import com.ttn.MongoDBDemo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // Should use a DTO instead of a person obj
    public String addPerson(Person person){
        return personRepository.save(person).getPersonId();
    }

    public List<Person> getPerson(String name){
        return personRepository.findByFirstNameIgnoreCase(name);
    }

    public String removePerson(String id){
        Optional<Person> person = personRepository.findById(id);
        Person personToDelete = person.get();
        personRepository.delete(personToDelete);
        return "Deleted Successfully";
    }

    public List<Person> getPersonByAgeBetween(Integer min, Integer max){
        return personRepository.findPersonByAgeBetween(min, max);
    }

    public Page<Person> search(String name, Integer minAge, Integer maxAge, String city, Pageable pageable) {

        Query query = new Query().with(pageable);
        List<Criteria> criteria = new ArrayList<>();

        if(name!=null && !name.isEmpty()){
            criteria.add(Criteria.where("firstName").regex(name,"i"));
        }
        if(minAge!=null && maxAge!=null){
            criteria.add(Criteria.where("age").gte(minAge).lte(maxAge));
        }

        if(city!=null && !city.isEmpty()){
            criteria.add(Criteria.where("addresses.city").is(city));
        }

        if(!criteria.isEmpty()){
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        Page<Person> people = PageableExecutionUtils.getPage(
                mongoTemplate.find(query,Person.class), pageable,
                () -> mongoTemplate.count(query.skip(0).limit(0), Person.class));

        return people;
    }

    public List<Document> getPopulationByCity() {

        UnwindOperation unwindOperation
                = Aggregation.unwind("addresses");
        GroupOperation groupOperation
                = Aggregation.group("addresses.city")
                .count().as("popCount");
        SortOperation sortOperation
                = Aggregation.sort(Sort.Direction.DESC, "popCount");

        ProjectionOperation projectionOperation
                = Aggregation.project()
                .andExpression("_id").as("city")
                .andExpression("popCount").as("count")
                .andExclude("_id");

        Aggregation aggregation
                = Aggregation.newAggregation(unwindOperation,groupOperation,sortOperation,projectionOperation);

        List<Document> documents
                = mongoTemplate.aggregate(aggregation,
                Person.class,
                Document.class).getMappedResults();
        return  documents;
    }
}
