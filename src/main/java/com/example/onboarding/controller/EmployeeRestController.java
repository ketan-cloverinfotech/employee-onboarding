package com.example.onboarding.controller;

import com.example.onboarding.dto.EmployeeForm;
import com.example.onboarding.dto.EmployeeResponse;
import com.example.onboarding.entity.EmployeeStatus;
import com.example.onboarding.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeRestController {

    private final EmployeeService employeeService;

    public EmployeeRestController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> list(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) EmployeeStatus status) {
        return employeeService.findEmployees(search, status).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return EmployeeResponse.from(employeeService.getEmployee(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeForm form) {
        return EmployeeResponse.from(employeeService.createEmployee(form));
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeForm form) {
        return EmployeeResponse.from(employeeService.updateEmployee(id, form));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}
