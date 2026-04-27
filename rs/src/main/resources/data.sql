-- ===== Seed Data =====
-- Note: passwords here are plaintext placeholders; on startup a runner re-hashes any non-BCrypt password.

INSERT INTO hospital (id, name, level, parent_id, address, phone) VALUES
 (1, '市第一人民医院（三甲）', '三级', NULL, '人民路 1 号', '010-11110001'),
 (2, '市第二人民医院（三乙）', '三级', NULL, '解放大街 88 号', '010-11110002'),
 (3, '区人民医院（二甲）', '二级', 1, '区府路 12 号', '010-22220001'),
 (4, '社区卫生服务中心', '一级', 3, '阳光小区门口', '010-33330001');

INSERT INTO department (id, hospital_id, name, description) VALUES
 (1, 1, '内科', '常见内科疾病诊疗'),
 (2, 1, '外科', '外科手术与术后管理'),
 (3, 1, '儿科', '儿童疾病诊疗'),
 (4, 2, '心血管科', '心脏病专科'),
 (5, 2, '呼吸科', '呼吸系统疾病'),
 (6, 3, '全科', '区域常见病'),
 (7, 4, '全科', '社区基础诊疗');

INSERT INTO sys_user (id, username, password, real_name, phone, id_card, gender, role, hospital_id, dept_id, title) VALUES
 (1, 'admin',    '123456', '系统管理员', '13800000000', '110101199001011234', '男', 'ADMIN', NULL, NULL, '超级管理员'),
 (2, 'doctor1',  '123456', '张主任',     '13800000001', '110101197505051234', '男', 'DOCTOR', 1, 1, '主任医师'),
 (3, 'doctor2',  '123456', '李医生',     '13800000002', '110101198008081234', '女', 'DOCTOR', 2, 4, '副主任医师'),
 (4, 'doctor3',  '123456', '王医生',     '13800000003', '110101198212121234', '男', 'DOCTOR', 3, 6, '主治医师'),
 (5, 'doctor4',  '123456', '赵医生',     '13800000004', '110101198506061234', '女', 'DOCTOR', 4, 7, '全科医师'),
 (6, 'patient1', '123456', '刘小明',     '13900000001', '110101199501011234', '男', 'PATIENT', NULL, NULL, NULL),
 (7, 'patient2', '123456', '陈小红',     '13900000002', '110101199602022234', '女', 'PATIENT', NULL, NULL, NULL);

-- 排班：未来 7 天，每位医生上午下午各 1 个号源池
INSERT INTO schedule (id, doctor_id, dept_id, hospital_id, work_date, time_slot, total_quota, remaining_quota, status, version) VALUES
 (1, 2, 1, 1, DATEADD('DAY', 1, CURRENT_DATE), 'AM', 20, 20, 'OPEN', 0),
 (2, 2, 1, 1, DATEADD('DAY', 1, CURRENT_DATE), 'PM', 15, 15, 'OPEN', 0),
 (3, 2, 1, 1, DATEADD('DAY', 2, CURRENT_DATE), 'AM', 20, 20, 'OPEN', 0),
 (4, 3, 4, 2, DATEADD('DAY', 1, CURRENT_DATE), 'AM', 15, 15, 'OPEN', 0),
 (5, 3, 4, 2, DATEADD('DAY', 2, CURRENT_DATE), 'AM', 15, 15, 'OPEN', 0),
 (6, 4, 6, 3, DATEADD('DAY', 1, CURRENT_DATE), 'AM', 30, 30, 'OPEN', 0),
 (7, 4, 6, 3, DATEADD('DAY', 1, CURRENT_DATE), 'PM', 30, 30, 'OPEN', 0),
 (8, 5, 7, 4, DATEADD('DAY', 1, CURRENT_DATE), 'AM', 40, 40, 'OPEN', 0),
 (9, 5, 7, 4, DATEADD('DAY', 1, CURRENT_DATE), 'PM', 40, 40, 'OPEN', 0);

-- 一条历史预约 + 病历（让患者有可看的历史就诊记录）
INSERT INTO appointment (id, patient_id, schedule_id, doctor_id, hospital_id, dept_id, status, source) VALUES
 (1, 6, 8, 5, 4, 7, 'VISITED', 'SELF');

INSERT INTO medical_record (id, appointment_id, patient_id, doctor_id, hospital_id, dept_id, chief_complaint, diagnosis, prescription, advice) VALUES
 (1, 1, 6, 5, 4, 7, '咳嗽、低热三天', '上呼吸道感染', '阿莫西林 0.5g tid×5d；布洛芬必要时口服', '多饮水，注意休息，必要时复诊');

INSERT INTO notification (user_id, title, content, type) VALUES
 (6, '欢迎使用双向转诊系统', '您可以在线预约挂号、查看就诊记录与转诊状态。', 'SYSTEM'),
 (7, '欢迎使用双向转诊系统', '您可以在线预约挂号、查看就诊记录与转诊状态。', 'SYSTEM');
