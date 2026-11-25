package com.hyeon.interlibraryloan.seed.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * seed 프로필로 실행하면 합성 데이터를 넣고 끝난다.
 * application-seed.yaml이 웹 서버를 띄우지 않게 해서, 넣은 뒤 앱이 종료된다.
 */
@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class SeedRunner implements ApplicationRunner {

	private final SyntheticDataSeeder seeder;

	@Override
	public void run(ApplicationArguments args) {
		SeedResult result = seeder.seed();
		if (result.skipped()) {
			log.info("이미 계정이 있어서 합성 데이터를 넣지 않았습니다. 다시 넣으려면 로컬 DB를 비우세요.");
			return;
		}
		log.info("합성 데이터를 넣었습니다. 직원 {}명, 이용자 {}명", result.staffCount(), result.patronCount());
	}
}
