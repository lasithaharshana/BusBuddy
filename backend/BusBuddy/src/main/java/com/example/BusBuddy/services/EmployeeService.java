package com.example.BusBuddy.services;

import com.example.BusBuddy.Exception.EntityNotFoundExceptions.EntityNotFoundException;
import com.example.BusBuddy.dto.Employee.EmployeeAddRequest;
import com.example.BusBuddy.dto.Employee.EmployeeEditReq;
import com.example.BusBuddy.dto.Employee.EmployeePaginationResponse;
import com.example.BusBuddy.dto.Employee.EmployeeResponse;
import com.example.BusBuddy.models.Business;
import com.example.BusBuddy.models.Employee;
import com.example.BusBuddy.models.User;
import com.example.BusBuddy.repositories.EmployeeRepository;
import com.example.BusBuddy.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BusinessService businessService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public ResponseEntity<EmployeePaginationResponse> findEmployees(HttpServletRequest httpServletRequest , int pageNumber , int pageSize){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        List<Employee> employees = employeePage.getContent();
        List<EmployeeResponse> employeeResponses = employees.stream().map((element) -> modelMapper.map(element, EmployeeResponse.class)).collect(Collectors.toList());

        EmployeePaginationResponse employeePaginationResponse = EmployeePaginationResponse.builder()
                .content(employeeResponses)
                .pageNo(employeePage.getNumber())
                .totalElements(employeePage.getTotalElements())
                .pageSize(employeePage.getSize())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(employeePaginationResponse);
    }

    @Transactional
    public EmployeeResponse save(HttpServletRequest httpRequest , EmployeeAddRequest request){
        Business business = businessService.extractBId(httpRequest);
        Employee employee = Employee.builder()
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .bDay(request.getBDay())
                .name(request.getName())
                .joinedDate(request.getJoinedDate())
                .business(business)
                .build();

        employeeRepository.save(employee);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()->new EntityNotFoundException("User Not found."));
        user.setEmployee(employee);
        user.setBusiness(business);
        userRepository.save(user);

        return modelMapper.map(employee, EmployeeResponse.class);
    }

    public ResponseEntity<String> editEmployee(EmployeeEditReq request){
        Employee info = employeeRepository.findById(request.getEmpId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id : " + request.getEmpId()
                ));
        info.setSalary(request.getSalary());
        info.setSalary(request.getSalary());

        employeeRepository.save(info);

        return ResponseEntity.status(HttpStatus.OK).body("Edited successfully");
    }

    public ResponseEntity<String> removeEmployee(Long empId){
        employeeRepository.deleteById(empId);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully Deleted.");
    }
}
