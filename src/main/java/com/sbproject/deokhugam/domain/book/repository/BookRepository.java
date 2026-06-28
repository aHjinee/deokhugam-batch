package com.sbproject.deokhugam.domain.book.repository;

import com.sbproject.deokhugam.domain.book.entity.Book;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookQueryRepository {

}
