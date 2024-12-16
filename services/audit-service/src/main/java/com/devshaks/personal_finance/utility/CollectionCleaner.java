package com.devshaks.personal_finance.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/*
@Component
public class CollectionCleaner implements CommandLineRunner {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        mongoTemplate.getDb().drop();
        System.out.println("\"Database dropped successfully.\"");
    }
}

*/