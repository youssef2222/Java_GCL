package com.gcl.conge.repo;

import com.gcl.conge.model.Employee;

import java.util.Collection;

public interface EmployeeRepository {
    Employee save(Employee e);
    Employee findById(String id);
    Collection<Employee> findAll();
}

