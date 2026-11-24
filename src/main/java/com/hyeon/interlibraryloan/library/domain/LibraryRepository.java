package com.hyeon.interlibraryloan.library.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, Long> {

	Optional<Library> findByCode(String code);
}
