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
@TypeAlias("popular_books")
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

  public static PopularBooksDocument create(PeriodType periodType, Instant periodDate, List<Ranking> rankings, Instant now) {
	  PopularBooksDocument doc = new PopularBooksDocument();
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

	public Ranking(int rank, String bookId, String title, String author,
		String thumbnailUrl, double score, int reviewCount, double avgRating) {
		this.rank = rank;
		this.bookId = bookId;
		this.title = title;
		this.author = author;
		this.thumbnailUrl = thumbnailUrl;
		this.score = score;
		this.reviewCount = reviewCount;
		this.avgRating = avgRating;
	}
  }
}