package com.example.manager.repo;

import com.example.manager.entity.UnsentTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnsentTaskRepository extends MongoRepository<UnsentTask, String> {

}
