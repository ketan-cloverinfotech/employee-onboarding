package com.example.onboarding.controller;

import com.example.onboarding.dto.EmployeeForm;
import com.example.onboarding.dto.TaskForm;
import com.example.onboarding.entity.Employee;
import com.example.onboarding.entity.EmployeeStatus;
import com.example.onboarding.entity.EmploymentType;
import com.example.onboarding.entity.TaskCategory;
import com.example.onboarding.exception.DuplicateResourceException;
import com.example.onboarding.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class EmployeeWebController {

    private final EmployeeService employeeService;

    public EmployeeWebController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @ModelAttribute
    public void commonModel(Model model) {
        model.addAttribute("statuses", EmployeeStatus.values());
        model.addAttribute("employmentTypes", EmploymentType.values());
        model.addAttribute("taskCategories", TaskCategory.values());
    }

    @GetMapping("/employees")
    public String listEmployees(@RequestParam(required = false) String search,
                                @RequestParam(required = false) EmployeeStatus status,
                                Model model) {
        model.addAttribute("employees", employeeService.findEmployees(search, status));
        model.addAttribute("stats", employeeService.getDashboardStats());
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("selectedStatus", status);
        return "employees/list";
    }

    @GetMapping("/employees/new")
    public String newEmployee(Model model) {
        EmployeeForm form = new EmployeeForm();
        form.setJoiningDate(LocalDate.now().plusDays(7));
        form.setStatus(EmployeeStatus.PRE_BOARDING);
        model.addAttribute("employeeForm", form);
        model.addAttribute("pageTitle", "Add employee");
        model.addAttribute("formAction", "/employees");
        return "employees/form";
    }

    @PostMapping("/employees")
    public String createEmployee(@Valid @ModelAttribute EmployeeForm employeeForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, "Add employee", "/employees");
            return "employees/form";
        }

        try {
            Employee employee = employeeService.createEmployee(employeeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Employee created with default onboarding tasks.");
            return "redirect:/employees/" + employee.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            prepareFormModel(model, "Add employee", "/employees");
            return "employees/form";
        }
    }

    @GetMapping("/employees/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployee(id);
        model.addAttribute("employee", employee);
        model.addAttribute("progress", employeeService.getProgress(employee));
        model.addAttribute("taskForm", new TaskForm());
        return "employees/detail";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployee(id);
        model.addAttribute("employeeForm", employeeService.toForm(employee));
        model.addAttribute("employeeId", id);
        prepareFormModel(model, "Edit employee", "/employees/" + id);
        return "employees/form";
    }

    @PostMapping("/employees/{id}")
    public String updateEmployee(@PathVariable Long id,
                                 @Valid @ModelAttribute EmployeeForm employeeForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeId", id);
            prepareFormModel(model, "Edit employee", "/employees/" + id);
            return "employees/form";
        }

        try {
            employeeService.updateEmployee(id, employeeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Employee details updated.");
            return "redirect:/employees/" + id;
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            model.addAttribute("employeeId", id);
            prepareFormModel(model, "Edit employee", "/employees/" + id);
            return "employees/form";
        }
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("successMessage", "Employee deleted.");
        return "redirect:/employees";
    }

    @PostMapping("/employees/{employeeId}/tasks/{taskId}/toggle")
    public String toggleTask(@PathVariable Long employeeId,
                             @PathVariable Long taskId,
                             RedirectAttributes redirectAttributes) {
        employeeService.toggleTask(employeeId, taskId);
        redirectAttributes.addFlashAttribute("successMessage", "Task status updated.");
        return "redirect:/employees/" + employeeId;
    }

    @PostMapping("/employees/{employeeId}/tasks")
    public String addTask(@PathVariable Long employeeId,
                          @Valid @ModelAttribute TaskForm taskForm,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Employee employee = employeeService.getEmployee(employeeId);
            model.addAttribute("employee", employee);
            model.addAttribute("progress", employeeService.getProgress(employee));
            return "employees/detail";
        }

        employeeService.addTask(employeeId, taskForm);
        redirectAttributes.addFlashAttribute("successMessage", "Onboarding task added.");
        return "redirect:/employees/" + employeeId;
    }

    @PostMapping("/employees/{employeeId}/tasks/{taskId}/delete")
    public String deleteTask(@PathVariable Long employeeId,
                             @PathVariable Long taskId,
                             RedirectAttributes redirectAttributes) {
        employeeService.deleteTask(employeeId, taskId);
        redirectAttributes.addFlashAttribute("successMessage", "Task deleted.");
        return "redirect:/employees/" + employeeId;
    }

    private void prepareFormModel(Model model, String pageTitle, String formAction) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }
}
