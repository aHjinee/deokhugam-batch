package com.sbproject.deokhugam.domain.dashboard.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@TypeAlias("userActivityStats")
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
  private Integer dailyPowerRank;

  @Field("created_at")
  private Instant createdAt;

  @Field("updated_at")
  private Instant updatedAt;

	public static UserActivityStatsDocument create(
		String userId,
		Instant activityDate,
		int reviewCount,
		int commentCount,
		int likeCount,
		int receivedCommentCount,
		int receivedLikeCount,
		Instant now
	) {
		UserActivityStatsDocument document =
			new UserActivityStatsDocument();

		document.userId = userId;
		document.activityDate = activityDate;
		document.reviewCount = reviewCount;
		document.commentCount = commentCount;
		document.likeCount = likeCount;
		document.receivedCommentCount = receivedCommentCount;
		document.receivedLikeCount = receivedLikeCount;

		// 순위는 새벽 랭킹 배치에서 확정
		document.dailyPowerRank = null;

		document.createdAt = now;
		document.updatedAt = now;

		return document;
	}

	/**
	 * 5분 활동 통계 배치에서 사용한다.
	 * 순위는 변경하지 않는다.
	 */
	public void update(
		int reviewCount,
		int commentCount,
		int likeCount,
		int receivedCommentCount,
		int receivedLikeCount,
		Instant now
	) {
		this.reviewCount = reviewCount;
		this.commentCount = commentCount;
		this.likeCount = likeCount;
		this.receivedCommentCount = receivedCommentCount;
		this.receivedLikeCount = receivedLikeCount;
		this.updatedAt = now;
	}


	public void updateDailyPowerRank(
		int dailyPowerRank,
		Instant now
	) {
		this.dailyPowerRank = dailyPowerRank;
		this.updatedAt = now;
	}
}