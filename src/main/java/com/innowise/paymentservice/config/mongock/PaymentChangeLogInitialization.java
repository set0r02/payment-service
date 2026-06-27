package com.innowise.paymentservice.config.mongock;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@ChangeUnit(id = "init-payments-database", order = "001", author = "Ilya")
public class PaymentChangeLogInitialization {

    @Execution
    void execute(MongoTemplate mongoTemplate){
        if(!mongoTemplate.collectionExists("payments")){
            mongoTemplate.createCollection("payments");
        }

        mongoTemplate.indexOps("payments")
                .createIndex(new Index().on("user_id", Sort.Direction.ASC));

        mongoTemplate.indexOps("payments")
                .createIndex(new Index().on("order_id",Sort.Direction.ASC).unique());

        mongoTemplate.indexOps("payments")
                .createIndex(new Index("status",Sort.Direction.ASC));
    }

    @RollbackExecution
    void rollBackExecute(MongoTemplate mongoTemplate){
        mongoTemplate.dropCollection("payments");
    }

}
