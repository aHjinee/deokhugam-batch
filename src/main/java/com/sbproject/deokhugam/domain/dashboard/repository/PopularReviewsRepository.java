package com.sbproject.deokhugam.domain.dashboard.repository;

import com.sbproject.deokhugam.domain.dashboard.document.PopularReviewsDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PopularReviewsRepository extends MongoRepository<PopularReviewsDocument, String> {

  Optional<PopularReviewsDocument> findTopByPeriodTypeOrderByPeriodDateDesc (PeriodType periodType);
}