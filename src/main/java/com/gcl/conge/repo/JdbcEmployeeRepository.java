package com.gcl.conge.repo;

import com.gcl.conge.db.Database;
import com.gcl.conge.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JdbcEmployeeRepository implements EmployeeRepository {
    private final Database db;

    public JdbcEmployeeRepository(Database db) {
        this.db = db;
    }

    @Override
    public Employee save(Employee e) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO employees(id, name, annual_entitlement) VALUES(?,?,?) " +
                             "ON CONFLICT(id) DO UPDATE SET name=excluded.name, annual_entitlement=excluded.annual_entitlement")) {
            ps.setString(1, e.getId());
            ps.setString(2, e.getName());
            ps.setInt(3, e.getAnnualEntitlementDays());
            ps.executeUpdate();
            return e;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Employee findById(String id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, annual_entitlement FROM employees WHERE id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(rs.getString("id"), rs.getString("name"), rs.getInt("annual_entitlement"));
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Collection<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, annual_entitlement FROM employees ORDER BY name")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Employee(rs.getString("id"), rs.getString("name"), rs.getInt("annual_entitlement")));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}

