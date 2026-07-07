package com.sbproject.deokhugam.domain.dashboard.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sbproject.deokhugam.domain.dashboard.document.PopularBooksDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;

public interface PopularBooksRepository extends MongoRepository<PopularBooksDocument, String> {

  Optional<PopularBooksDocument> findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType periodType);
}