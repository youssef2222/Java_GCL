package com.gcl.conge.repo;

import com.gcl.conge.model.Employee;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEmployeeRepository implements EmployeeRepository {
    private final Map<String, Employee> employees = new ConcurrentHashMap<>();

    @Override
    public Employee save(Employee e) {
        employees.put(e.getId(), e);
        return e;
    }

    @Override
    public Employee findById(String id) {
        return employees.get(id);
    }

    @Override
    public Collection<Employee> findAll() {
        return employees.values();
    }
}

