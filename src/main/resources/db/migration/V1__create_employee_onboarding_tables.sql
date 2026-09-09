CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_code VARCHAR(30) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NULL,
    department VARCHAR(100) NOT NULL,
    job_title VARCHAR(120) NOT NULL,
    manager_name VARCHAR(160) NULL,
    joining_date DATE NOT NULL,
    employment_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_employees_employee_code UNIQUE (employee_code),
    CONSTRAINT uk_employees_email UNIQUE (email),
    INDEX idx_employees_status (status),
    INDEX idx_employees_joining_date (joining_date),
    INDEX idx_employees_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE onboarding_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(500) NULL,
    category VARCHAR(30) NOT NULL,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tasks_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE,
    INDEX idx_tasks_employee_id (employee_id),
    INDEX idx_tasks_completed (completed),
    INDEX idx_tasks_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
