package com.hyeon.interlibraryloan.seed.application;

import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.library.domain.repository.LibraryRepository;
import com.hyeon.interlibraryloan.member.domain.entity.AppUser;
import com.hyeon.interlibraryloan.member.domain.entity.Patron;
import com.hyeon.interlibraryloan.member.domain.entity.Staff;
import com.hyeon.interlibraryloan.member.domain.repository.AppUserRepository;
import com.hyeon.interlibraryloan.member.domain.repository.PatronRepository;
import com.hyeon.interlibraryloan.member.domain.repository.StaffRepository;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
import com.hyeon.interlibraryloan.seed.config.SeedProperties;
import com.hyeon.interlibraryloan.seed.generator.PatronSeed;
import com.hyeon.interlibraryloan.seed.generator.SeedPlan;
import com.hyeon.interlibraryloan.seed.generator.StaffSeed;
import com.hyeon.interlibraryloan.seed.generator.SyntheticDataGenerator;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 합성 데이터를 DB에 넣는다. 계정이 하나라도 있으면 아무것도 하지 않는다.
 * 다시 넣으려면 로컬 DB를 비운 뒤 실행한다.
 */
@Service
@RequiredArgsConstructor
public class SyntheticDataSeeder {

	private final LibraryRepository libraryRepository;
	private final AppUserRepository appUserRepository;
	private final StaffRepository staffRepository;
	private final PatronRepository patronRepository;
	private final SeedProperties properties;

	@Transactional
	public SeedResult seed() {
		if (appUserRepository.count() > 0) {
			return SeedResult.skippedResult();
		}

		Map<String, Library> libraries = libraryRepository.findAll().stream()
				.collect(Collectors.toMap(Library::getCode, Function.identity()));
		SeedPlan plan = SyntheticDataGenerator.generate(new ArrayList<>(libraries.keySet()), properties);

		for (StaffSeed seed : plan.staff()) {
			AppUser user = appUserRepository.save(AppUser.create(seed.loginId(), seed.role()));
			Library library = seed.libraryCode() == null ? null : libraries.get(seed.libraryCode());
			staffRepository.save(Staff.create(user, seed.name(), library));
		}
		for (PatronSeed seed : plan.patrons()) {
			AppUser user = appUserRepository.save(AppUser.create(seed.loginId(), UserRole.PATRON));
			patronRepository.save(Patron.builder()
					.library(libraries.get(seed.libraryCode()))
					.cardNumber(seed.cardNumber())
					.name(seed.name())
					.email(seed.email())
					.phone(seed.phone())
					.birthDate(seed.birthDate())
					.status(seed.status())
					.suspendedUntil(seed.suspendedUntil())
					.appUser(user)
					.build());
		}
		return SeedResult.created(plan.staff().size(), plan.patrons().size());
	}
}
