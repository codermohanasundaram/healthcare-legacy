-- =====================================================================
-- City General Hospital - Patient Management System
-- Database schema + sample data (MySQL 5.7)
--
-- History:
--   v1.0  2009  initial schema (users, patients, doctors, appointments)
--   v1.4  2011  users.password changed to MD5 (old rows not migrated)
--   v2.0  2014  medical_records, bills
--   v2.1  2016  bills.appointment_id, sp_patient_visit_summary
--
-- NOTE: all patient data below is FICTITIOUS test data.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS healthcare DEFAULT CHARACTER SET latin1;
USE healthcare;

DROP PROCEDURE IF EXISTS sp_patient_visit_summary;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS doctors;

-- ---------------------------------------------------------------------
-- doctors
-- ---------------------------------------------------------------------
CREATE TABLE doctors (
    id                INT          NOT NULL AUTO_INCREMENT,
    first_name        VARCHAR(50)  NOT NULL,
    last_name         VARCHAR(50)  NOT NULL,
    specialization    VARCHAR(100),
    phone             VARCHAR(20),
    email             VARCHAR(100),
    consultation_fee  DOUBLE       DEFAULT 0,
    active            TINYINT(1)   DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- users (application logins)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id            INT           NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)   NOT NULL,
    password      VARCHAR(100)  NOT NULL,     -- MD5 hex (or plain text for pre-v1.4 accounts)
    role          VARCHAR(20)   NOT NULL,     -- ADMIN, DOCTOR, RECEPTIONIST, BILLING
    full_name     VARCHAR(100),
    doctor_id     INT           NULL,         -- link to doctors for DOCTOR role
    active        TINYINT(1)    DEFAULT 1,
    last_login    DATETIME      NULL,
    created_date  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- patients
-- ---------------------------------------------------------------------
CREATE TABLE patients (
    id                       INT           NOT NULL AUTO_INCREMENT,
    mrn                      VARCHAR(20)   NOT NULL,
    first_name               VARCHAR(50)   NOT NULL,
    last_name                VARCHAR(50)   NOT NULL,
    date_of_birth            DATE,
    gender                   CHAR(1),
    phone                    VARCHAR(20),
    email                    VARCHAR(100),
    address                  VARCHAR(200),
    city                     VARCHAR(50),
    state                    VARCHAR(2),
    zip_code                 VARCHAR(10),
    ssn                      VARCHAR(11),
    insurance_provider       VARCHAR(100),
    insurance_number         VARCHAR(50),
    blood_group              VARCHAR(5),
    emergency_contact_name   VARCHAR(100),
    emergency_contact_phone  VARCHAR(20),
    created_date             DATETIME,
    created_by               VARCHAR(50),
    active                   TINYINT(1)    DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_patients_mrn (mrn),
    KEY idx_patients_name (last_name, first_name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- appointments
-- ---------------------------------------------------------------------
CREATE TABLE appointments (
    id                INT           NOT NULL AUTO_INCREMENT,
    patient_id        INT           NOT NULL,
    doctor_id         INT           NOT NULL,
    appointment_date  DATE          NOT NULL,
    appointment_time  VARCHAR(5)    NOT NULL,   -- HH:mm
    reason            VARCHAR(255),
    status            VARCHAR(20)   DEFAULT 'SCHEDULED',
    notes             TEXT,
    created_date      DATETIME,
    PRIMARY KEY (id),
    KEY idx_appt_date (appointment_date),
    KEY idx_appt_doctor (doctor_id, appointment_date),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
    -- no FK to doctors: doctors are hard-deleted from the admin screen
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- medical_records
-- ---------------------------------------------------------------------
CREATE TABLE medical_records (
    id              INT           NOT NULL AUTO_INCREMENT,
    patient_id      INT           NOT NULL,
    doctor_id       INT           NOT NULL,
    appointment_id  INT           NULL,
    visit_date      DATE,
    diagnosis       TEXT,
    prescription    TEXT,
    notes           TEXT,
    blood_pressure  VARCHAR(10),
    temperature     DOUBLE,
    weight          DOUBLE,
    created_date    DATETIME,
    PRIMARY KEY (id),
    KEY idx_mr_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- bills
-- ---------------------------------------------------------------------
CREATE TABLE bills (
    id                INT          NOT NULL AUTO_INCREMENT,
    patient_id        INT          NOT NULL,
    appointment_id    INT          NULL,
    bill_date         DATE,
    consultation_fee  DOUBLE       DEFAULT 0,
    medicine_charges  DOUBLE       DEFAULT 0,
    lab_charges       DOUBLE       DEFAULT 0,
    discount          DOUBLE       DEFAULT 0,
    tax               DOUBLE       DEFAULT 0,
    total_amount      DOUBLE       DEFAULT 0,
    status            VARCHAR(20)  DEFAULT 'PENDING',   -- PENDING, PAID
    payment_method    VARCHAR(20),
    paid_date         DATETIME,
    PRIMARY KEY (id),
    KEY idx_bills_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ---------------------------------------------------------------------
-- Stored procedure used by ReportServlet (type=visits)
-- ---------------------------------------------------------------------
DELIMITER $$
CREATE PROCEDURE sp_patient_visit_summary(IN p_from DATE, IN p_to DATE)
BEGIN
    SELECT p.mrn,
           CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
           COUNT(DISTINCT a.id)                   AS visits,
           MAX(a.appointment_date)                AS last_visit,
           IFNULL(SUM(b.total_amount), 0)         AS total_billed
      FROM patients p
      JOIN appointments a ON a.patient_id = p.id AND a.status = 'COMPLETED'
      LEFT JOIN bills b   ON b.appointment_id = a.id
     WHERE a.appointment_date BETWEEN p_from AND p_to
     GROUP BY p.id, p.mrn, p.first_name, p.last_name
     ORDER BY visits DESC, patient_name;
END$$
DELIMITER ;

-- =====================================================================
-- Sample data
-- =====================================================================

INSERT INTO doctors (id, first_name, last_name, specialization, phone, email, consultation_fee, active) VALUES
(1, 'John',    'Smith',  'General Medicine', '555-0101', 'j.smith@citygeneral.example',  500,  1),
(2, 'Priya',   'Raman',  'Cardiology',       '555-0102', 'p.raman@citygeneral.example',  1200, 1),
(3, 'Ahmed',   'Khan',   'Orthopedics',      '555-0103', 'a.khan@citygeneral.example',   800,  1),
(4, 'Emily',   'Chen',   'Pediatrics',       '555-0104', 'e.chen@citygeneral.example',   600,  1),
(5, 'Carlos',  'Diaz',   'Dermatology',      '555-0105', 'c.diaz@citygeneral.example',   700,  1),
(6, 'Margaret','Hughes', 'Neurology',        '555-0106', 'm.hughes@citygeneral.example', 1500, 0);

-- Demo logins:  admin/admin123  drsmith/doctor123  reception/welcome1  billing/billing123
INSERT INTO users (username, password, role, full_name, doctor_id, active) VALUES
('admin',     MD5('admin123'),  'ADMIN',        'System Administrator', NULL, 1),
('drsmith',   MD5('doctor123'), 'DOCTOR',       'Dr. John Smith',       1,    1),
('drraman',   MD5('doctor123'), 'DOCTOR',       'Dr. Priya Raman',      2,    1),
('reception', MD5('welcome1'),  'RECEPTIONIST', 'Mary Jones',           NULL, 1),
('billing',   'billing123',     'BILLING',      'Robert Brown',         NULL, 1);   -- pre-v1.4 plain text

-- SSNs use the 900-999 area range, which is never issued (fictitious)
INSERT INTO patients (id, mrn, first_name, last_name, date_of_birth, gender, phone, email, address, city, state, zip_code,
                      ssn, insurance_provider, insurance_number, blood_group, emergency_contact_name, emergency_contact_phone,
                      created_date, created_by, active) VALUES
(1,  'MRN201101120001', 'James',    'Wilson',   '1950-03-14', 'M', '555-201-0001', 'jwilson@example.com',   '12 Oak St',       'Springfield', 'IL', '62701', '900-11-0001', 'BlueCross', 'BC-448120',  'O+',  'Mary Wilson',     '555-201-1001', '2011-01-12 09:15:00', 'admin',     1),
(2,  'MRN201203050002', 'Linda',    'Martinez', '1985-07-22', 'F', '555-201-0002', 'lmartinez@example.com', '45 Pine Ave',     'Springfield', 'IL', '62702', '900-11-0002', 'Aetna',     'AE-99231',   'A+',  'Jose Martinez',   '555-201-1002', '2012-03-05 10:20:00', 'reception', 1),
(3,  'MRN201306180003', 'Michael',  'Thompson', '1972-11-05', 'M', '555-201-0003', NULL,                    '8 Elm Rd',        'Springfield', 'IL', '62703', '900-11-0003', NULL,        NULL,         'B+',  'Karen Thompson',  '555-201-1003', '2013-06-18 14:05:00', 'reception', 1),
(4,  'MRN201409220004', 'Sarah',    'Johnson',  '2012-04-18', 'F', '555-201-0004', 'johnson.fam@example.com','77 Maple Dr',    'Chatham',     'IL', '62629', NULL,          'Cigna',     'CG-100457',  'A-',  'Paul Johnson',    '555-201-1004', '2014-09-22 11:30:00', 'reception', 1),
(5,  'MRN201502100005', 'David',    'Lee',      '1945-09-30', 'M', '555-201-0005', NULL,                    '301 Cedar Ln',    'Springfield', 'IL', '62704', '900-11-0005', NULL,        NULL,         'AB+', 'Grace Lee',       '555-201-1005', '2015-02-10 08:45:00', 'admin',     1),
(6,  'MRN201507070006', 'Jennifer', 'Garcia',   '1990-01-09', 'F', '555-201-0006', 'jgarcia@example.com',   '19 Birch Ct',     'Rochester',   'IL', '62563', '900-11-0006', 'UnitedHealth','UH-77812', 'O-',  'Luis Garcia',     '555-201-1006', '2015-07-07 16:10:00', 'reception', 1),
(7,  'MRN201511300007', 'Robert',   'Anderson', '1966-06-17', 'M', '555-201-0007', 'randerson@example.com', '5 Walnut St',     'Springfield', 'IL', '62701', '900-11-0007', 'BlueCross', 'BC-551903',  'A+',  'Susan Anderson',  '555-201-1007', '2015-11-30 13:25:00', 'reception', 1),
(8,  'MRN201603140008', 'Aisha',    'Patel',    '1998-12-02', 'F', '555-201-0008', 'apatel@example.com',    '210 Spruce Way',  'Sherman',     'IL', '62684', '900-11-0008', NULL,        NULL,         'B-',  'Raj Patel',       '555-201-1008', '2016-03-14 09:50:00', 'reception', 1),
(9,  'MRN201604280009', 'William',  'Clark',    '1958-08-25', 'M', '555-201-0009', NULL,                    '66 Hickory Blvd', 'Springfield', 'IL', '62702', '900-11-0009', 'Medicare',  'MC-3321098', 'O+',  'Helen Clark',     '555-201-1009', '2016-04-28 15:40:00', 'admin',     1),
(10, 'MRN201605190010', 'Olivia',   'Brown',    '2001-05-11', 'F', '555-201-0010', 'obrown@example.com',    '3 Willow Pl',     'Springfield', 'IL', '62703', '900-11-0010', 'Aetna',     'AE-10299',   'AB-', 'Nancy Brown',     '555-201-1010', '2016-05-19 10:05:00', 'reception', 1);

INSERT INTO appointments (id, patient_id, doctor_id, appointment_date, appointment_time, reason, status, notes, created_date) VALUES
(1,  1,  2, DATE_SUB(CURDATE(), INTERVAL 30 DAY), '10:00', 'Chest pain follow-up', 'COMPLETED', NULL,                      NOW()),
(2,  2,  1, DATE_SUB(CURDATE(), INTERVAL 20 DAY), '09:30', 'Fever and cough',      'COMPLETED', NULL,                      NOW()),
(3,  3,  3, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '11:00', 'Right knee pain',      'COMPLETED', 'Bring previous X-rays', NOW()),
(4,  5,  1, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  '14:00', 'Blood pressure check', 'COMPLETED', NULL,                      NOW()),
(5,  4,  4, CURDATE(),                            '09:00', 'Vaccination',          'SCHEDULED', NULL,                      NOW()),
(6,  6,  5, CURDATE(),                            '10:30', 'Skin rash',            'SCHEDULED', NULL,                      NOW()),
(7,  7,  2, CURDATE(),                            '15:00', 'ECG review',           'SCHEDULED', NULL,                      NOW()),
(8,  8,  1, DATE_ADD(CURDATE(), INTERVAL 2 DAY),  '11:30', 'Annual physical',      'SCHEDULED', NULL,                      NOW()),
(9,  9,  3, DATE_ADD(CURDATE(), INTERVAL 3 DAY),  '13:00', 'Lower back pain',      'SCHEDULED', NULL,                      NOW()),
(10, 10, 4, DATE_SUB(CURDATE(), INTERVAL 2 DAY),  '16:00', 'Allergy consultation', 'CANCELLED', 'Patient called to cancel', NOW());

INSERT INTO medical_records (patient_id, doctor_id, appointment_id, visit_date, diagnosis, prescription, notes, blood_pressure, temperature, weight, created_date) VALUES
(1, 2, 1, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Stable angina',            'Aspirin 75mg OD; Atorvastatin 20mg HS', 'Stress test ordered. Review in 4 weeks.', '145/90', 98.4, 82.0, NOW()),
(2, 1, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'Acute bronchitis',         'Azithromycin 500mg x 3 days; rest',      'CBC normal.',                           '118/76', 100.9, 61.5, NOW()),
(3, 3, 3, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Osteoarthritis, right knee','Ibuprofen 400mg TDS after food',        'MRI done. Physiotherapy 6 sessions.',   '130/84', 98.6, 91.2, NOW()),
(5, 1, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  'Essential hypertension',   'Amlodipine 5mg OD',                      'Low salt diet advised.',                '160/95', 98.2, 74.0, NOW()),
(1, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 400 DAY), 'Type 2 diabetes mellitus', 'Metformin 500mg BD',               'HbA1c 7.4',                             '138/88', 98.6, 84.5, NOW());

-- Bill 1: patient 1 (age 60+, insured): subtotal 2350, discount 235 + 240 = 475, tax 337.50, total 2212.50
-- Bill 2: patient 2 (insured):          subtotal 850,  discount 60,              tax 142.20, total 932.20
-- Bill 3: patient 3 (self pay):         subtotal 2300, discount 0,               tax 414.00, total 2714.00
-- Bill 4: patient 5 (age 60+) - auto-generated when appointment 4 was completed:
--         consultation only, NO senior discount applied (see AppointmentServlet)
INSERT INTO bills (patient_id, appointment_id, bill_date, consultation_fee, medicine_charges, lab_charges, discount, tax, total_amount, status, payment_method, paid_date) VALUES
(1, 1, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 1200, 350, 800,  475, 337.5, 2212.5, 'PAID',    'CARD', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 500,  150, 200,  60,  142.2, 932.2,  'PAID',    'CASH', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 3, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 800,  0,   1500, 0,   414,   2714,   'PENDING', NULL,   NULL),
(5, 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  500,  0,   0,    0,   90,    590,    'PENDING', NULL,   NULL);

-- Application DB account (docker-compose creates it automatically)
-- CREATE USER 'healthcare_app'@'%' IDENTIFIED BY 'Hc@2015!';
-- GRANT ALL PRIVILEGES ON healthcare.* TO 'healthcare_app'@'%';
