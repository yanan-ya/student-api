CREATE TABLE student (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    score DOUBLE NOT NULL,
    CONSTRAINT uk_student_email UNIQUE (email)
);
