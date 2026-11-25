package com.hyeon.interlibraryloan.member.domain.repository;

import com.hyeon.interlibraryloan.member.domain.entity.Staff;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {

	List<Staff> findByLibraryIdAndRole(Long libraryId, UserRole role);
}
