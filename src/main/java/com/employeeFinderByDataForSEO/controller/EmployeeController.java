package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.dto.EmployeeResponse;
import com.employeeFinderByDataForSEO.service.EmployeeSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeSearchService employeeSearchService;

    public EmployeeController(
            EmployeeSearchService employeeSearchService) {

        this.employeeSearchService = employeeSearchService;
    }

    @GetMapping
    public List<EmployeeResponse> search(
            @RequestParam String company) {

        return employeeSearchService.search(company);
    }
}