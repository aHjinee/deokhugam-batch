package com.sbproject.deokhugam.domain.dashboard.document;

import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@TypeAlias("power_users")
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

  public static PowerUsersDocument create(PeriodType periodType, Instant periodDate, List<Ranking> rankings, Instant now) {
	  PowerUsersDocument doc = new PowerUsersDocument();
	  doc.periodType = periodType;
	  doc.periodDate = periodDate;
	  doc.rankings = rankings;
	  doc.createdAt = now;
	  doc.updatedAt = now;
	  return doc;
  }

  public void update(List<Ranking> rankings, Instant now) {
	  this.rankings = rankings;
	  this.updatedAt = now;
  }


  @Getter
  @NoArgsConstructor
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

	public Ranking(int rank, String userId, String nickname,
		double activityScore, double reviewScore,
		int likeCount, int commentCount) {
		this.rank = rank;
		this.userId = userId;
		this.nickname = nickname;
		this.activityScore = activityScore;
		this.reviewScore = reviewScore;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
	}
  }
}