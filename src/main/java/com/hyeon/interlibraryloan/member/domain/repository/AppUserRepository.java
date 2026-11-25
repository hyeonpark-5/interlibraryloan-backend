package com.hyeon.interlibraryloan.member.domain.repository;

import com.hyeon.interlibraryloan.member.domain.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByLoginId(String loginId);
}
