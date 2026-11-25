package com.hyeon.interlibraryloan.book.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소장 자료를 상호대차로 내줄 수 없는 이유 (BR-01 사전 차단 중 자료 부분).
 * 검색 결과에서 "상호대차 불가" 사유를 보여줄 때도 쓴다.
 */
@Getter
@RequiredArgsConstructor
public enum InterlibraryIneligibleReason {

	NOT_AVAILABLE("대출 가능한 상태가 아닙니다"),
	NOT_LENDABLE("상호대차가 허용되지 않은 자료입니다"),
	MATERIAL_TYPE_NOT_ALLOWED("상호대차가 제한된 자료 유형입니다"),
	NEW_BOOK_RESTRICTED("신간이라 아직 상호대차할 수 없습니다");

	private final String description;
}
