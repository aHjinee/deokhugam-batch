package com.sbproject.deokhugam.domain.book.entity;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sbproject.deokhugam.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@Table(name = "books")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

  @Size(max = 50)
  @NotNull
  @Column(name = "isbn", nullable = false, length = 50)
  private String isbn;

  @Size(max = 255)
  @NotNull
  @Column(name = "title", nullable = false)
  private String title;

  @Size(max = 255)
  @NotNull
  @Column(name = "author", nullable = false)
  private String author;

  @NotNull
  @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
  private String description;

  @Size(max = 100)
  @NotNull
  @Column(name = "publisher", nullable = false, length = 100)
  private String publisher;

  @NotNull
  @Column(name = "published_date", nullable = false)
  private LocalDate publishedDate;

  @Column(name = "thumbnail_url", length = Integer.MAX_VALUE)
  private String thumbnailUrl;

  @NotNull
  @ColumnDefault("0")
  @Column(name = "review_count", nullable = false)
  private Integer reviewCount;

  @NotNull
  @ColumnDefault("0")
  @Column(name = "total_score", nullable = false)
  private Integer totalScore;

  @NotNull
  @ColumnDefault("0.0")
  @Column(name = "rating", nullable = false)
  private Double rating;

}
