package com.example.student_api.controller;

import com.example.student_api.entity.Student;
import com.example.student_api.exception.StudentNotFoundException;
import com.example.student_api.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StudentService service;

    @Test
    void listsStudentsWithExistingResponseFormat() throws Exception {
        when(service.getAllStudent()).thenReturn(List.of(new Student(1, "小明", 85.5, "ming@example.com")));

        mvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("小明"))
                .andExpect(jsonPath("$.data[0].email").value("ming@example.com"))
                .andExpect(jsonPath("$.data[0].score").value(85.5));
    }

    @Test
    void emptyDatabaseReturnsEmptyArray() throws Exception {
        when(service.getAllStudent()).thenReturn(List.of());
        mvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void addsStudent() throws Exception {
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"小明","email":"ming@example.com","score":85.5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000))
                .andExpect(content().json("""
                        {"code":10000,"message":"成功","data":null}
                        """));
        verify(service).addStudent("小明", "ming@example.com", 85.5);
    }

    @Test
    void duplicateEmailKeepsExistingBusinessCode() throws Exception {
        when(service.addStudent("小明", "ming@example.com", 85.5))
                .thenThrow(new DuplicateKeyException("database detail"));
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"小明","email":"ming@example.com","score":85.5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("邮箱已存在"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\" \",\"email\":\"ming@example.com\",\"score\":80}",
            "{\"name\":\"小明\",\"email\":\"\",\"score\":80}",
            "{\"name\":\"小明\",\"email\":\"invalid\",\"score\":80}",
            "{\"name\":\"小明\",\"email\":\"ming@example.com\",\"score\":-0.5}",
            "{\"name\":\"小明\",\"email\":\"ming@example.com\",\"score\":100.5}"
    })
    void invalidAddDoesNotReachService(String body) throws Exception {
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 100})
    void acceptsScoreBoundaries(double score) throws Exception {
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"小明","email":"ming@example.com","score":%s}
                """.formatted(score)))
                .andExpect(jsonPath("$.code").value(10000));
        verify(service).addStudent("小明", "ming@example.com", score);
    }

    @Test
    void updatesScoreWithoutRequiringName() throws Exception {
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"ming@example.com","score":90.5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000));
        verify(service).updateScore("ming@example.com", 90.5);
    }

    @Test
    void updateStillAcceptsOriginalStudentBody() throws Exception {
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"id":1,"name":"小明","email":"ming@example.com","score":90.5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000));
        verify(service).updateScore("ming@example.com", 90.5);
    }

    @Test
    void updatingMissingStudentReturnsBusinessError() throws Exception {
        when(service.updateScore("missing@example.com", 90)).thenThrow(new StudentNotFoundException());
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"missing@example.com","score":90}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10003))
                .andExpect(jsonPath("$.message").value("学生不存在"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"score\":80}",
            "{\"email\":\" \",\"score\":80}",
            "{\"email\":\"invalid\",\"score\":80}",
            "{\"email\":\"ming@example.com\",\"score\":-0.5}",
            "{\"email\":\"ming@example.com\",\"score\":100.5}"
    })
    void invalidUpdateDoesNotReachService(String body) throws Exception {
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
        verifyNoInteractions(service);
    }

    @Test
    void deletesByEmail() throws Exception {
        mvc.perform(delete("/student").param("email", "ming@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000));
        verify(service).deleteByEmail("ming@example.com");
    }

    @Test
    void deletingMissingStudentReturnsBusinessError() throws Exception {
        when(service.deleteByEmail("missing@example.com")).thenThrow(new StudentNotFoundException());
        mvc.perform(delete("/student").param("email", "missing@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10003))
                .andExpect(jsonPath("$.message").value("学生不存在"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid"})
    void invalidDeleteEmailDoesNotReachService(String email) throws Exception {
        mvc.perform(delete("/student").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
        verifyNoInteractions(service);
    }

    @Test
    void missingDeleteEmailIsRequestError() throws Exception {
        mvc.perform(delete("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"score\":\"abc\"}", ""})
    void unreadableJsonIsRequestError(String body) throws Exception {
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
        verifyNoInteractions(service);
    }

    @Test
    void unexpectedFailureDoesNotExposeDatabaseDetails() throws Exception {
        when(service.getAllStudent()).thenThrow(new DataAccessResourceFailureException("private database details"));
        mvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"code":99999,"message":"系统繁忙,请稍后再试","data":null}
                        """));
    }
}
