package com.example.student_api.service;

import com.example.student_api.dao.StudentDao;
import com.example.student_api.entity.Student;
import com.example.student_api.exception.StudentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service   //交给spring管理
public class StudentService {
    private final StudentDao studentDao;

    public StudentService(StudentDao studentDao){
        this.studentDao=studentDao;
    }

    public List<Student> getAllStudent(){
        return studentDao.findAll();
    }

    //加入转发方法,service指挥Dao拿数据;
    public int addStudent(String name,String email,double score){
        return studentDao.add(name,email,score);
    }

    public int updateScore(String email,double newScore){
        int rows = studentDao.updateScore(email,newScore);
        if (rows == 0) {
            throw new StudentNotFoundException();
        }
        return rows;
    }

    public int deleteByEmail(String email){
        int rows = studentDao.deleteByEmail(email);
        if (rows == 0) {
            throw new StudentNotFoundException();
        }
        return rows;
    }
}
