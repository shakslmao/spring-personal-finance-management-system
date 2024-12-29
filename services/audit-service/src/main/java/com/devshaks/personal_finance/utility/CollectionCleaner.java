package com.devshaks.personal_finance.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.utility.collection.cleaner.enabled", havingValue = "true", matchIfMissing = false)
public class CollectionCleaner implements CommandLineRunner {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        mongoTemplate.getDb().drop();
        System.out.println("\"Database dropped successfully.\"");
    }
}
