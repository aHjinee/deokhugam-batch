package com.sbproject.deokhugam.domain.dashboard.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "user_activity_stats")
public class UserActivityStatsDocument {

  @Id
  private String id;

  @Field("user_id")
  private String userId;

  @Field("activity_date")
  private Instant activityDate;

  @Field("review_count")
  private int reviewCount;

  @Field("comment_count")
  private int commentCount;

  @Field("like_count")
  private int likeCount;

  @Field("received_comment_count")
  private int receivedCommentCount;

  @Field("received_like_count")
  private int receivedLikeCount;

  @Field("daily_power_rank")
  private int dailyPowerRank;

  @Field("created_at")
  private Instant createdAt;

  @Field("updated_at")
  private Instant updatedAt;
}