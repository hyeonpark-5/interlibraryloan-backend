package com.hyeon.interlibraryloan.book.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.TestcontainersConfiguration;
import com.hyeon.interlibraryloan.book.domain.exception.ItemNotEligibleException;
import com.hyeon.interlibraryloan.book.domain.exception.ItemTransitionException;
import com.hyeon.interlibraryloan.book.domain.fixture.BookFixtures;
import com.hyeon.interlibraryloan.book.domain.repository.MaterialTypeRepository;
import com.hyeon.interlibraryloan.book.domain.type.InterlibraryIneligibleReason;
import com.hyeon.interlibraryloan.book.domain.type.ItemEvent;
import com.hyeon.interlibraryloan.book.domain.type.ItemStatus;
import com.hyeon.interlibraryloan.library.domain.entity.Library;
import com.hyeon.interlibraryloan.library.domain.repository.LibraryRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소장 자료 엔티티의 행위 메서드. 도서관과 자료 유형을 DB에서 읽어야 해서 컨테이너 위에서 돈다.
 */
@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class ItemTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 25);

	@Autowired
	LibraryRepository libraryRepository;

	@Autowired
	MaterialTypeRepository materialTypeRepository;

	Library library;
	BibRecord bib;

	@BeforeEach
	void setUp() {
		library = libraryRepository.findByCode("MA").orElseThrow();
		bib = BookFixtures.bibRecord("9788985130691");
	}

	private Item item(String materialTypeCode) {
		MaterialType type = materialTypeRepository.findById(materialTypeCode).orElseThrow();
		return BookFixtures.availableItem(library, bib, type, "EMA-TEST-1");
	}

	@Test
	@DisplayName("배정 가능한 자료를 배정하면 예약됨 상태가 된다")
	void reserve() {
		Item item = item("GENERAL");

		item.reserveForInterlibrary(0, TODAY);

		assertThat(item.getStatus()).isEqualTo(ItemStatus.RESERVED);
	}

	@Test
	@DisplayName("BR-01 배정할 수 없는 자료는 이유와 함께 거부되고 상태가 바뀌지 않는다")
	void reserveIneligible() {
		Item item = item("REFERENCE");

		assertThatThrownBy(() -> item.reserveForInterlibrary(0, TODAY))
				.isInstanceOf(ItemNotEligibleException.class)
				.extracting("reasons")
				.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.collection(InterlibraryIneligibleReason.class))
				.containsExactly(InterlibraryIneligibleReason.MATERIAL_TYPE_NOT_ALLOWED);
		assertThat(item.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
	}

	@Test
	@DisplayName("상호대차 불가 자료로 확인되어 배정이 풀리면 대출 가능으로 돌아가고 상호대차 허용이 꺼진다")
	void releaseNonCirculating() {
		Item item = item("GENERAL");
		item.reserveForInterlibrary(0, TODAY);

		item.apply(ItemEvent.RELEASE_NON_CIRCULATING);

		assertThat(item.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
		assertThat(item.isInterlibraryLendable()).isFalse();
		assertThat(item.isInterlibraryEligible(0, TODAY)).isFalse();
	}

	@Test
	@DisplayName("배정되지 않은 자료는 발송할 수 없다")
	void cannotLendOutWithoutReservation() {
		Item item = item("GENERAL");

		assertThatThrownBy(() -> item.apply(ItemEvent.LEND_OUT)).isInstanceOf(ItemTransitionException.class);
		assertThat(item.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
	}

	@Test
	@DisplayName("배정은 조건 확인을 건너뛸 수 없도록 apply로 할 수 없다")
	void reserveOnlyThroughDedicatedMethod() {
		Item item = item("GENERAL");

		assertThatThrownBy(() -> item.apply(ItemEvent.RESERVE)).isInstanceOf(IllegalArgumentException.class);
	}
}
