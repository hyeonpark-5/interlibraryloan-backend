package com.hyeon.interlibraryloan.seed.generator;

import java.util.List;
import java.util.Random;

/**
 * 가짜 한국어 이름. 성 하나와 이름 두 글자를 조합한다.
 * 실존 인물을 흉내 내지 않도록 흔한 글자만 무작위로 섞는다.
 */
final class KoreanNameGenerator {

	private static final List<String> SURNAMES = List.of(
			"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
			"한", "오", "서", "신", "권", "황", "안", "송", "류", "홍");

	private static final List<String> GIVEN_SYLLABLES = List.of(
			"민", "서", "지", "현", "준", "우", "윤", "하", "도", "연",
			"수", "아", "예", "은", "진", "호", "재", "유", "원", "채",
			"승", "다", "시", "혜", "영", "성", "태", "나", "주", "린");

	private final Random random;

	KoreanNameGenerator(Random random) {
		this.random = random;
	}

	String next() {
		return pick(SURNAMES) + pick(GIVEN_SYLLABLES) + pick(GIVEN_SYLLABLES);
	}

	private String pick(List<String> values) {
		return values.get(random.nextInt(values.size()));
	}
}
