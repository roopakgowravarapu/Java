-- ==========================================================
-- CAMPUS PLACEMENT MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Database: MySQL 8.0+
-- ==========================================================

CREATE DATABASE IF NOT EXISTS campus_placement;
USE campus_placement;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS application_history;
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS placement_drives;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS students;

-- 1. STUDENTS TABLE
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    cgpa DECIMAL(3, 2) NOT NULL,
    graduation_year INT NOT NULL,
    skills VARCHAR(255) NOT NULL,
    backlog_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cgpa CHECK (cgpa >= 0.00 AND cgpa <= 10.00),
    CONSTRAINT chk_backlogs CHECK (backlog_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. USERS TABLE (Authentication & Role Management)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL, -- 'OFFICER', 'STUDENT'
    email VARCHAR(100) NOT NULL UNIQUE,
    student_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. COMPANIES TABLE
CREATE TABLE companies (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    industry VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    website VARCHAR(100),
    hr_contact VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. PLACEMENT DRIVES TABLE
CREATE TABLE placement_drives (
    drive_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    job_role VARCHAR(100) NOT NULL,
    job_description TEXT,
    package_lpa DECIMAL(5, 2) NOT NULL,
    min_cgpa DECIMAL(3, 2) NOT NULL DEFAULT 6.00,
    eligible_departments VARCHAR(255) NOT NULL,
    max_backlogs INT NOT NULL DEFAULT 0,
    required_skills VARCHAR(255) NOT NULL,
    application_deadline DATETIME NOT NULL,
    drive_status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'IN_PROGRESS', 'CLOSED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. APPLICATIONS TABLE
CREATE TABLE applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    drive_id INT NOT NULL,
    student_id INT NOT NULL,
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'Applied', -- 'Applied', 'Under Review', 'Shortlisted', 'Interview Scheduled', 'Selected', 'Rejected'
    interview_date DATETIME NULL,
    remarks TEXT NULL,
    UNIQUE KEY uq_drive_student (drive_id, student_id),
    FOREIGN KEY (drive_id) REFERENCES placement_drives(drive_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. APPLICATION HISTORY TABLE (Audit Trail)
CREATE TABLE application_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    comments VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================================
-- SAMPLE DEMO DATA
-- Password for all demo accounts is their username + '123' (e.g. admin123, officer123, student123)
-- SHA-256 for 'admin123': 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- SHA-256 for 'officer123': cb217036a18d179d67b2ffeb4ab16a24be5be30b805fc4c9ee5a08991fa9a5e0
-- SHA-256 for 'student123': cd6357efdd966de8d0cb2f802c86c44bb64ff37f6543c4174f48e730fc060011
-- SHA-256 for 'priya123': 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
-- ==========================================================

-- Insert Sample Students
INSERT INTO students (student_id, roll_number, full_name, email, phone_number, department, cgpa, graduation_year, skills, backlog_count) VALUES
(1, '24CS101', 'Rahul Sharma', 'rahul.sharma@college.edu', '9876543210', 'Computer Science', 8.75, 2026, 'Java, Python, Spring Boot, MySQL', 0),
(2, '24IT102', 'Priya Patel', 'priya.patel@college.edu', '9876543211', 'Information Tech', 9.10, 2026, 'Java, React, AWS, Docker, Git', 0),
(3, '24EC103', 'Aditya Verma', 'aditya.verma@college.edu', '9876543212', 'Electronics & Comm', 7.40, 2026, 'C++, Embedded Systems, IoT, Python', 1),
(4, '24CS104', 'Ananya Iyer', 'ananya.iyer@college.edu', '9876543213', 'Computer Science', 8.90, 2026, 'Java, Machine Learning, Data Structures', 0),
(5, '24ME105', 'Rohan Kulkarni', 'rohan.kulkarni@college.edu', '9876543214', 'Mechanical Eng', 7.80, 2026, 'AutoCAD, SolidWorks, Python, MATLAB', 0),
(6, '24IT106', 'Sneha Reddy', 'sneha.reddy@college.edu', '9876543215', 'Information Tech', 8.25, 2026, 'Java, Node.js, SQL, HTML/CSS', 0),
(7, '24EE107', 'Vikram Singh', 'vikram.singh@college.edu', '9876543216', 'Electrical Eng', 6.85, 2026, 'Power Systems, MATLAB, C', 2),
(8, '24CS108', 'Meera Nair', 'meera.nair@college.edu', '9876543217', 'Computer Science', 9.45, 2026, 'Java, Kotlin, Android, Cloud, Microservices', 0);

-- Sample Users with real SHA-256 hashes
-- officer / officer123: 118b8d35a17bcf2c7d2d790509e12308dc6332c5d234f0098d2d6be6700bebb1
-- student / student123: 703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b
-- priya / priya123: 0a8b8dfad3f637d7d30fed7b108c5c5986c4775d14cab26ec9279866eba99116
INSERT INTO users (user_id, username, password_hash, role, email, student_id) VALUES
(1, 'officer', '118b8d35a17bcf2c7d2d790509e12308dc6332c5d234f0098d2d6be6700bebb1', 'OFFICER', 'placement.officer@college.edu', NULL),
(2, 'student', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT', 'rahul.sharma@college.edu', 1),
(3, 'priya', '0a8b8dfad3f637d7d30fed7b108c5c5986c4775d14cab26ec9279866eba99116', 'STUDENT', 'priya.patel@college.edu', 2);

-- Insert Sample Companies
INSERT INTO companies (company_id, company_name, industry, location, website, hr_contact, email) VALUES
(1, 'Google', 'Technology', 'Bangalore, India', 'https://careers.google.com', 'Sundar R.', 'campus-in@google.com'),
(2, 'Microsoft', 'Software', 'Hyderabad, India', 'https://careers.microsoft.com', 'Satya N.', 'campus-in@microsoft.com'),
(3, 'Tata Consultancy Services', 'IT Services', 'Mumbai, India', 'https://tcs.com', 'Kavita M.', 'careers@tcs.com'),
(4, 'Infosys Technologies', 'Consulting & IT', 'Bangalore, India', 'https://infosys.com', 'Deepak S.', 'campus@infosys.com'),
(5, 'Amazon Web Services', 'Cloud & E-Commerce', 'Hyderabad, India', 'https://amazon.jobs', 'Pooja G.', 'aws-campus@amazon.com');

-- Insert Sample Placement Drives
-- Dates: 2 future deadlines (approaching in 2-3 days for deadline monitor testing)
INSERT INTO placement_drives (drive_id, company_id, job_role, job_description, package_lpa, min_cgpa, eligible_departments, max_backlogs, required_skills, application_deadline, drive_status) VALUES
(1, 1, 'Associate Software Engineer', 'Develop scalable backend services, APIs and distributed systems in Java/C++.', 24.50, 8.50, 'Computer Science,Information Tech', 0, 'Java, Data Structures, Algorithms', DATE_ADD(NOW(), INTERVAL 2 DAY), 'OPEN'),
(2, 2, 'Software Development Engineer I', 'Cloud architecture, distributed caching, and microservices on Azure.', 21.00, 8.00, 'Computer Science,Information Tech,Electronics & Comm', 0, 'Java, C++, Cloud, Microservices', DATE_ADD(NOW(), INTERVAL 5 DAY), 'OPEN'),
(3, 3, 'Digital Systems Specialist', 'Enterprise software modernization, database optimization and full-stack web.', 9.00, 7.00, 'Computer Science,Information Tech,Electronics & Comm,Mechanical Eng,Electrical Eng', 1, 'Java, Python, SQL', DATE_ADD(NOW(), INTERVAL 1 DAY), 'OPEN'),
(4, 4, 'System Engineer', 'Global client enterprise solutions, data engineering and automation.', 6.50, 6.50, 'Computer Science,Information Tech,Electronics & Comm,Electrical Eng,Mechanical Eng', 1, 'Java, C, SQL', DATE_ADD(NOW(), INTERVAL 10 DAY), 'OPEN'),
(5, 5, 'Cloud Support Associate', 'Cloud diagnostics, Linux system management, networking and infrastructure automation.', 14.00, 7.50, 'Computer Science,Information Tech,Electronics & Comm', 0, 'Linux, Networking, Python, AWS', DATE_SUB(NOW(), INTERVAL 1 DAY), 'CLOSED');

-- Insert Sample Applications (Student 1 'student' has zero initial applications for clean demo)
INSERT INTO applications (application_id, drive_id, student_id, application_date, status, interview_date, remarks) VALUES
(1, 1, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Selected', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Cleared technical & HR rounds with distinction. Offer released: 24.5 LPA'),
(2, 2, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), 'Interview Scheduled', DATE_ADD(NOW(), INTERVAL 2 DAY), 'Scheduled for round 2 virtual interview'),
(3, 3, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Under Review', NULL, 'Verification of core electronic modules'),
(4, 4, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Applied', NULL, 'Standard application submitted'),
(5, 3, 5, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Selected', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Selected for CAD/Automation division. Offer released: 9.0 LPA'),
(6, 4, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Rejected', NULL, 'Does not meet minimum CGPA requirement (6.85 vs 7.00 cutoff)');

-- Insert Sample Application History
INSERT INTO application_history (history_id, application_id, old_status, new_status, updated_by, comments) VALUES
(1, 1, 'Applied', 'Shortlisted', 'officer', 'Round 1 cleared'),
(2, 1, 'Shortlisted', 'Interview Scheduled', 'officer', 'Final panel interview'),
(3, 1, 'Interview Scheduled', 'Selected', 'officer', 'Candidate cleared all rounds with top score'),
(4, 2, 'Applied', 'Interview Scheduled', 'officer', 'Scheduled for virtual round 2 interview'),
(5, 5, 'Applied', 'Selected', 'officer', 'Direct campus placement offer'),
(6, 6, 'Applied', 'Rejected', 'officer', 'Eligibility criteria not met');
