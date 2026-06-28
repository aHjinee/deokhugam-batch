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
@Document(collection = "power_users")
public class PowerUsersDocument {

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

    @Field("user_id")
    private String userId;

    private String nickname;

    @Field("activity_score")
    private double activityScore;

    @Field("review_score")
    private double reviewScore;

    @Field("like_count")
    private int likeCount;

    @Field("comment_count")
    private int commentCount;
  }
}