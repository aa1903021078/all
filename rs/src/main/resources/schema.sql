-- ===== Schema =====
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(64),
  phone VARCHAR(32),
  id_card VARCHAR(32),
  gender VARCHAR(8),
  role VARCHAR(16) NOT NULL, -- PATIENT / DOCTOR / ADMIN
  hospital_id BIGINT,
  dept_id BIGINT,
  title VARCHAR(64),
  enabled TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS hospital;
CREATE TABLE hospital (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  level VARCHAR(16) NOT NULL, -- 三级 / 二级 / 一级
  parent_id BIGINT,
  address VARCHAR(255),
  phone VARCHAR(32),
  deleted TINYINT NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS department;
CREATE TABLE department (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hospital_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255),
  deleted TINYINT NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS schedule;
CREATE TABLE schedule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  time_slot VARCHAR(16) NOT NULL, -- AM / PM
  total_quota INT NOT NULL,
  remaining_quota INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN', -- OPEN / CLOSED
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  schedule_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'BOOKED', -- BOOKED / VISITED / CANCELLED
  source VARCHAR(16) DEFAULT 'SELF', -- SELF / REFERRAL
  referral_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS medical_record;
CREATE TABLE medical_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  chief_complaint VARCHAR(500),
  diagnosis VARCHAR(1000),
  prescription VARCHAR(2000),
  advice VARCHAR(1000),
  image_url VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS referral;
CREATE TABLE referral (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  from_hospital_id BIGINT NOT NULL,
  from_dept_id BIGINT,
  from_doctor_id BIGINT NOT NULL,
  to_hospital_id BIGINT NOT NULL,
  to_dept_id BIGINT,
  to_doctor_id BIGINT,
  type VARCHAR(8) NOT NULL, -- UP / DOWN
  summary VARCHAR(2000),
  rehab_plan VARCHAR(2000),
  image_url VARCHAR(500),
  status VARCHAR(16) NOT NULL DEFAULT 'REVIEWING', -- DRAFT / REVIEWING / ACCEPTED / VISITING / COMPLETED / REJECTED
  reject_reason VARCHAR(500),
  target_schedule_id BIGINT,
  target_appointment_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(1000),
  type VARCHAR(32),
  read_flag TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  username VARCHAR(64),
  action VARCHAR(64),
  target VARCHAR(128),
  params VARCHAR(2000),
  success TINYINT,
  error_msg VARCHAR(1000),
  ip VARCHAR(64),
  duration_ms BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
