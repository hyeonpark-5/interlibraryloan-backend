package com.hyeon.interlibraryloan.book.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.TestcontainersConfiguration;
import com.hyeon.interlibraryloan.library.domain.Library;
import com.hyeon.interlibraryloan.library.domain.LibraryRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class BookRepositoryTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 25);

	@Autowired
	BibRecordRepository bibRecordRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	MaterialTypeRepository materialTypeRepository;

	@Autowired
	LibraryRepository libraryRepository;

	@Autowired
	EntityManager entityManager;

	@Autowired
	JdbcTemplate jdbcTemplate;

	Library gangnam;
	Library jeongdok;
	MaterialType general;
	MaterialType reference;

	@BeforeEach
	void setUp() {
		gangnam = libraryRepository.findByCode("MA").orElseThrow();
		jeongdok = libraryRepository.findByCode("MT").orElseThrow();
		general = materialTypeRepository.findById("GENERAL").orElseThrow();
		reference = materialTypeRepository.findById("REFERENCE").orElseThrow();
	}

	@Test
	@DisplayName("서지와 소장 자료를 저장하고 도서관과 등록번호로 다시 읽는다")
	void saveAndFind() {
		// given
		BibRecord bib = bibRecordRepository.save(BookFixtures.bibRecord("8985130692"));
		itemRepository.save(BookFixtures.availableItem(gangnam, bib, general, "EMA000000001"));
		entityManager.flush();
		entityManager.clear();

		// when
		Item found = itemRepository.findByLibraryIdAndBarcode(gangnam.getId(), "EMA000000001").orElseThrow();

		// then
		assertThat(found.getBibRecord().getIsbn13()).isEqualTo("9788985130691");
		assertThat(found.getLibrary().getCode()).isEqualTo("MA");
		assertThat(found.getMaterialType().getCode()).isEqualTo("GENERAL");
		assertThat(found.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
		assertThat(found.getVersion()).isZero();
		assertThat(found.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("BR-01 참고자료 유형의 자료는 DB에서 읽은 자료 유형 기준으로 배정할 수 없다")
	void eligibilityUsesStoredMaterialType() {
		// given
		BibRecord bib = bibRecordRepository.save(BookFixtures.bibRecord("9780306406157"));
		Item generalItem = itemRepository.save(BookFixtures.availableItem(gangnam, bib, general, "EMA000000002"));
		Item referenceItem = itemRepository.save(BookFixtures.availableItem(gangnam, bib, reference, "EMA000000003"));

		// when, then
		assertThat(generalItem.isInterlibraryEligible(0, TODAY)).isTrue();
		assertThat(referenceItem.interlibraryIneligibleReasons(0, TODAY))
				.containsExactly(InterlibraryIneligibleReason.MATERIAL_TYPE_NOT_ALLOWED);
	}

	@Test
	@DisplayName("같은 ISBN의 서지는 두 번 저장할 수 없다")
	void isbnIsUnique() {
		bibRecordRepository.saveAndFlush(BookFixtures.bibRecord("9791162241462"));

		assertThatThrownBy(() -> bibRecordRepository.saveAndFlush(BookFixtures.bibRecord("9791162241462")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("등록번호는 도서관 안에서만 유일하다")
	void barcodeIsUniquePerLibrary() {
		// given
		BibRecord bib = bibRecordRepository.save(BookFixtures.bibRecord("9780804429573"));
		itemRepository.saveAndFlush(BookFixtures.availableItem(gangnam, bib, general, "SAME-001"));

		// when: 다른 도서관은 같은 등록번호를 쓸 수 있다
		itemRepository.saveAndFlush(BookFixtures.availableItem(jeongdok, bib, general, "SAME-001"));

		// then: 같은 도서관에서는 안 된다
		assertThatThrownBy(() -> itemRepository.saveAndFlush(
				BookFixtures.availableItem(gangnam, bib, general, "SAME-001")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("DB도 13자리 ISBN 형식이 아닌 값을 막는다")
	void dbRejectsInvalidIsbn() {
		assertThatThrownBy(() -> jdbcTemplate.update("""
				insert into bib_record (id, isbn13, title, created_at, updated_at)
				values (nextval('bib_record_seq'), '8985130692', '10자리 ISBN', now(), now())
				"""))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("DB는 정해진 목록에 없는 자료 상태를 막는다")
	void dbRejectsUnknownStatus() {
		// given
		BibRecord bib = bibRecordRepository.save(BookFixtures.bibRecord("9788985130691"));
		Item item = itemRepository.saveAndFlush(BookFixtures.availableItem(gangnam, bib, general, "EMA000000004"));

		// when, then
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update item set status = 'BORROWED' where id = ?", item.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
