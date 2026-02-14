package com.gcl.conge;

import com.gcl.conge.db.Database;
import com.gcl.conge.model.LeaveType;
import com.gcl.conge.repo.JdbcEmployeeRepository;
import com.gcl.conge.repo.JdbcLeaveRequestRepository;
import com.gcl.conge.service.LeaveService;
import com.gcl.conge.ui.CongeApp;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Database db = new Database("conge.db");
        try {
            db.initSchema();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JdbcEmployeeRepository employeeRepo = new JdbcEmployeeRepository(db);
        JdbcLeaveRequestRepository requestRepo = new JdbcLeaveRequestRepository(db);
        LeaveService service = new LeaveService(employeeRepo, requestRepo);
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                CongeApp app = new CongeApp(service, employeeRepo);
                app.setVisible(true);
            });
            return;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("demo")) {
            service.addEmployee("E001", "Alice", 20);
            service.addEmployee("E002", "Bob", 15);
            com.gcl.conge.model.LeaveRequest req1 = service.requestLeave("E001",
                    LocalDate.of(LocalDate.now().getYear(), 3, 10),
                    LocalDate.of(LocalDate.now().getYear(), 3, 14), LeaveType.ANNUAL);
            service.approveRequest(req1.getId());
            com.gcl.conge.model.LeaveRequest req2 = service.requestLeave("E001",
                    LocalDate.of(LocalDate.now().getYear(), 6, 1),
                    LocalDate.of(LocalDate.now().getYear(), 6, 2), LeaveType.SICK);
            service.approveRequest(req2.getId());
            int remainingAlice = service.getAnnualRemainingDays("E001", LocalDate.now().getYear());
            System.out.println("Alice remaining annual days: " + remainingAlice);
            int remainingBob = service.getAnnualRemainingDays("E002", LocalDate.now().getYear());
            System.out.println("Bob remaining annual days: " + remainingBob);
            return;
        }
    }
}
