package com.sbproject.deokhugam.domain.dashboard.repository;

import com.sbproject.deokhugam.domain.dashboard.document.PopularBooksDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PopularBooksRepository extends MongoRepository<PopularBooksDocument, String> {

  Optional<PopularBooksDocument> findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType periodType);
}