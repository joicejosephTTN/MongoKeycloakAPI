package com.ttn.MongoDBDemo.collection;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
public class Address {
    private String addressLineOne;
    private String addressLineTwo;
    private String city;
    private String country;
}
