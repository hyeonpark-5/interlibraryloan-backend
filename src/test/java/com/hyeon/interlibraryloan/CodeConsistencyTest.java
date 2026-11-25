package com.hyeon.interlibraryloan;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.interlibraryloan.book.domain.type.ItemStatus;
import com.hyeon.interlibraryloan.loanrequest.domain.type.CancelReasonCode;
import com.hyeon.interlibraryloan.member.domain.type.PatronStatus;
import com.hyeon.interlibraryloan.member.domain.type.UserRole;
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

	@Test
	@DisplayName("app_user 테이블의 역할 CHECK 제약과 UserRole enum의 값 목록이 같다")
	void userRoleCheck() {
		assertThat(checkConstraintValues("ck_app_user_role"))
				.isEqualTo(Arrays.stream(UserRole.values()).map(Enum::name).collect(Collectors.toSet()));
	}

	@Test
	@DisplayName("staff 테이블의 역할 CHECK 제약은 UserRole 중 직원 역할과 같다")
	void staffRoleCheck() {
		assertThat(checkConstraintValues("ck_staff_role"))
				.isEqualTo(Arrays.stream(UserRole.values()).filter(UserRole::isStaff).map(Enum::name)
						.collect(Collectors.toSet()));
	}

	@Test
	@DisplayName("patron 테이블의 상태 CHECK 제약과 PatronStatus enum의 값 목록이 같다")
	void patronStatusCheck() {
		assertThat(checkConstraintValues("ck_patron_status"))
				.isEqualTo(Arrays.stream(PatronStatus.values()).map(Enum::name).collect(Collectors.toSet()));
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
