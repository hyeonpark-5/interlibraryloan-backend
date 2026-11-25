package com.hyeon.interlibraryloan;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.interlibraryloan.book.domain.type.ItemStatus;
import com.hyeon.interlibraryloan.loanrequest.domain.type.CancelReasonCode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Java enum과 DB에 같은 값 목록이 두 곳에 있는 경우, 둘이 어긋나지 않았는지 확인한다.
 * 한쪽만 바꾸면 앱은 뜨는데 규칙이 조용히 어긋나기 때문이다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CodeConsistencyTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("취소 사유 테이블과 CancelReasonCode enum의 코드와 고를 수 있는 쪽이 같다")
	void cancelReasons() {
		Map<String, String> db = new HashMap<>();
		jdbcTemplate.query("select code, actor_scope from cancel_reason",
				rs -> {
					db.put(rs.getString("code"), rs.getString("actor_scope"));
				});

		Map<String, String> code = Arrays.stream(CancelReasonCode.values())
				.collect(Collectors.toMap(Enum::name, c -> c.getScope().name()));

		assertThat(db).isEqualTo(code);
	}

	@Test
	@DisplayName("item 테이블의 상태 CHECK 제약과 ItemStatus enum의 값 목록이 같다")
	void itemStatusCheck() {
		assertThat(checkConstraintValues("ck_item_status"))
				.isEqualTo(Arrays.stream(ItemStatus.values()).map(Enum::name).collect(Collectors.toSet()));
	}

	/** pg_get_constraintdef 결과에서 'VALUE'::character varying 형태의 값을 뽑는다. */
	private Set<String> checkConstraintValues(String constraintName) {
		String definition = jdbcTemplate.queryForObject(
				"select pg_get_constraintdef(oid) from pg_constraint where conname = ?", String.class, constraintName);
		Matcher matcher = Pattern.compile("'([A-Z_]+)'").matcher(definition);
		Set<String> values = new java.util.HashSet<>();
		while (matcher.find()) {
			values.add(matcher.group(1));
		}
		return values;
	}
}
