package com.sbproject.deokhugam.domain.review.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sbproject.deokhugam.domain.BaseEntity;
import com.sbproject.deokhugam.domain.book.entity.Book;
import com.sbproject.deokhugam.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "reviews")
@Getter @Setter @SuperBuilder @ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "rating", nullable = false)
	private Integer rating;

	@Column(name = "like_count", nullable = false)
	@Builder.Default
	private Integer likeCount = 0;

	@Column(name = "comment_count", nullable = false)
	@Builder.Default
	private Integer commentCount = 0;
}
