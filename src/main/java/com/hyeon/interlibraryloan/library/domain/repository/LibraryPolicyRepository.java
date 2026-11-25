package com.hyeon.interlibraryloan.library.domain.repository;

import com.hyeon.interlibraryloan.library.domain.entity.LibraryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryPolicyRepository extends JpaRepository<LibraryPolicy, Long> {
}
