package com.sbproject.deokhugam.domain.dashboard.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sbproject.deokhugam.domain.dashboard.document.PowerUsersDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;

public interface PowerUsersRepository extends MongoRepository<PowerUsersDocument, String> {

  Optional<PowerUsersDocument> findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType periodType);
}