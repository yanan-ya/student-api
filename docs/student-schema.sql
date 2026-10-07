-- Reference schema for a NEW local MySQL database, not an automatic migration.
-- Select your database before executing. Do not run against an existing table.
-- Column lengths here are example values, not inferred from your local database.
CREATE TABLE student (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    score DOUBLE NOT NULL,
    CONSTRAINT uk_student_email UNIQUE (email)
);
