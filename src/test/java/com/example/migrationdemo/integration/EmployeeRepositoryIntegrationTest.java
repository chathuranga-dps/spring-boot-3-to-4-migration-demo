package com.example.migrationdemo.integration;

import com.example.migrationdemo.entity.Employee;
import com.example.migrationdemo.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MIGRATION-DEMO:
 * Repository integration tests use H2 to match the application runtime database
 * and validate Hibernate / Spring Data compatibility in Spring Boot 4.
 */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void testSaveEmployee() {
        Employee employee = new Employee(
                "EMP001",
                "John",
                "Doe",
                "john@example.com",
                "Technology",
                new BigDecimal("85000.00"));

        Employee savedEmployee = employeeRepository.save(employee);

        assertNotNull(savedEmployee.getId());
        assertEquals("EMP001", savedEmployee.getEmployeeNumber());
        assertEquals("john@example.com", savedEmployee.getEmail());
    }

    @Test
    void testFindByEmployeeNumber() {
        Employee employee = new Employee(
                "EMP002",
                "Jane",
                "Smith",
                "jane@example.com",
                "Finance",
                new BigDecimal("90000.00"));
        employeeRepository.save(employee);

        Optional<Employee> found = employeeRepository.findByEmployeeNumber("EMP002");

        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
    }

    @Test
    void testFindByEmail() {
        Employee employee = new Employee(
                "EMP006",
                "Avery",
                "Taylor",
                "avery@example.com",
                "Operations",
                new BigDecimal("78000.00"));
        employeeRepository.save(employee);

        Optional<Employee> found = employeeRepository.findByEmail("avery@example.com");

        assertTrue(found.isPresent());
        assertEquals("EMP006", found.get().getEmployeeNumber());
    }

    @Test
    void testFindByDepartmentIgnoreCase() {
        Employee emp1 = new Employee("EMP001", "John", "Doe", "john@example.com", "Technology",
                new BigDecimal("85000.00"));
        Employee emp2 = new Employee("EMP002", "Jane", "Smith", "jane@example.com", "Technology",
                new BigDecimal("90000.00"));
        Employee emp3 = new Employee("EMP003", "Mike", "Brown", "mike@example.com", "Finance",
                new BigDecimal("80000.00"));

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        List<Employee> techEmployees = employeeRepository.findByDepartmentIgnoreCase("TECHNOLOGY");

        assertEquals(2, techEmployees.size());
    }

    @Test
    void testFindActiveEmployeesByDepartment() {
        Employee emp1 = new Employee("EMP001", "John", "Doe", "john@example.com", "Technology",
                new BigDecimal("85000.00"));
        emp1.setActive(true);

        Employee emp2 = new Employee("EMP002", "Jane", "Smith", "jane@example.com", "Technology",
                new BigDecimal("90000.00"));
        emp2.setActive(false);

        Employee emp3 = new Employee("EMP003", "Mike", "Brown", "mike@example.com", "Finance",
                new BigDecimal("80000.00"));
        emp3.setActive(true);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        /**
         * MIGRATION-DEMO: JPQL query test
         * This test validates that the JPQL query works correctly
         */
        List<Employee> activeInTech = employeeRepository.findActiveEmployeesByDepartment("Technology");

        assertEquals(1, activeInTech.size());
        assertEquals("John", activeInTech.get(0).getFirstName());
    }

    @Test
    void testFindHighEarners() {
        Employee emp1 = new Employee("EMP001", "John", "Doe", "john@example.com", "Technology",
                new BigDecimal("95000.00"));
        emp1.setActive(true);

        Employee emp2 = new Employee("EMP002", "Jane", "Smith", "jane@example.com", "Finance",
                new BigDecimal("75000.00"));
        emp2.setActive(true);

        Employee emp3 = new Employee("EMP003", "Mike", "Brown", "mike@example.com", "Technology",
                new BigDecimal("88000.00"));
        emp3.setActive(true);

        employeeRepository.save(emp1);
        employeeRepository.save(emp2);
        employeeRepository.save(emp3);

        // MIGRATION-DEMO: Validates the native SQL query against the H2 runtime database.
        List<Employee> highEarners = employeeRepository.findHighEarners(new BigDecimal("90000.00"));

        assertEquals(1, highEarners.size());
        assertEquals("John", highEarners.get(0).getFirstName());
    }

    @Test
    void testUpdateEmployee() {
        Employee employee = employeeRepository.save(new Employee(
                "EMP007",
                "Morgan",
                "Lee",
                "morgan@example.com",
                "Finance",
                new BigDecimal("81000.00")));

        employee.setDepartment("Technology");
        employee.setSalary(new BigDecimal("91000.00"));
        employeeRepository.saveAndFlush(employee);

        Employee updated = employeeRepository.findById(employee.getId()).orElseThrow();
        assertEquals("Technology", updated.getDepartment());
        assertEquals(new BigDecimal("91000.00"), updated.getSalary());
    }

    @Test
    void testDeleteEmployee() {
        Employee employee = employeeRepository.save(new Employee(
                "EMP008",
                "Jordan",
                "Casey",
                "jordan@example.com",
                "HR",
                new BigDecimal("70000.00")));

        employeeRepository.deleteById(employee.getId());
        employeeRepository.flush();

        assertFalse(employeeRepository.findById(employee.getId()).isPresent());
    }

}
