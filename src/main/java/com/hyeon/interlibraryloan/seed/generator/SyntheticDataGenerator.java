package com.hyeon.interlibraryloan.seed.generator;

import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import com.hyeon.interlibraryloan.seed.config.SeedProperties;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 합성 데이터를 정한다. DB에 넣지는 않는다.
 * <p>
 * 같은 도서관 목록과 같은 설정이면 항상 같은 결과가 나온다. 도서관은 코드 순서로 처리한다.
 * 실제 개인정보와 겹치지 않도록 이메일과 전화번호는 형식을 흉내 내지 않은 영문 소문자 임의 문자열로 채운다.
 */
public final class SyntheticDataGenerator {

	public static final String SYSTEM_ADMIN_LOGIN_ID = "admin";

	private static final int OLDEST_AGE = 80;
	private static final int YOUNGEST_AGE = 7;
	private static final int MAX_SUSPENDED_DAYS = 30;
	private static final int EMAIL_LENGTH = 12;
	private static final int PHONE_LENGTH = 10;

	private SyntheticDataGenerator() {
	}

	public static SeedPlan generate(List<String> libraryCodes, SeedProperties properties) {
		Random random = new Random(properties.randomSeed());
		KoreanNameGenerator names = new KoreanNameGenerator(random);
		List<String> codes = libraryCodes.stream().sorted().toList();

		List<StaffSeed> staff = new ArrayList<>();
		staff.add(new StaffSeed(SYSTEM_ADMIN_LOGIN_ID, UserRole.SYSTEM_ADMIN, "시스템 관리자", null));
		for (String code : codes) {
			String prefix = code.toLowerCase(Locale.ROOT);
			staff.add(new StaffSeed(prefix + "-manager", UserRole.LIBRARY_MANAGER, names.next(), code));
			for (int i = 1; i <= properties.librariansPerLibrary(); i++) {
				staff.add(new StaffSeed(prefix + "-librarian" + i, UserRole.LIBRARIAN, names.next(), code));
			}
		}

		List<PatronSeed> patrons = new ArrayList<>();
		LocalDate reference = properties.referenceDate();
		for (String code : codes) {
			for (int i = 1; i <= properties.patronsPerLibrary(); i++) {
				String cardNumber = code + String.format("%06d", i);
				String loginId = cardNumber.toLowerCase(Locale.ROOT);
				boolean suspended = random.nextDouble() < properties.suspendedRatio();
				patrons.add(new PatronSeed(
						loginId,
						code,
						cardNumber,
						names.next(),
						randomLetters(random, EMAIL_LENGTH),
						randomLetters(random, PHONE_LENGTH),
						randomBirthDate(random, reference),
						suspended ? PatronStatus.SUSPENDED : PatronStatus.ACTIVE,
						suspended ? reference.plusDays(1 + random.nextInt(MAX_SUSPENDED_DAYS)) : null));
			}
		}
		return new SeedPlan(staff, patrons);
	}

	private static String randomLetters(Random random, int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append((char) ('a' + random.nextInt(26)));
		}
		return builder.toString();
	}

	private static LocalDate randomBirthDate(Random random, LocalDate reference) {
		LocalDate oldest = reference.minusYears(OLDEST_AGE);
		LocalDate youngest = reference.minusYears(YOUNGEST_AGE);
		long span = ChronoUnit.DAYS.between(oldest, youngest);
		return oldest.plusDays(random.nextInt((int) span + 1));
	}
}
