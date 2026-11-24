package com.hyeon.interlibraryloan.book.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibRecordRepository extends JpaRepository<BibRecord, Long> {

	Optional<BibRecord> findByIsbn13(String isbn13);
}
