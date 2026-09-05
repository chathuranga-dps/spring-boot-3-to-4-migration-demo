package com.example.migrationdemo.controller;

import com.example.migrationdemo.dto.EmployeeCreateRequest;
import com.example.migrationdemo.dto.EmployeeResponse;
import com.example.migrationdemo.exception.EmployeeNotFoundException;
import com.example.migrationdemo.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testGetAllEmployees_Success() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                .with(httpBasic("demo", "demo123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAllEmployees_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllEmployees_WithBasicAuth() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                .with(httpBasic("demo", "demo123")))
                .andExpect(status().isOk());
    }

    @Test
    void testOpenApiDocumentation_Public() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.title").value("Employee Management API"))
                .andExpect(jsonPath("$.info.version").value("1.0"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.paths['/api/v1/employees'].get.summary")
                        .value("Get all employees"))
                .andExpect(jsonPath("$.paths['/api/v1/employees'].post.summary")
                        .value("Create an employee"))
                .andExpect(jsonPath("$.paths['/api/v1/employees/{id}'].get.summary")
                        .value("Get an employee by ID"))
                .andExpect(jsonPath("$.paths['/api/v1/employees/{id}'].put.summary")
                        .value("Update an employee"))
                .andExpect(jsonPath("$.paths['/api/v1/employees/{id}'].delete.summary")
                        .value("Delete an employee"))
                .andExpect(jsonPath("$.paths['/api/v1/employees/search'].get.summary")
                        .value("Search employees by department"))
                .andExpect(jsonPath("$.paths['/api/v1/employees/high-earners'].get.summary")
                        .value("Find active employees earning above a salary threshold"));
    }

    @Test
    void testSwaggerUi_Public() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void testGetEmployeeById_Success() throws Exception {
        EmployeeResponse response = new EmployeeResponse(
                1L,
                "EMP001",
                "John",
                "Doe",
                "john@example.com",
                "Technology",
                new BigDecimal("85000.00"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/employees/1")
                .with(httpBasic("demo", "demo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"))
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    void testJacksonContract_ExcludesNullValues() throws Exception {
        EmployeeResponse response = new EmployeeResponse(
                1L,
                "EMP001",
                "John",
                "Doe",
                "john@example.com",
                null,
                new BigDecimal("85000.00"),
                true,
                LocalDateTime.of(2026, 9, 5, 12, 30),
                LocalDateTime.of(2026, 9, 5, 12, 30));

        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/employees/1")
                .with(httpBasic("demo", "demo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").doesNotExist())
                .andExpect(jsonPath("$.createdAt").value("2026-09-05T12:30:00"));
    }

    @Test
    void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new EmployeeNotFoundException(999L));

        mockMvc.perform(get("/api/v1/employees/999")
                .with(httpBasic("demo", "demo123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateEmployee_Success() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "EMP001",
                "John",
                "Doe",
                "john@example.com",
                "Technology",
                new BigDecimal("85000.00"));

        EmployeeResponse response = new EmployeeResponse(
                1L,
                "EMP001",
                "John",
                "Doe",
                "john@example.com",
                "Technology",
                new BigDecimal("85000.00"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(employeeService.createEmployee(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/employees")
                .with(httpBasic("demo", "demo123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCreateEmployee_InvalidRequest() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "", // Empty employee number
                "John",
                "Doe",
                "invalid-email", // Invalid email
                "Technology",
                new BigDecimal("85000.00"));

        mockMvc.perform(post("/api/v1/employees")
                .with(httpBasic("demo", "demo123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testActuatorHealth_Public() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testActuatorInfo_Public() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

}
