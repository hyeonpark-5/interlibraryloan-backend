package com.hyeon.interlibraryloan.book.domain.exception;

import com.hyeon.interlibraryloan.book.domain.type.ItemEvent;
import com.hyeon.interlibraryloan.book.domain.type.ItemStatus;

public class ItemTransitionException extends IllegalStateException {

	public ItemTransitionException(ItemStatus from, ItemEvent event) {
		super(from + " 상태의 자료에는 " + event + " 할 수 없습니다");
	}
}
