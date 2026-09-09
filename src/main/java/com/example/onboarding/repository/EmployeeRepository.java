package com.example.onboarding.repository;

import com.example.onboarding.entity.Employee;
import com.example.onboarding.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    long countByStatus(EmployeeStatus status);

    @EntityGraph(attributePaths = "tasks")
    @Query("select distinct e from Employee e order by e.createdAt desc")
    List<Employee> findAllWithTasks();

    @EntityGraph(attributePaths = "tasks")
    @Query("select distinct e from Employee e where e.id = :id")
    Optional<Employee> findByIdWithTasks(@Param("id") Long id);

    @EntityGraph(attributePaths = "tasks")
    @Query("""
            select distinct e from Employee e
            where (:search is null or :search = '' or
                   lower(e.firstName) like lower(concat('%', :search, '%')) or
                   lower(e.lastName) like lower(concat('%', :search, '%')) or
                   lower(e.email) like lower(concat('%', :search, '%')) or
                   lower(e.employeeCode) like lower(concat('%', :search, '%')) or
                   lower(e.department) like lower(concat('%', :search, '%')))
              and (:status is null or e.status = :status)
            order by e.createdAt desc
            """)
    List<Employee> search(@Param("search") String search, @Param("status") EmployeeStatus status);
}
