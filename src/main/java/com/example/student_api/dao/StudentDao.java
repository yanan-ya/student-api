package com.example.student_api.dao;
import com.example.student_api.entity.Student;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@Repository
public class StudentDao {
    private final JdbcTemplate jdbcTemplate;

    public StudentDao(JdbcTemplate jdbcTemplate) throws Exception{
        this.jdbcTemplate=jdbcTemplate;
    }

    public List<Student> findAll() throws Exception{
        String sql="SELECT * FROM student";

        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            Student s = new Student();
            s.setId(rs.getInt("id"));
            s.setName(rs.getString("name"));
            s.setEmail(rs.getString("email"));
            s.setScore(rs.getDouble("score"));
            return s;
        });

    }

    //MySQL语句开始忘记了;
    public int add(String name,String email,double score){
        //延续之前的安全策略，直接传值，防止用户注入的情况发生;
        String sql="INSERT INTO student(name,email,score)VALUES(?,?,?)";
        return jdbcTemplate.update(sql,name,email,score);//传值;
    }

    public int updateScore(String email,double newScore){
        String sql="UPDATE student SET score=? WHERE email=?";

        //顺序应该没有影响?
        return jdbcTemplate.update(sql,newScore,email);
    }

    public int deleteByEmail(String email){
        String sql="DELETE FROM student WHERE email=?";
        return jdbcTemplate.update(sql,email);
    }
}