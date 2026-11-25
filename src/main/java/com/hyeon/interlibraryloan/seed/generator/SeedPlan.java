package com.hyeon.interlibraryloan.seed.generator;

import java.util.List;

/**
 * 생성기가 정한, DB에 넣을 합성 데이터 전체.
 */
public record SeedPlan(List<StaffSeed> staff, List<PatronSeed> patrons) {

	public SeedPlan {
		staff = List.copyOf(staff);
		patrons = List.copyOf(patrons);
	}
}
