package com.example.student_api.service;

import com.example.student_api.dao.StudentDao;
import com.example.student_api.entity.Student;
import com.example.student_api.exception.StudentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentDao dao;

    @InjectMocks
    private StudentService service;

    @Test
    void returnsStudentsFromDao() throws Exception {
        var students = List.of(new Student(1, "小明", 80.5, "ming@example.com"));
        when(dao.findAll()).thenReturn(students);
        assertThat(service.getAllStudent()).isEqualTo(students);
    }

    @Test
    void returnsEmptyListWhenNoStudentsExist() throws Exception {
        when(dao.findAll()).thenReturn(List.of());
        assertThat(service.getAllStudent()).isEmpty();
    }

    @Test
    void addsStudentWithCorrectArguments() {
        when(dao.add("小明", "ming@example.com", 80.5)).thenReturn(1);
        assertThat(service.addStudent("小明", "ming@example.com", 80.5)).isEqualTo(1);
        verify(dao).add("小明", "ming@example.com", 80.5);
    }

    @Test
    void duplicateEmailIsNotSwallowed() {
        var failure = new DuplicateKeyException("duplicate email");
        when(dao.add("小明", "ming@example.com", 80)).thenThrow(failure);
        assertThatThrownBy(() -> service.addStudent("小明", "ming@example.com", 80)).isSameAs(failure);
    }

    @Test
    void updatesScoreWithCorrectArguments() {
        when(dao.updateScore("ming@example.com", 90.5)).thenReturn(1);
        assertThat(service.updateScore("ming@example.com", 90.5)).isEqualTo(1);
        verify(dao).updateScore("ming@example.com", 90.5);
    }

    @Test
    void missingStudentCannotBeUpdatedSuccessfully() {
        when(dao.updateScore("missing@example.com", 90)).thenReturn(0);
        assertThatThrownBy(() -> service.updateScore("missing@example.com", 90))
                .isInstanceOf(StudentNotFoundException.class).hasMessage("学生不存在");
    }

    @Test
    void deletesByEmail() {
        when(dao.deleteByEmail("ming@example.com")).thenReturn(1);
        assertThat(service.deleteByEmail("ming@example.com")).isEqualTo(1);
        verify(dao).deleteByEmail("ming@example.com");
    }

    @Test
    void missingStudentCannotBeDeletedSuccessfully() {
        when(dao.deleteByEmail("missing@example.com")).thenReturn(0);
        assertThatThrownBy(() -> service.deleteByEmail("missing@example.com"))
                .isInstanceOf(StudentNotFoundException.class).hasMessage("学生不存在");
    }
}
