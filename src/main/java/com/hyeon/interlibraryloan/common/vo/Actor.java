package com.hyeon.interlibraryloan.common.vo;

import com.hyeon.interlibraryloan.common.type.ActorType;
import java.util.Objects;

/**
 * 지금 행동하는 사람. 서비스는 이 값을 파라미터로 받는다.
 * <p>
 * Spring Security가 붙기 전에는 테스트가 직접 만들어 넣고, 붙은 뒤에는 로그인 정보로 채운다.
 * 이 요청에서 요청관인지 제공관인지 같은 관계는 이 값과 요청을 보고 서비스가 계산한다.
 *
 * @param userId    계정 id. 시스템이면 null
 * @param libraryId 이용자는 소속 도서관, 사서는 근무 도서관. 시스템이면 null
 * @param patronId  이용자 id. 이용자일 때만 있다
 */
public record Actor(ActorType type, Long userId, Long libraryId, Long patronId) {

	public Actor {
		Objects.requireNonNull(type, "type");
		switch (type) {
			case PATRON -> {
				Objects.requireNonNull(userId, "이용자 행위자는 계정 id가 필요합니다");
				Objects.requireNonNull(libraryId, "이용자 행위자는 소속 도서관이 필요합니다");
				Objects.requireNonNull(patronId, "이용자 행위자는 이용자 id가 필요합니다");
			}
			case LIBRARIAN -> {
				Objects.requireNonNull(userId, "사서 행위자는 계정 id가 필요합니다");
				Objects.requireNonNull(libraryId, "사서 행위자는 근무 도서관이 필요합니다");
				requireNull(patronId, "사서 행위자는 이용자 id를 가질 수 없습니다");
			}
			case SYSTEM -> {
				requireNull(userId, "시스템 행위자는 계정 id를 가질 수 없습니다");
				requireNull(libraryId, "시스템 행위자는 도서관을 가질 수 없습니다");
				requireNull(patronId, "시스템 행위자는 이용자 id를 가질 수 없습니다");
			}
		}
	}

	public static Actor patron(Long userId, Long patronId, Long libraryId) {
		return new Actor(ActorType.PATRON, userId, libraryId, patronId);
	}

	public static Actor librarian(Long userId, Long libraryId) {
		return new Actor(ActorType.LIBRARIAN, userId, libraryId, null);
	}

	public static Actor system() {
		return new Actor(ActorType.SYSTEM, null, null, null);
	}

	private static void requireNull(Object value, String message) {
		if (value != null) {
			throw new IllegalArgumentException(message);
		}
	}
}
