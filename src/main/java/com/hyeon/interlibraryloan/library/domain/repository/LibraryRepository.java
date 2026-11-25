package com.hyeon.interlibraryloan.library.domain.repository;

import com.hyeon.interlibraryloan.library.domain.entity.Library;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, Long> {

	Optional<Library> findByCode(String code);
}
