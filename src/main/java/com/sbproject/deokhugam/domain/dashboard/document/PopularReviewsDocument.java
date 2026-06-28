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
@Document(collection = "popular_reviews")
public class PopularReviewsDocument {

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

    @Field("review_id")
    private String reviewId;

    @Field("user_id")
    private String userId;

    @Field("book_id")
    private String bookId;

    private String nickname;
    private String title;

    @Field("thumbnail_url")
    private String thumbnailUrl;

    private String content;
    private double rating;
    private double score;

    @Field("like_count")
    private int likeCount;

    @Field("comment_count")
    private int commentCount;
  }
}