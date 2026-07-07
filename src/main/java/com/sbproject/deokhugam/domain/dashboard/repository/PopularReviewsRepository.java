package com.sbproject.deokhugam.domain.dashboard.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sbproject.deokhugam.domain.dashboard.document.PopularReviewsDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;

public interface PopularReviewsRepository extends MongoRepository<PopularReviewsDocument, String> {

  Optional<PopularReviewsDocument> findTopByPeriodTypeOrderByPeriodDateDesc (PeriodType periodType);
}