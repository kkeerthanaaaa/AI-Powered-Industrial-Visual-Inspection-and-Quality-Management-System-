-- =============================================================================
-- ABC Precision Manufacturing Pvt. Ltd.
-- AI-Powered Industrial Visual Inspection and Quality Management System
-- Database Schema & Comprehensive Seed Data
-- =============================================================================

DROP DATABASE IF EXISTS industrial_inspection;
CREATE DATABASE industrial_inspection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE industrial_inspection;

-- -----------------------------------------------------------------------------
-- 1. Table: users
-- Role-based authentication and user account management
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('ADMINISTRATOR', 'QUALITY_ENGINEER', 'PRODUCTION_SUPERVISOR', 'INSPECTION_OPERATOR') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    failed_attempts INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_status (status)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 2. Table: products
-- Catalog of automotive and aerospace precision manufactured parts
-- -----------------------------------------------------------------------------
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    production_line VARCHAR(60) NOT NULL,
    manufacturing_date DATE NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    supplier VARCHAR(100) NOT NULL,
    customer VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_category (category),
    INDEX idx_product_line (production_line)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 3. Table: machines
-- Manufacturing machinery, press lines, CNC machines, and inspection rigs
-- -----------------------------------------------------------------------------
CREATE TABLE machines (
    machine_id INT AUTO_INCREMENT PRIMARY KEY,
    machine_code VARCHAR(50) NOT NULL UNIQUE,
    machine_name VARCHAR(100) NOT NULL,
    machine_type VARCHAR(60) NOT NULL,
    production_line VARCHAR(60) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    installation_date DATE NOT NULL,
    last_maintenance DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_machine_status (status),
    INDEX idx_machine_line (production_line)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 4. Table: operators
-- Floor staff and certified quality operators assigned to shifts
-- -----------------------------------------------------------------------------
CREATE TABLE operators (
    operator_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(80) NOT NULL,
    shift ENUM('MORNING', 'EVENING', 'NIGHT') NOT NULL,
    contact VARCHAR(30) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'ON_LEAVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator_shift (shift),
    INDEX idx_operator_status (status)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 5. Table: production_batches
-- Production run grouping for quality control and lot tracking
-- -----------------------------------------------------------------------------
CREATE TABLE production_batches (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    batch_number VARCHAR(60) NOT NULL UNIQUE,
    product_id INT NOT NULL,
    batch_size INT NOT NULL CHECK (batch_size > 0),
    production_date DATE NOT NULL,
    shift ENUM('MORNING', 'EVENING', 'NIGHT') NOT NULL,
    machine_id INT NOT NULL,
    operator_id INT NOT NULL,
    status ENUM('PENDING', 'IN_INSPECTION', 'APPROVED', 'REJECTED', 'QUARANTINED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_batch_machine FOREIGN KEY (machine_id) REFERENCES machines(machine_id) ON DELETE RESTRICT,
    CONSTRAINT fk_batch_operator FOREIGN KEY (operator_id) REFERENCES operators(operator_id) ON DELETE RESTRICT,
    INDEX idx_batch_status (status),
    INDEX idx_batch_date (production_date)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 6. Table: product_images
-- Optical inspection capture files and camera metadata
-- -----------------------------------------------------------------------------
CREATE TABLE product_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    batch_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    capture_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    camera_id VARCHAR(50) NOT NULL DEFAULT 'CAM-OPTICAL-01',
    width INT NOT NULL,
    height INT NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    inspection_status ENUM('UNINSPECTED', 'INSPECTED', 'REJECTED') DEFAULT 'UNINSPECTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_image_batch FOREIGN KEY (batch_id) REFERENCES production_batches(batch_id) ON DELETE CASCADE,
    INDEX idx_image_batch (batch_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 7. Table: defect_types
-- Master classification taxonomy of visual industrial defects
-- -----------------------------------------------------------------------------
CREATE TABLE defect_types (
    defect_type_id INT AUTO_INCREMENT PRIMARY KEY,
    defect_code VARCHAR(40) NOT NULL UNIQUE,
    defect_name VARCHAR(100) NOT NULL,
    category VARCHAR(60) NOT NULL,
    default_severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    description TEXT,
    standard_remedy TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_defect_code (defect_code)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 8. Table: ai_predictions
-- Outputs from the Visual Inspection AI Simulation Module
-- -----------------------------------------------------------------------------
CREATE TABLE ai_predictions (
    prediction_id INT AUTO_INCREMENT PRIMARY KEY,
    image_id INT NOT NULL,
    predicted_label VARCHAR(80) NOT NULL,
    confidence_score DECIMAL(5,2) NOT NULL CHECK (confidence_score >= 0.0 AND confidence_score <= 100.0),
    defect_type VARCHAR(80) NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    defect_location ENUM('TOP_LEFT', 'TOP_RIGHT', 'CENTER', 'BOTTOM_LEFT', 'BOTTOM_RIGHT', 'PERIMETER', 'ENTIRE_SURFACE') DEFAULT 'CENTER',
    bounding_box_x INT DEFAULT 0,
    bounding_box_y INT DEFAULT 0,
    bounding_box_w INT DEFAULT 0,
    bounding_box_h INT DEFAULT 0,
    processing_time_ms INT NOT NULL DEFAULT 45,
    raw_response_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pred_image FOREIGN KEY (image_id) REFERENCES product_images(image_id) ON DELETE CASCADE,
    INDEX idx_pred_label (predicted_label)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 9. Table: inspection_records
-- Quality Engineer evaluations, reviews, and definitive audit decisions
-- -----------------------------------------------------------------------------
CREATE TABLE inspection_records (
    inspection_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    batch_id INT NOT NULL,
    image_id INT NOT NULL,
    inspector_id INT NOT NULL,
    prediction_id INT,
    inspection_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    final_decision ENUM('PASS', 'FAIL', 'REQUIRES_REVIEW') NOT NULL,
    remarks TEXT,
    status ENUM('SUBMITTED', 'VERIFIED', 'AUDITED') DEFAULT 'SUBMITTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_insp_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_insp_batch FOREIGN KEY (batch_id) REFERENCES production_batches(batch_id) ON DELETE RESTRICT,
    CONSTRAINT fk_insp_image FOREIGN KEY (image_id) REFERENCES product_images(image_id) ON DELETE RESTRICT,
    CONSTRAINT fk_insp_user FOREIGN KEY (inspector_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_insp_pred FOREIGN KEY (prediction_id) REFERENCES ai_predictions(prediction_id) ON DELETE SET NULL,
    INDEX idx_insp_decision (final_decision),
    INDEX idx_insp_date (inspection_date)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 10. Table: defect_results
-- Specific defects confirmed or classified during an inspection
-- -----------------------------------------------------------------------------
CREATE TABLE defect_results (
    defect_result_id INT AUTO_INCREMENT PRIMARY KEY,
    inspection_id INT NOT NULL,
    defect_type_id INT NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    location ENUM('TOP_LEFT', 'TOP_RIGHT', 'CENTER', 'BOTTOM_LEFT', 'BOTTOM_RIGHT', 'PERIMETER', 'ENTIRE_SURFACE') NOT NULL,
    defect_x INT DEFAULT 0,
    defect_y INT DEFAULT 0,
    defect_w INT DEFAULT 0,
    defect_h INT DEFAULT 0,
    comments VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dr_inspection FOREIGN KEY (inspection_id) REFERENCES inspection_records(inspection_id) ON DELETE CASCADE,
    CONSTRAINT fk_dr_defecttype FOREIGN KEY (defect_type_id) REFERENCES defect_types(defect_type_id) ON DELETE RESTRICT,
    INDEX idx_dr_severity (severity)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 11. Table: corrective_actions
-- CAPA (Corrective and Preventive Action) logged for non-conformances
-- -----------------------------------------------------------------------------
CREATE TABLE corrective_actions (
    action_id INT AUTO_INCREMENT PRIMARY KEY,
    inspection_id INT NOT NULL,
    defect_result_id INT,
    root_cause TEXT NOT NULL,
    corrective_action TEXT NOT NULL,
    responsible_person VARCHAR(100) NOT NULL,
    target_date DATE NOT NULL,
    completion_date DATE,
    status ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED', 'VERIFIED') DEFAULT 'OPEN',
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ca_inspection FOREIGN KEY (inspection_id) REFERENCES inspection_records(inspection_id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_defectresult FOREIGN KEY (defect_result_id) REFERENCES defect_results(defect_result_id) ON DELETE SET NULL,
    INDEX idx_ca_status (status)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 12. Table: quality_reports
-- Generated PDF/CSV/TXT inspection summaries and metrics
-- -----------------------------------------------------------------------------
CREATE TABLE quality_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    report_type ENUM('DAILY_INSPECTION', 'BATCH_QUALITY', 'PRODUCT_DEFECT', 'OPERATOR_PERFORMANCE', 'MACHINE_DEFECT', 'MONTHLY_SUMMARY', 'PRODUCTION_TREND') NOT NULL,
    title VARCHAR(150) NOT NULL,
    generated_by INT NOT NULL,
    parameters_json TEXT,
    file_path VARCHAR(500) NOT NULL,
    file_format ENUM('CSV', 'TXT', 'HTML', 'PDF') NOT NULL DEFAULT 'CSV',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_qr_user FOREIGN KEY (generated_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_qr_type (report_type)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 13. Table: audit_log
-- Immutable security and activity event log
-- -----------------------------------------------------------------------------
CREATE TABLE audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(80) NOT NULL,
    entity_name VARCHAR(80) NOT NULL,
    entity_id INT,
    description TEXT,
    ip_address VARCHAR(50) DEFAULT '127.0.0.1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 14. Table: login_log
-- Authentication history and brute-force tracking
-- -----------------------------------------------------------------------------
CREATE TABLE login_log (
    login_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username_attempted VARCHAR(60) NOT NULL,
    status ENUM('SUCCESS', 'FAILED') NOT NULL,
    failure_reason VARCHAR(100),
    ip_address VARCHAR(50) DEFAULT '127.0.0.1',
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loginlog_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_login_status (status)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 15. Table: system_settings
-- Global configuration parameters for inspection tolerances and cameras
-- -----------------------------------------------------------------------------
CREATE TABLE system_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(80) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    setting_group VARCHAR(60) NOT NULL DEFAULT 'GENERAL',
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_settings_key (setting_key)
) ENGINE=InnoDB;


-- =============================================================================
-- SEED DATA INSERTION
-- Default Users (Password: "Admin@123" for admin, "Pass@123" for others)
-- Uses PBKDF2WithHmacSHA256 representation or system hashes
-- =============================================================================

-- Salt: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
-- PBKDF2WithHmacSHA256 Hash for "Admin@123": "ca31ea8f7778c8817c6317583975902abb92015a5baae9d55d824fee7d35f903"
-- PBKDF2WithHmacSHA256 Hash for "Pass@123":  "ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1"

INSERT INTO users (username, password_hash, salt, full_name, email, role, status) VALUES
('admin', 'ca31ea8f7778c8817c6317583975902abb92015a5baae9d55d824fee7d35f903', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'Rajesh Sharma (System Admin)', 'admin@abcprecision.com', 'ADMINISTRATOR', 'ACTIVE'),
('engineer', 'ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'Priya Patel (Lead Quality Engineer)', 'priya.patel@abcprecision.com', 'QUALITY_ENGINEER', 'ACTIVE'),
('supervisor', 'ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'Vikram Singh (Shift Supervisor)', 'vikram.singh@abcprecision.com', 'PRODUCTION_SUPERVISOR', 'ACTIVE'),
('operator', 'ad4d45fc1b328a5bd49faf97cd32055ac0c79668940b710743ba12886f60cff1', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'Kavita Roy (Optical Inspector)', 'kavita.roy@abcprecision.com', 'INSPECTION_OPERATOR', 'ACTIVE');

-- -----------------------------------------------------------------------------
-- Seed Machines
-- -----------------------------------------------------------------------------
INSERT INTO machines (machine_code, machine_name, machine_type, production_line, status, installation_date, last_maintenance) VALUES
('CNC-001', 'DMG Mori 5-Axis Milling Rig', 'CNC Milling', 'Aero-Line-1', 'ACTIVE', '2023-01-15', '2026-09-10'),
('CNC-002', 'Mazak Integrex Multi-Tasking Center', 'CNC Lathe & Mill', 'Auto-Line-2', 'ACTIVE', '2023-04-20', '2026-09-15'),
('PRESS-001', 'Schuler 400T Hydraulic Stamping Press', 'Stamping Press', 'Chassis-Line-1', 'ACTIVE', '2022-11-05', '2026-08-25'),
('ASSEMBLY-001', 'KUKA Robotic Precision Cell', 'Robotic Assembly', 'Assembly-Line-3', 'MAINTENANCE', '2024-02-12', '2026-10-01');

-- -----------------------------------------------------------------------------
-- Seed Operators
-- -----------------------------------------------------------------------------
INSERT INTO operators (name, employee_code, department, shift, contact, status) VALUES
('Kavita Roy', 'EMP-OP-101', 'Quality Assurance', 'MORNING', '+91 98765 43210', 'ACTIVE'),
('Anand Verma', 'EMP-OP-102', 'Precision Machining', 'EVENING', '+91 98765 43211', 'ACTIVE'),
('Deepak Kumar', 'EMP-OP-103', 'Aerospace Assembly', 'NIGHT', '+91 98765 43212', 'ACTIVE'),
('Sunita Joshi', 'EMP-OP-104', 'Stamping & Forging', 'MORNING', '+91 98765 43213', 'ACTIVE');

-- -----------------------------------------------------------------------------
-- Seed Defect Types
-- -----------------------------------------------------------------------------
INSERT INTO defect_types (defect_code, defect_name, category, default_severity, description, standard_remedy) VALUES
('DEF-SCRATCH', 'Surface Scratch', 'Surface Flaw', 'MEDIUM', 'Linear abrasive damage penetrating the surface coating or polished face', 'Micro-buffing or recoat pass'),
('DEF-CRACK', 'Structural Micro-Crack', 'Structural Defect', 'CRITICAL', 'Stress or fatigue fracture propagating across alloy body', 'Immediate scrap and tool recalibration'),
('DEF-DENT', 'Mechanical Dent', 'Form Deformation', 'HIGH', 'Depression caused by impact handling or clamping over-torque', 'Dimensional re-measurement or rejection'),
('DEF-DISCOLOR', 'Thermal Discoloration', 'Surface Finish', 'LOW', 'Oxidation discoloration due to excessive heat during machining cut', 'Coolant flow adjustment and chemical rinse'),
('DEF-SURF-DMG', 'Surface Damage & Pitting', 'Surface Flaw', 'HIGH', 'Localized casting porosity, pitting corrosion, or tool chatter scarring', 'Material review and tooling replacement'),
('DEF-MISS-COMP', 'Missing Component / Dowel', 'Assembly Error', 'CRITICAL', 'Absence of secondary fastener, sealing ring, or locator dowel pin', 'Return to assembly station for component insertion'),
('DEF-CORROSION', 'Chemical Corrosion / Rust', 'Material Degradation', 'HIGH', 'Oxidative corrosion visible on ground steel mating face', 'Rust-inhibitor immersion and sand-blast refacing'),
('DEF-MISALIGN', 'Feature Misalignment', 'Dimensional Tolerance', 'MEDIUM', 'Angular or concentric deviation exceeding GD&T boundary', 'CNC offset recalibration');

-- -----------------------------------------------------------------------------
-- Seed Products
-- -----------------------------------------------------------------------------
INSERT INTO products (product_name, category, production_line, manufacturing_date, batch_number, supplier, customer, description) VALUES
('Brake Disc Ventilated 320mm', 'Automotive Braking', 'Auto-Line-2', '2026-09-28', 'BATCH-2026-BRK-01', 'Tata Steel Ltd', 'Mahindra Automotive', 'High-carbon cast iron ventilated rotor for premium utility vehicles'),
('Engine Mount Hydraulic Assembly', 'Automotive Powertrain', 'Auto-Line-2', '2026-09-30', 'BATCH-2026-ENG-02', 'Kalyani Forgings', 'Tata Motors Ltd', 'Hydraulic fluid filled damping bracket for high-torque diesel platforms'),
('Aircraft Titanium Wing Bracket', 'Aerospace Structural', 'Aero-Line-1', '2026-10-01', 'BATCH-2026-AERO-03', 'Mishra Dhatu Nigam', 'HAL Aerospace', 'Ti-6Al-4V grade critical wing spar joint bracket with tight tolerance'),
('Transmission Housing 8-Speed', 'Automotive Transmission', 'Chassis-Line-1', '2026-10-02', 'BATCH-2026-TRN-04', 'Hindalco Industries', 'Maruti Suzuki India', 'Die-cast aluminum casing for 8-speed automatic torque-converter gearbox'),
('Planetary Gear Assembly Sun Pinion', 'Aerospace Transmission', 'Aero-Line-1', '2026-10-03', 'BATCH-2026-GEAR-05', 'Steel Authority of India', 'Bharat Dynamics Ltd', 'Carburized case-hardened alloy gear for satellite solar deployment drives');

-- -----------------------------------------------------------------------------
-- Seed Production Batches
-- -----------------------------------------------------------------------------
INSERT INTO production_batches (batch_number, product_id, batch_size, production_date, shift, machine_id, operator_id, status) VALUES
('BATCH-2026-BRK-01', 1, 500, '2026-09-28', 'MORNING', 2, 1, 'APPROVED'),
('BATCH-2026-ENG-02', 2, 250, '2026-09-30', 'EVENING', 2, 2, 'IN_INSPECTION'),
('BATCH-2026-AERO-03', 3, 50,  '2026-10-01', 'MORNING', 1, 1, 'IN_INSPECTION'),
('BATCH-2026-TRN-04', 4, 180, '2026-10-02', 'NIGHT',   3, 3, 'PENDING'),
('BATCH-2026-GEAR-05', 5, 80,  '2026-10-03', 'MORNING', 1, 4, 'PENDING');

-- -----------------------------------------------------------------------------
-- Seed Product Images
-- -----------------------------------------------------------------------------
INSERT INTO product_images (product_id, batch_id, file_name, file_path, capture_date, camera_id, width, height, file_size_bytes, inspection_status) VALUES
(1, 1, 'sample_brake_disc_inspected.png', 'uploads/inspections/sample_brake_disc_inspected.png', '2026-09-29 10:15:00', 'CAM-OPTICAL-01', 1920, 1080, 2458900, 'INSPECTED'),
(3, 3, 'sample_aero_bracket_crack.png',   'uploads/inspections/sample_aero_bracket_crack.png',   '2026-10-01 14:22:10', 'CAM-OPTICAL-02', 2048, 1536, 3125400, 'INSPECTED'),
(2, 2, 'sample_engine_mount_good.png',    'uploads/inspections/sample_engine_mount_good.png',    '2026-10-02 09:40:00', 'CAM-OPTICAL-01', 1920, 1080, 2198000, 'INSPECTED');

-- -----------------------------------------------------------------------------
-- Seed AI Predictions
-- -----------------------------------------------------------------------------
INSERT INTO ai_predictions (image_id, predicted_label, confidence_score, defect_type, severity, defect_location, bounding_box_x, bounding_box_y, bounding_box_w, bounding_box_h, processing_time_ms, raw_response_json) VALUES
(1, 'DEFECT_FREE', 98.45, 'DEFECT_FREE', 'LOW', 'CENTER', 0, 0, 0, 0, 42, '{"prediction":"DEFECT_FREE","confidence":98.45,"model":"VisualInspection-Sim-v2.1"}'),
(2, 'CRACK', 94.60, 'DEF-CRACK', 'CRITICAL', 'TOP_RIGHT', 1350, 280, 320, 190, 58, '{"prediction":"CRACK","confidence":94.60,"severity":"CRITICAL","coords":{"x":1350,"y":280,"w":320,"h":190}}'),
(3, 'DEFECT_FREE', 97.10, 'DEFECT_FREE', 'LOW', 'CENTER', 0, 0, 0, 0, 39, '{"prediction":"DEFECT_FREE","confidence":97.10,"model":"VisualInspection-Sim-v2.1"}');

-- -----------------------------------------------------------------------------
-- Seed Inspection Records
-- -----------------------------------------------------------------------------
INSERT INTO inspection_records (product_id, batch_id, image_id, inspector_id, prediction_id, inspection_date, final_decision, remarks, status) VALUES
(1, 1, 1, 2, 1, '2026-09-29 10:30:00', 'PASS', 'Finished rotor surface meets ISO 9001 specs. Balanced and scratch-free.', 'AUDITED'),
(3, 3, 2, 2, 2, '2026-10-01 14:45:00', 'FAIL', 'AI flagged 94.6% confidence structural micro-crack on upper spar mount. Scrap confirmed.', 'AUDITED'),
(2, 2, 3, 4, 3, '2026-10-02 10:00:00', 'PASS', 'Damping rubber bonding intact, bolt channels clean.', 'VERIFIED');

-- -----------------------------------------------------------------------------
-- Seed Defect Results
-- -----------------------------------------------------------------------------
INSERT INTO defect_results (inspection_id, defect_type_id, severity, location, defect_x, defect_y, defect_w, defect_h, comments) VALUES
(2, 2, 'CRITICAL', 'TOP_RIGHT', 1350, 280, 320, 190, 'Crack exceeds allowable 0.05mm ultrasonic limit');

-- -----------------------------------------------------------------------------
-- Seed Corrective Actions
-- -----------------------------------------------------------------------------
INSERT INTO corrective_actions (inspection_id, defect_result_id, root_cause, corrective_action, responsible_person, target_date, completion_date, status, remarks) VALUES
(2, 1, 'Excessive cutting speed and worn carbide insert on CNC-001 causing micro-fracture during titanium hogging cut', 'Replaced milling tool insert with Walter Ti-Grade, recalibrated spindle feed rate to 45m/min, and issued 100% dye-penetrant inspection for entire batch', 'Priya Patel (Lead QE)', '2026-10-05', '2026-10-04', 'VERIFIED', 'Tooling replaced and verified via test cut.');

-- -----------------------------------------------------------------------------
-- Seed Audit Log
-- -----------------------------------------------------------------------------
INSERT INTO audit_log (user_id, action, entity_name, entity_id, description) VALUES
(1, 'USER_LOGIN', 'users', 1, 'Admin logged in from local workstation'),
(1, 'SYSTEM_INITIALIZED', 'system', 0, 'Database and initial application configuration verified'),
(2, 'INSPECTION_CREATED', 'inspection_records', 1, 'Inspection completed for Brake Disc BATCH-2026-BRK-01 (PASS)'),
(2, 'INSPECTION_CREATED', 'inspection_records', 2, 'Inspection completed for Aircraft Titanium Bracket BATCH-2026-AERO-03 (FAIL)'),
(2, 'CORRECTIVE_ACTION_CREATED', 'corrective_actions', 1, 'Corrective action issued for titanium bracket crack');

-- -----------------------------------------------------------------------------
-- Seed Login Log
-- -----------------------------------------------------------------------------
INSERT INTO login_log (user_id, username_attempted, status, failure_reason) VALUES
(1, 'admin', 'SUCCESS', NULL),
(2, 'engineer', 'SUCCESS', NULL),
(NULL, 'hacker', 'FAILED', 'Invalid username or credentials');

-- -----------------------------------------------------------------------------
-- Seed System Settings
-- -----------------------------------------------------------------------------
INSERT INTO system_settings (setting_key, setting_value, setting_group, description) VALUES
('COMPANY_NAME', 'ABC Precision Manufacturing Pvt. Ltd.', 'GENERAL', 'Company registered name'),
('COMPANY_FACILITY', 'Plant 3, Aerospace & Precision Machining Hub, Pune, India', 'GENERAL', 'Plant facility description'),
('AI_CONFIDENCE_THRESHOLD', '85.0', 'INSPECTION', 'Minimum AI confidence percentage to recommend automatic pass'),
('MAX_IMAGE_UPLOAD_MB', '25', 'INSPECTION', 'Maximum inspection file size limit in megabytes'),
('SUPPORTED_EXTENSIONS', 'jpg,jpeg,png,bmp', 'INSPECTION', 'Permitted optical inspection image formats'),
('CAMERA_PRIMARY_ID', 'CAM-OPTICAL-01', 'HARDWARE', 'Default camera mount identifier in line inspection cell'),
('AUTO_AUDIT_LOGGING', 'true', 'SECURITY', 'Flag to toggle background multithreaded audit logging'),
('DEFAULT_BACKUP_PATH', 'C:/Users/keert/.gemini/antigravity-ide/scratch/IndustrialInspectionSystem/backups', 'DATABASE', 'Default folder for SQL database dumps');
