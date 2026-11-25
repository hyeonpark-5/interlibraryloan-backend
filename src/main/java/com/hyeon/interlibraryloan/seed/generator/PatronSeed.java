package com.hyeon.interlibraryloan.seed.generator;

import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import java.time.LocalDate;

/**
 * 만들 이용자 한 명. 계정도 함께 만든다.
 *
 * @param email 실제 주소와 겹치지 않도록 영문 소문자 임의 문자열
 * @param phone 실제 번호와 겹치지 않도록 영문 소문자 임의 문자열
 */
public record PatronSeed(
		String loginId,
		String libraryCode,
		String cardNumber,
		String name,
		String email,
		String phone,
		LocalDate birthDate,
		PatronStatus status,
		LocalDate suspendedUntil) {
}
