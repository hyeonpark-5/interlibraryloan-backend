package com.hyeon.interlibraryloan.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.common.type.ActorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActorTest {

	@Test
	@DisplayName("이용자 행위자는 계정, 이용자, 소속 도서관을 가진다")
	void patron() {
		Actor actor = Actor.patron(10L, 20L, 1L);

		assertThat(actor.type()).isEqualTo(ActorType.PATRON);
		assertThat(actor.userId()).isEqualTo(10L);
		assertThat(actor.patronId()).isEqualTo(20L);
		assertThat(actor.libraryId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("사서 행위자는 계정과 근무 도서관을 가지고 이용자 id는 없다")
	void librarian() {
		Actor actor = Actor.librarian(10L, 1L);

		assertThat(actor.type()).isEqualTo(ActorType.LIBRARIAN);
		assertThat(actor.patronId()).isNull();
	}

	@Test
	@DisplayName("시스템 행위자는 계정도 도서관도 없다")
	void system() {
		Actor actor = Actor.system();

		assertThat(actor.type()).isEqualTo(ActorType.SYSTEM);
		assertThat(actor.userId()).isNull();
		assertThat(actor.libraryId()).isNull();
		assertThat(actor.patronId()).isNull();
	}

	@Test
	@DisplayName("필요한 값이 빠지면 만들 수 없다")
	void requiredValues() {
		assertThatThrownBy(() -> Actor.patron(10L, null, 1L)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> Actor.patron(10L, 20L, null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> Actor.librarian(10L, null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("유형에 맞지 않는 값이 있으면 만들 수 없다")
	void unexpectedValues() {
		assertThatThrownBy(() -> new Actor(ActorType.LIBRARIAN, 10L, 1L, 20L))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new Actor(ActorType.SYSTEM, 10L, null, null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
