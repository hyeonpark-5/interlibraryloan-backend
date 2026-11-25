package com.hyeon.interlibraryloan.book.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.interlibraryloan.book.domain.exception.InvalidIsbnException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class IsbnTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"8985130692, 9788985130691",          // 소장도서정보 API 활용가이드 예시
			"0-306-40615-2, 9780306406157",
			"080442957X, 9780804429573",          // 검증 숫자 X
			"080442957x, 9780804429573",          // 소문자 x
	})
	@DisplayName("10자리 ISBN은 13자리로 바꾼다")
	void convertsIsbn10To13(String raw, String expected) {
		assertThat(Isbn.parse(raw).value()).isEqualTo(expected);
	}

	@ParameterizedTest(name = "\"{0}\" -> {1}")
	@CsvSource(delimiter = '|', value = {
			"9780306406157       | 9780306406157",
			"978-0-306-40615-7   | 9780306406157",
			"' 9788985130691 '   | 9788985130691",
			"9791162241462       | 9791162241462",   // 979로 시작하는 국내 ISBN
	})
	@DisplayName("13자리 ISBN은 하이픈과 공백만 지우고 그대로 쓴다")
	void keepsIsbn13(String raw, String expected) {
		assertThat(Isbn.parse(raw).value()).isEqualTo(expected);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@ValueSource(strings = {
			"9780306406158",   // 13자리 검증 숫자 틀림
			"0306406153",      // 10자리 검증 숫자 틀림
			"1234567890128",   // 978, 979로 시작하지 않음
			"08044X957X",      // X가 마지막 자리가 아님
			"12345",           // 자릿수 틀림
			"97803064061570",  // 14자리
			"abcdefghij",      // 숫자 아님
	})
	@DisplayName("형식이나 검증 숫자가 틀리면 예외가 난다")
	void rejectsInvalid(String raw) {
		assertThatThrownBy(() -> Isbn.parse(raw)).isInstanceOf(InvalidIsbnException.class);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	@DisplayName("값이 비어 있으면 예외가 난다")
	void rejectsBlank(String raw) {
		assertThatThrownBy(() -> Isbn.parse(raw)).isInstanceOf(InvalidIsbnException.class);
	}

	@Test
	@DisplayName("tryParse는 잘못된 값이면 비어 있는 결과를 돌려준다")
	void tryParse() {
		assertThat(Isbn.tryParse("8985130692")).map(Isbn::value).contains("9788985130691");
		assertThat(Isbn.tryParse("9780306406158")).isEmpty();
		assertThat(Isbn.tryParse(null)).isEmpty();
	}

	@Test
	@DisplayName("생성자로 직접 만들 때도 13자리 형식과 검증 숫자를 확인한다")
	void constructorValidates() {
		assertThat(new Isbn("9780306406157").value()).isEqualTo("9780306406157");
		assertThatThrownBy(() -> new Isbn("0306406152")).isInstanceOf(InvalidIsbnException.class);
		assertThatThrownBy(() -> new Isbn("9780306406158")).isInstanceOf(InvalidIsbnException.class);
	}
}
