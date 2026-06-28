package com.sbproject.deokhugam.domain.dashboard.repository;

import com.sbproject.deokhugam.domain.dashboard.document.PowerUsersDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PowerUsersRepository extends MongoRepository<PowerUsersDocument, String> {

  Optional<PowerUsersDocument> findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType periodType);
}