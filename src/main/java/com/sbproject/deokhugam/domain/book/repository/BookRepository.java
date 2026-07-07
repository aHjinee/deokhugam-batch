package com.sbproject.deokhugam.domain.book.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbproject.deokhugam.domain.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, UUID>, BookQueryRepository {

}
