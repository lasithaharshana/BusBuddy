package com.example.BusBuddy.services;

import com.example.BusBuddy.Exception.EntityNotFoundException;
import com.example.BusBuddy.dto.Employee.*;
import com.example.BusBuddy.models.*;
import com.example.BusBuddy.repositories.DocumentRepository;
import com.example.BusBuddy.repositories.EmployeeRepository;
import com.example.BusBuddy.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BusinessService businessService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public EmployeePaginationResponse findAll(int  pageNumber , int pageSize){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        List<Employee> employees = employeePage.getContent();
        List<EmployeeResponse> employeeResponses = employees.stream().map((element) -> modelMapper.map(element, EmployeeResponse.class)).collect(Collectors.toList());

        return EmployeePaginationResponse.builder()
                .content(employeeResponses)
                .pageNo(employeePage.getNumber())
                .totalElements(employeePage.getTotalElements())
                .pageSize(employeePage.getSize())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .build();
    }

    @Transactional
    public EmployeePaginationResponse findEmployees(HttpServletRequest httpServletRequest,
                                                                              String name,
                                                                              int  pageNumber , int pageSize){

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Employee> employeePage;

        if(name != null && !name.isEmpty()){
            employeePage = employeeRepository.findByBusinessAndNameContainingIgnoreCase(
                    businessService.extractBId(httpServletRequest),
                    name , pageable);
        }else{
            employeePage = employeeRepository.findByBusiness(businessService.extractBId(httpServletRequest),
                    pageable
            );
        }

        List<Employee> employees = employeePage.getContent();
        List<EmployeeResponse> employeeResponses = employees.stream().map((element) -> modelMapper.map(element, EmployeeResponse.class)).collect(Collectors.toList());

        return EmployeePaginationResponse.builder()
                .content(employeeResponses)
                .pageNo(employeePage.getNumber())
                .totalElements(employeePage.getTotalElements())
                .pageSize(employeePage.getSize())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .build();
    }

    @Transactional
    public String add(HttpServletRequest httpServletRequest,
                                Float salary,
                                Date joinedDate,
                                Date bDay,
                                String email,
                                MultipartFile file){
        Business business = businessService.extractBId(httpServletRequest);
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("User Not found."));
        if(user.getEmployee() != null){
            throw new EntityNotFoundException("User is enrolled to a another business.");
        }
        Employee employee = Employee.builder()
                .designation(EmployeeType.valueOf("EMPLOYEE_TYPE_" + user.getRole().toString().substring(5)))
                .salary(salary)
                .bDay(bDay)
                .name(user.getFirstName()+" "+user.getLastName())
                .joinedDate(joinedDate)
                .business(business)
                .build();

        employeeRepository.save(employee);

        user.setEmployee(employee);
        user.setBusiness(business);
        userRepository.save(user);

        return "User enrolled to the business as a employee.";
    }

    @Transactional
    public String edit(
            HttpServletRequest httpServletRequest,
            Long empId,
            Float salary,
            Date joinedDate,
            Date bDay,
            MultipartFile file) throws IOException {
        Employee editedEmployee = employeeRepository.findById(empId)
                .orElseThrow(()-> new EntityNotFoundException("Employee Not found."));

        if(file != null && editedEmployee.getDocument() != null){
            Document document = editedEmployee.getDocument();
            document.setData(file.getBytes());
            document.setDocName(file.getOriginalFilename());
            documentRepository.save(document);
        }else if(file != null){
            documentService.add(file,httpServletRequest,
                    DocCategory.DOC_CATEGORY_SERVICE_AGREEMENT,
                    file.getOriginalFilename(),
                    editedEmployee.getEmpId()
            );
        }

        editedEmployee.setSalary(salary);
        editedEmployee.setJoinedDate(joinedDate);
        editedEmployee.setBDay(bDay);
        employeeRepository.save(editedEmployee);

        return  "Successfully Edited";
    }

    @Transactional
    public String removeEmployee(Long empId){
        Employee employee = employeeRepository.findById(empId).orElseThrow(()-> new EntityNotFoundException("Employee not found."));
        User user = employee.getUser();
        user.setBusiness(null);
        userRepository.save(user);
        employeeRepository.deleteById(empId);
        return "Successfully Deleted.";
    }

    public EmployeeCountResponse countEmployee(HttpServletRequest httpServletRequest){
        Business business = businessService.extractBId(httpServletRequest);
        Long totalCount = employeeRepository.countByBusiness(business);
        Long driverCount = employeeRepository.countByBusinessAndDesignation(business , EmployeeType.EMPLOYEE_TYPE_DRIVER);
        Long conductorCount = employeeRepository.countByBusinessAndDesignation(business, EmployeeType.EMPLOYEE_TYPE_CONDUCTOR);
        EmployeeCountResponse employeeCountResponse = EmployeeCountResponse.builder().totalCount(totalCount)
                .driverCount(driverCount).conductorCount(conductorCount).build();
        return employeeCountResponse;
    }

    @Transactional
    public ResponseEntity<EmployeePaginationResponse> findEmployee(
            HttpServletRequest httpServletRequest,
            int pageNumber,
            int pageSize,
            String name){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Employee> employeePage;
        if(name != null && !name.isEmpty()){
            employeePage =
                    employeeRepository.findByBusinessAndNameContainingIgnoreCase(
                            businessService.extractBId(httpServletRequest) ,
                            name,
                            pageable
                    );
        }else{
            employeePage =
                    employeeRepository.findByBusiness(
                            businessService.extractBId(httpServletRequest),
                            pageable
                    );
        }

        List<Employee> employees = employeePage.getContent();
        List<EmployeeResponse> busResponses = employees.stream()
                .map(employee ->
                        EmployeeResponse.builder()
                                .empId(employee.getEmpId())
                                .age(employee.getAge())
                                .bDay(employee.getBDay())
                                .salary(employee.getSalary())
                                .designation(employee.getDesignation())
                                .joinedDate(employee.getJoinedDate())
                                .docId(employee.getDocument() != null ? employee.getDocument().getDocId() : null)
                                .docName(employee.getDocument() != null ? employee.getDocument().getDocName() : null)
                                .build()
                )
                .toList();

        EmployeePaginationResponse employeePaginationResponse = EmployeePaginationResponse.builder()

                .content(busResponses)
                .pageSize(employeePage.getSize())
                .pageNo(employeePage.getNumber())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(employeePaginationResponse);

    }


    public Employee extractEmpId(@NotNull HttpServletRequest httpServletRequest){
        String str = (String) httpServletRequest.getAttribute("emp_id");
        long bId = Long.parseLong(str);
        return employeeRepository.findById(bId).orElseThrow(() -> new RuntimeException("Business not found."));
    }

    public List<Long> getDriverIds(HttpServletRequest httpServletRequest){
        Business business = businessService.extractBId(httpServletRequest);
        List<Long> driverIds = employeeRepository.findByBusinessAndDesignation(business , EmployeeType.EMPLOYEE_TYPE_DRIVER);

        return driverIds;
    }

    public List<Long> getConductorIds(HttpServletRequest httpServletRequest){
        Business business = businessService.extractBId(httpServletRequest);
        List<Long> conductorIds = employeeRepository.findByBusinessAndDesignation(business , EmployeeType.EMPLOYEE_TYPE_CONDUCTOR);

        return conductorIds;
    }
}
