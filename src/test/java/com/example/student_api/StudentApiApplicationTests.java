package com.example.student_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentApiApplicationTests {
    @Autowired
    private MockMvc mvc;

	@Test
	void contextLoads() {
	}

    @Test
    void studentCanBeAddedListedUpdatedAndDeletedThroughAllLayers() throws Exception {
        addStudent();
        mvc.perform(get("/student"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].name").value("小明"))
                .andExpect(jsonPath("$.data[0].score").value(85.5));

        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"ming@example.com","score":90.5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000));
        mvc.perform(get("/student"))
                .andExpect(jsonPath("$.data[0].name").value("小明"))
                .andExpect(jsonPath("$.data[0].score").value(90.5));

        // An update to the same score is also successful.
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"ming@example.com","score":90.5}
                """))
                .andExpect(jsonPath("$.code").value(10000));
        mvc.perform(delete("/student").param("email", "ming@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000));
        mvc.perform(get("/student")).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void duplicateEmailIsTranslatedFromDatabaseToApiResponse() throws Exception {
        addStudent();
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Other","email":"ming@example.com","score":10}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10001));
        mvc.perform(get("/student"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("小明"))
                .andExpect(jsonPath("$.data[0].score").value(85.5));
    }

    @Test
    void missingStudentIsReportedForBothUpdateAndDelete() throws Exception {
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"missing@example.com","score":90}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10003));
        mvc.perform(delete("/student").param("email", "missing@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10003));
    }

    @Test
    void invalidUpdateDoesNotChangeStoredScore() throws Exception {
        addStudent();
        mvc.perform(put("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"ming@example.com","score":101}
                """))
                .andExpect(jsonPath("$.code").value(10002));
        mvc.perform(get("/student")).andExpect(jsonPath("$.data[0].score").value(85.5));
    }

    private void addStudent() throws Exception {
        mvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"小明","email":"ming@example.com","score":85.5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10000));
    }

}
