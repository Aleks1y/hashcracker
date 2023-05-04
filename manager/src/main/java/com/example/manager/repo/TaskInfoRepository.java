package com.example.manager.repo;

import com.example.manager.entity.RequestStatusEnum;
import com.example.manager.entity.TaskInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskInfoRepository extends MongoRepository<TaskInfo, String> {
    List<TaskInfo> findByRequestStatusAndRequestTimeBefore(RequestStatusEnum status, LocalDateTime time);
}
