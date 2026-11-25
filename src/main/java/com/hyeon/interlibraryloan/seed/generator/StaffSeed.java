package com.hyeon.interlibraryloan.seed.generator;

import com.hyeon.interlibraryloan.member.domain.type.UserRole;

/**
 * 만들 직원 한 명.
 *
 * @param libraryCode 소속 도서관 코드. 시스템 관리자는 null
 */
public record StaffSeed(String loginId, UserRole role, String name, String libraryCode) {
}
