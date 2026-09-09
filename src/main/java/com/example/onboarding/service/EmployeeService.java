package com.example.onboarding.service;

import com.example.onboarding.dto.DashboardStats;
import com.example.onboarding.dto.EmployeeForm;
import com.example.onboarding.dto.TaskForm;
import com.example.onboarding.entity.Employee;
import com.example.onboarding.entity.EmployeeStatus;
import com.example.onboarding.entity.OnboardingTask;
import com.example.onboarding.entity.TaskCategory;
import com.example.onboarding.exception.DuplicateResourceException;
import com.example.onboarding.exception.ResourceNotFoundException;
import com.example.onboarding.repository.EmployeeRepository;
import com.example.onboarding.repository.OnboardingTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OnboardingTaskRepository taskRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           OnboardingTaskRepository taskRepository) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> findEmployees(String search, EmployeeStatus status) {
        return employeeRepository.search(normalizeSearch(search), status);
    }

    @Transactional(readOnly = true)
    public Employee getEmployee(Long id) {
        return employeeRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    public Employee createEmployee(EmployeeForm form) {
        validateUniqueFields(form, null);

        Employee employee = new Employee();
        copyFormToEmployee(form, employee);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        addDefaultTasks(employee);

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, EmployeeForm form) {
        Employee employee = getEmployee(id);
        validateUniqueFields(form, id);
        copyFormToEmployee(form, employee);
        employee.setUpdatedAt(LocalDateTime.now());
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public void toggleTask(Long employeeId, Long taskId) {
        Employee employee = getEmployee(employeeId);
        OnboardingTask task = employee.getTasks().stream()
                .filter(item -> item.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for this employee"));

        boolean completed = !task.isCompleted();
        task.setCompleted(completed);
        task.setCompletedAt(completed ? LocalDateTime.now() : null);
        taskRepository.save(task);

        updateEmployeeStatusFromProgress(employee);
    }

    public void addTask(Long employeeId, TaskForm form) {
        Employee employee = getEmployee(employeeId);

        OnboardingTask task = new OnboardingTask();
        task.setTitle(form.getTitle().trim());
        task.setDescription(trimToNull(form.getDescription()));
        task.setCategory(form.getCategory());
        task.setDueDate(form.getDueDate());
        task.setCompleted(false);
        employee.addTask(task);
        employee.setUpdatedAt(LocalDateTime.now());

        employeeRepository.save(employee);
        updateEmployeeStatusFromProgress(employee);
    }

    public void deleteTask(Long employeeId, Long taskId) {
        Employee employee = getEmployee(employeeId);
        OnboardingTask task = employee.getTasks().stream()
                .filter(item -> item.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for this employee"));

        employee.getTasks().remove(task);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
        updateEmployeeStatusFromProgress(employee);
    }

    @Transactional(readOnly = true)
    public int getProgress(Employee employee) {
        int total = employee.getTasks().size();
        if (total == 0) {
            return 0;
        }
        long completed = employee.getTasks().stream().filter(OnboardingTask::isCompleted).count();
        return (int) ((completed * 100) / total);
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        return new DashboardStats(
                employeeRepository.count(),
                employeeRepository.countByStatus(EmployeeStatus.PRE_BOARDING),
                employeeRepository.countByStatus(EmployeeStatus.IN_PROGRESS),
                employeeRepository.countByStatus(EmployeeStatus.ACTIVE),
                taskRepository.countByCompletedFalse()
        );
    }

    public EmployeeForm toForm(Employee employee) {
        EmployeeForm form = new EmployeeForm();
        form.setEmployeeCode(employee.getEmployeeCode());
        form.setFirstName(employee.getFirstName());
        form.setLastName(employee.getLastName());
        form.setEmail(employee.getEmail());
        form.setPhone(employee.getPhone());
        form.setDepartment(employee.getDepartment());
        form.setJobTitle(employee.getJobTitle());
        form.setManagerName(employee.getManagerName());
        form.setJoiningDate(employee.getJoiningDate());
        form.setEmploymentType(employee.getEmploymentType());
        form.setStatus(employee.getStatus());
        return form;
    }

    private void validateUniqueFields(EmployeeForm form, Long currentEmployeeId) {
        String email = form.getEmail().trim();
        String employeeCode = form.getEmployeeCode().trim();

        boolean emailExists = currentEmployeeId == null
                ? employeeRepository.existsByEmailIgnoreCase(email)
                : employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, currentEmployeeId);
        if (emailExists) {
            throw new DuplicateResourceException("email", "An employee with this email already exists");
        }

        boolean codeExists = currentEmployeeId == null
                ? employeeRepository.existsByEmployeeCodeIgnoreCase(employeeCode)
                : employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(employeeCode, currentEmployeeId);
        if (codeExists) {
            throw new DuplicateResourceException("employeeCode", "This employee code is already in use");
        }
    }

    private void copyFormToEmployee(EmployeeForm form, Employee employee) {
        employee.setEmployeeCode(form.getEmployeeCode().trim().toUpperCase());
        employee.setFirstName(form.getFirstName().trim());
        employee.setLastName(form.getLastName().trim());
        employee.setEmail(form.getEmail().trim().toLowerCase());
        employee.setPhone(trimToNull(form.getPhone()));
        employee.setDepartment(form.getDepartment().trim());
        employee.setJobTitle(form.getJobTitle().trim());
        employee.setManagerName(trimToNull(form.getManagerName()));
        employee.setJoiningDate(form.getJoiningDate());
        employee.setEmploymentType(form.getEmploymentType());
        employee.setStatus(form.getStatus());
    }

    private void addDefaultTasks(Employee employee) {
        LocalDate joiningDate = employee.getJoiningDate();
        employee.addTask(newTask(
                "Collect identity and address documents",
                "Verify government ID, address proof, and emergency contact details.",
                TaskCategory.DOCUMENTATION,
                joiningDate.minusDays(5)
        ));
        employee.addTask(newTask(
                "Prepare employment documents",
                "Generate offer, NDA, policy acknowledgement, and payroll forms.",
                TaskCategory.HR,
                joiningDate.minusDays(3)
        ));
        employee.addTask(newTask(
                "Create company accounts",
                "Create email, identity provider, VPN, and required application accounts.",
                TaskCategory.IT_SETUP,
                joiningDate.minusDays(2)
        ));
        employee.addTask(newTask(
                "Allocate laptop and accessories",
                "Prepare laptop, charger, security tools, and required software.",
                TaskCategory.IT_SETUP,
                joiningDate.minusDays(1)
        ));
        employee.addTask(newTask(
                "Manager welcome meeting",
                "Introduce the employee to the manager, team, goals, and first-week plan.",
                TaskCategory.MANAGER,
                joiningDate
        ));
        employee.addTask(newTask(
                "Complete induction training",
                "Complete company orientation, security awareness, and policy training.",
                TaskCategory.TRAINING,
                joiningDate.plusDays(3)
        ));
    }

    private OnboardingTask newTask(String title, String description, TaskCategory category, LocalDate dueDate) {
        OnboardingTask task = new OnboardingTask();
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setDueDate(dueDate);
        task.setCompleted(false);
        return task;
    }

    private void updateEmployeeStatusFromProgress(Employee employee) {
        int progress = getProgress(employee);
        if (progress == 100 && !employee.getTasks().isEmpty()) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        } else if (progress > 0 && employee.getStatus() != EmployeeStatus.ON_HOLD) {
            employee.setStatus(EmployeeStatus.IN_PROGRESS);
        } else if (progress == 0 && employee.getStatus() != EmployeeStatus.ON_HOLD) {
            employee.setStatus(EmployeeStatus.PRE_BOARDING);
        }
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    private String normalizeSearch(String search) {
        return search == null ? "" : search.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
