package com.hyeon.interlibraryloan.member.domain.repository;

import com.hyeon.interlibraryloan.member.domain.entity.Patron;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatronRepository extends JpaRepository<Patron, Long> {

	Optional<Patron> findByLibraryIdAndCardNumber(Long libraryId, String cardNumber);

	long countByLibraryId(Long libraryId);
}
