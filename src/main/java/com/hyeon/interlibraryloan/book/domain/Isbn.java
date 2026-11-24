package com.hyeon.interlibraryloan.book.domain;

import java.util.Optional;

/**
 * ISBN-13 값 객체. 도서관끼리 같은 책을 묶는 기준이다.
 * <p>
 * 소장도서정보 API는 10자리, 하이픈이 섞인 값, 빈 값을 섞어서 보낸다.
 * {@link #parse(String)}가 이를 검증하고 13자리 하나로 맞춘다.
 */
public record Isbn(String value) {

	public Isbn {
		if (value == null || !value.matches("97[89]\\d{10}") || !hasValidIsbn13CheckDigit(value)) {
			throw new InvalidIsbnException(value, "13자리 ISBN 형식이 아니거나 검증 숫자가 맞지 않습니다");
		}
	}

	/**
	 * 하이픈과 공백을 지우고, 10자리면 13자리로 바꾼다.
	 *
	 * @throws InvalidIsbnException 형식이나 검증 숫자가 틀린 경우
	 */
	public static Isbn parse(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new InvalidIsbnException(raw, "값이 비어 있습니다");
		}
		String digits = raw.replaceAll("[\\s-]", "").toUpperCase();

		if (digits.length() == 13) {
			return new Isbn(digits);
		}
		if (digits.length() == 10) {
			return new Isbn(convertIsbn10To13(raw, digits));
		}
		throw new InvalidIsbnException(raw, "10자리나 13자리가 아닙니다");
	}

	/** 적재처럼 잘못된 값을 건너뛰어야 할 때 쓴다. */
	public static Optional<Isbn> tryParse(String raw) {
		try {
			return Optional.of(parse(raw));
		} catch (InvalidIsbnException e) {
			return Optional.empty();
		}
	}

	private static String convertIsbn10To13(String raw, String isbn10) {
		if (!isbn10.matches("\\d{9}[\\dX]")) {
			throw new InvalidIsbnException(raw, "10자리 ISBN 형식이 아닙니다");
		}
		if (!hasValidIsbn10CheckDigit(isbn10)) {
			throw new InvalidIsbnException(raw, "10자리 ISBN 검증 숫자가 맞지 않습니다");
		}
		String body = "978" + isbn10.substring(0, 9);
		return body + isbn13CheckDigit(body);
	}

	/** ISBN-10: 앞에서부터 10, 9, ..., 1을 곱한 합이 11의 배수. 마지막 자리 X는 10. */
	private static boolean hasValidIsbn10CheckDigit(String isbn10) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			char c = isbn10.charAt(i);
			int digit = (c == 'X') ? 10 : c - '0';
			sum += digit * (10 - i);
		}
		return sum % 11 == 0;
	}

	private static boolean hasValidIsbn13CheckDigit(String isbn13) {
		return isbn13CheckDigit(isbn13.substring(0, 12)) == isbn13.charAt(12) - '0';
	}

	/** ISBN-13: 앞 12자리에 1, 3을 번갈아 곱한 합으로 마지막 자리를 정한다. */
	private static int isbn13CheckDigit(String first12) {
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			int digit = first12.charAt(i) - '0';
			sum += digit * (i % 2 == 0 ? 1 : 3);
		}
		return (10 - sum % 10) % 10;
	}
}
