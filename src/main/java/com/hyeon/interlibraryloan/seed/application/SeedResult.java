package com.hyeon.interlibraryloan.seed.application;

/**
 * 합성 데이터 실행 결과.
 *
 * @param skipped 이미 데이터가 있어서 아무것도 하지 않았는지
 */
public record SeedResult(boolean skipped, int staffCount, int patronCount) {

	public static SeedResult skippedResult() {
		return new SeedResult(true, 0, 0);
	}

	public static SeedResult created(int staffCount, int patronCount) {
		return new SeedResult(false, staffCount, patronCount);
	}
}
