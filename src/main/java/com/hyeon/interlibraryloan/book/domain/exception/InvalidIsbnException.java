package com.hyeon.interlibraryloan.book.domain.exception;

public class InvalidIsbnException extends IllegalArgumentException {

	public InvalidIsbnException(String raw, String reason) {
		super("유효하지 않은 ISBN [" + raw + "]: " + reason);
	}
}
