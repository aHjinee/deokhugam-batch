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
@TypeAlias("popular_reviews")
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

  public static PopularReviewsDocument create(PeriodType periodType, Instant periodDate, List<Ranking> rankings, Instant now) {
  	PopularReviewsDocument doc = new PopularReviewsDocument();
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


	public Ranking(int rank, String reviewId, String userId, String bookId,
		String nickname, String title, String thumbnailUrl,
		String content, double rating, double score,
		int likeCount, int commentCount) {
		this.rank = rank;
		this.reviewId = reviewId;
		this.userId = userId;
		this.bookId = bookId;
		this.nickname = nickname;
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.content = content;
		this.rating = rating;
		this.score = score;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
	}
  }
}

