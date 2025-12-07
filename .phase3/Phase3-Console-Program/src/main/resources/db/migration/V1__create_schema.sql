-- ================================================
-- Phase 3 - Database Schema Creation
-- 과팅 매칭 시스템 데이터베이스
-- ================================================

-- ================================================
-- DROP EXISTING TABLES (초기화)
-- ================================================
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE REPORT CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE MESSAGE CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE SESSION_MATCHES CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE CHAT_ROOM CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE MATCHING_QUEUE CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE MATCHING_SESSION CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE STUDENT_GROUP CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE GROUPS CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE STUDENT CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- ================================================
-- DROP SEQUENCES
-- ================================================
BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE student_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE group_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE session_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE queue_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE message_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE report_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- ================================================
-- CREATE SEQUENCES
-- ================================================
CREATE SEQUENCE student_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE group_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE session_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE queue_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE message_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE report_seq START WITH 1 INCREMENT BY 1;

-- ================================================
-- TABLE CREATION
-- ================================================

-- 1. STUDENT Table (Strong Entity)
CREATE TABLE STUDENT (
    student_id NUMBER PRIMARY KEY,
    student_number VARCHAR2(20) NOT NULL UNIQUE,
    age NUMBER NOT NULL CHECK (age >= 18 AND age <= 30),
    gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F')),
    name VARCHAR2(50) NOT NULL,
    mbti CHAR(4) CHECK (REGEXP_LIKE(mbti, '^[IE][NS][TF][JP]$')),
    profile CLOB,
    department VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE INDEX idx_student_gender ON STUDENT(gender);
CREATE INDEX idx_student_created_at ON STUDENT(created_at);
CREATE INDEX idx_student_department ON STUDENT(department);
CREATE INDEX idx_student_mbti ON STUDENT(mbti);

-- 2. GROUPS Table (Strong Entity)
CREATE TABLE GROUPS (
    group_id NUMBER PRIMARY KEY,
    gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F')),
    status VARCHAR2(20) DEFAULT 'AVAILABLE' NOT NULL CHECK (status IN ('AVAILABLE', 'QUEUING', 'MATCHED')),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE INDEX idx_group_gender ON GROUPS(gender);
CREATE INDEX idx_group_status ON GROUPS(status);

-- 3. STUDENT_GROUP Table (Many-to-Many Relationship)
CREATE TABLE STUDENT_GROUP (
    student_id NUMBER NOT NULL,
    group_id NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    PRIMARY KEY (student_id, group_id),
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES GROUPS(group_id) ON DELETE CASCADE
);

CREATE INDEX idx_student_group_student ON STUDENT_GROUP(student_id);
CREATE INDEX idx_student_group_group ON STUDENT_GROUP(group_id);

-- 4. MATCHING_SESSION Table (Strong Entity)
CREATE TABLE MATCHING_SESSION (
    session_id NUMBER PRIMARY KEY,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELED')),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE INDEX idx_matching_session_created_at ON MATCHING_SESSION(created_at);
CREATE INDEX idx_matching_session_status ON MATCHING_SESSION(status);

-- 5. MATCHING_QUEUE Table (Strong Entity)
CREATE TABLE MATCHING_QUEUE (
    queue_id NUMBER PRIMARY KEY,
    group_id NUMBER NOT NULL UNIQUE,
    matching_status VARCHAR2(20) NOT NULL
        CHECK (matching_status IN ('WAITING', 'MATCHED', 'CANCELED')),
    matching_type VARCHAR2(15) NOT NULL
        CHECK (matching_type IN ('ONE_TO_ONE', 'THREE_TO_THREE')),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    FOREIGN KEY (group_id) REFERENCES GROUPS(group_id) ON DELETE CASCADE
);

CREATE INDEX idx_matching_queue_status ON MATCHING_QUEUE(matching_status);

-- 6. CHAT_ROOM Table (Weak Entity)
CREATE TABLE CHAT_ROOM (
    session_id NUMBER PRIMARY KEY,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    FOREIGN KEY (session_id) REFERENCES MATCHING_SESSION(session_id) ON DELETE CASCADE
);

-- 7. SESSION_MATCHES Table (Relationship between Session and Groups via Queue)
CREATE TABLE SESSION_MATCHES (
    session_id NUMBER NOT NULL,
    group_id NUMBER NOT NULL,
    queue_id NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    PRIMARY KEY (session_id, group_id),
    FOREIGN KEY (session_id) REFERENCES MATCHING_SESSION(session_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES GROUPS(group_id) ON DELETE CASCADE,
    FOREIGN KEY (queue_id) REFERENCES MATCHING_QUEUE(queue_id) ON DELETE CASCADE
);

CREATE INDEX idx_session_matches_session ON SESSION_MATCHES(session_id);
CREATE INDEX idx_session_matches_group ON SESSION_MATCHES(group_id);
CREATE INDEX idx_session_matches_queue ON SESSION_MATCHES(queue_id);

-- 8. MESSAGE Table (Weak Entity)
CREATE TABLE MESSAGE (
    session_id NUMBER NOT NULL,
    student_id NUMBER NOT NULL,
    message_id NUMBER NOT NULL,
    content CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    PRIMARY KEY (session_id, student_id, message_id),
    FOREIGN KEY (session_id) REFERENCES CHAT_ROOM(session_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE INDEX idx_message_session ON MESSAGE(session_id);
CREATE INDEX idx_message_student ON MESSAGE(student_id);
CREATE INDEX idx_message_created_at ON MESSAGE(created_at);

-- 9. REPORT Table (Strong Entity)
CREATE TABLE REPORT (
    report_id NUMBER PRIMARY KEY,
    student_id NUMBER NOT NULL,
    title VARCHAR2(200) NOT NULL,
    content CLOB NOT NULL,
    status VARCHAR2(20) NOT NULL
        CHECK (status IN ('PENDING', 'REVIEWING', 'RESOLVED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE INDEX idx_report_student ON REPORT(student_id);
CREATE INDEX idx_report_status ON REPORT(status);

COMMIT;
