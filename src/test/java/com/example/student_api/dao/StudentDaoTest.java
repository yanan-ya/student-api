package com.example.student_api.dao;

import com.example.student_api.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import(StudentDao.class)
class StudentDaoTest {
    @Autowired
    private StudentDao dao;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void emptyTableReturnsEmptyList() throws Exception {
        assertThat(dao.findAll()).isEmpty();
    }

    @Test
    void mapsAllColumnsForAllStudents() throws Exception {
        jdbc.update("INSERT INTO student(id, name, email, score) VALUES (?, ?, ?, ?)", 101, "小明", "ming@example.com", 85.5);
        jdbc.update("INSERT INTO student(id, name, email, score) VALUES (?, ?, ?, ?)", 102, "Alice", "alice@example.com", 100);
        assertThat(dao.findAll()).extracting(Student::getId, Student::getName, Student::getEmail, Student::getScore)
                .containsExactlyInAnyOrder(tuple(101, "小明", "ming@example.com", 85.5), tuple(102, "Alice", "alice@example.com", 100.0));
    }

    @Test
    void insertsStudentAndGeneratesId() {
        assertThat(dao.add("小明", "ming@example.com", 85.5)).isEqualTo(1);
        var row = jdbc.queryForMap("SELECT id, name, email, score FROM student WHERE email = ?", "ming@example.com");
        assertThat(((Number) row.get("id")).intValue()).isPositive();
        assertThat(row).containsEntry("name", "小明").containsEntry("email", "ming@example.com").containsEntry("score", 85.5);
    }

    @Test
    void duplicateEmailIsRejectedByDatabaseWithoutChangingOriginal() {
        dao.add("小明", "ming@example.com", 85.5);
        assertThatThrownBy(() -> dao.add("Other", "ming@example.com", 10)).isInstanceOf(DuplicateKeyException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT name FROM student WHERE email = ?", String.class, "ming@example.com")).isEqualTo("小明");
    }

    @Test
    void updatesOnlySelectedStudentsScore() throws Exception {
        dao.add("小明", "ming@example.com", 85.5);
        dao.add("Alice", "alice@example.com", 70);
        assertThat(dao.updateScore("ming@example.com", 92.5)).isEqualTo(1);
        assertThat(dao.findAll()).extracting(Student::getName, Student::getEmail, Student::getScore)
                .containsExactlyInAnyOrder(tuple("小明", "ming@example.com", 92.5), tuple("Alice", "alice@example.com", 70.0));
    }

    @Test
    void updatingSameScoreStillMatchesExistingStudent() {
        dao.add("小明", "ming@example.com", 85.5);
        assertThat(dao.updateScore("ming@example.com", 85.5)).isEqualTo(1);
    }

    @Test
    void deletesOnlySelectedStudent() throws Exception {
        dao.add("小明", "ming@example.com", 85.5);
        dao.add("Alice", "alice@example.com", 70);
        assertThat(dao.deleteByEmail("ming@example.com")).isEqualTo(1);
        assertThat(dao.findAll()).extracting(Student::getEmail).containsExactly("alice@example.com");
    }

    @Test
    void missingStudentsReturnZeroAffectedRows() {
        assertThat(dao.updateScore("missing@example.com", 80)).isZero();
        assertThat(dao.deleteByEmail("missing@example.com")).isZero();
    }

    @Test
    void sqlLikeInputIsTreatedAsData() throws Exception {
        dao.add("O'Brien", "obrien@example.com", 80);
        String suspiciousEmail = "' OR '1'='1";
        assertThat(dao.updateScore(suspiciousEmail, 0)).isZero();
        assertThat(dao.deleteByEmail(suspiciousEmail)).isZero();
        assertThat(dao.findAll()).extracting(Student::getName, Student::getScore).containsExactly(tuple("O'Brien", 80.0));
    }
}
