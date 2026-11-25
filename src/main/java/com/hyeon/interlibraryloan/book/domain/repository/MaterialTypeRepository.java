package com.hyeon.interlibraryloan.book.domain.repository;

import com.hyeon.interlibraryloan.book.domain.entity.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialTypeRepository extends JpaRepository<MaterialType, String> {
}
