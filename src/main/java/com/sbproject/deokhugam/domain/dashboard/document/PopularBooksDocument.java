package com.sbproject.deokhugam.domain.dashboard.document;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@Document(collection = "popular_books")
public class PopularBooksDocument {

  @Id
  private String id;

  @Field("period_type")
  private PeriodType periodType;

  @Field("period_date")
  private Instant periodDate;

  private List<Ranking> rankings;

  @Field("created_at")
  private Instant createdAt;

  @Field("updated_at")
  private Instant updatedAt;

  @Getter
  public static class Ranking {
    private int rank;

    @Field("book_id")
    private String bookId;

    private String title;
    private String author;

    @Field("thumbnail_url")
    private String thumbnailUrl;

    private double score;

    @Field("review_count")
    private int reviewCount;

    @Field("avg_rating")
    private double avgRating;
  }
}