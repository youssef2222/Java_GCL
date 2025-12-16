package com.gcl.conge;

import com.gcl.conge.model.LeaveType;
import com.gcl.conge.service.LeaveService;
import com.gcl.conge.db.Database;
import com.gcl.conge.repo.JdbcEmployeeRepository;
import com.gcl.conge.repo.JdbcLeaveRequestRepository;
import com.gcl.conge.ui.CongeApp;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

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
            var req1 = service.requestLeave("E001", LocalDate.of(LocalDate.now().getYear(), 3, 10),
                    LocalDate.of(LocalDate.now().getYear(), 3, 14), LeaveType.ANNUAL);
            service.approveRequest(req1.getId());
            var req2 = service.requestLeave("E001", LocalDate.of(LocalDate.now().getYear(), 6, 1),
                    LocalDate.of(LocalDate.now().getYear(), 6, 2), LeaveType.SICK);
            service.approveRequest(req2.getId());
            int remainingAlice = service.getAnnualRemainingDays("E001", LocalDate.now().getYear());
            System.out.println("Alice remaining annual days: " + remainingAlice);
            int remainingBob = service.getAnnualRemainingDays("E002", LocalDate.now().getYear());
            System.out.println("Bob remaining annual days: " + remainingBob);
            return;
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1) Add employee");
            System.out.println("2) Request leave");
            System.out.println("3) Approve request");
            System.out.println("4) Reject request");
            System.out.println("5) Annual remaining");
            System.out.println("6) List pending");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            try {
                if ("1".equals(choice)) {
                    System.out.print("Employee ID: ");
                    String id = scanner.nextLine().trim();
                    System.out.print("Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Annual entitlement days: ");
                    int ent = Integer.parseInt(scanner.nextLine().trim());
                    service.addEmployee(id, name, ent);
                    System.out.println("Saved");
                } else if ("2".equals(choice)) {
                    System.out.print("Employee ID: ");
                    String empId = scanner.nextLine().trim();
                    System.out.print("Start date (yyyy-MM-dd): ");
                    LocalDate start = LocalDate.parse(scanner.nextLine().trim());
                    System.out.print("End date (yyyy-MM-dd): ");
                    LocalDate end = LocalDate.parse(scanner.nextLine().trim());
                    System.out.print("Type (ANNUAL,SICK,UNPAID,MATERNITY,PATERNITY): ");
                    LeaveType type = LeaveType.valueOf(scanner.nextLine().trim().toUpperCase());
                    var req = service.requestLeave(empId, start, end, type);
                    System.out.println("Request ID: " + req.getId());
                } else if ("3".equals(choice)) {
                    System.out.print("Request ID (UUID): ");
                    UUID id = UUID.fromString(scanner.nextLine().trim());
                    var r = service.approveRequest(id);
                    System.out.println("Approved: " + r.getId());
                } else if ("4".equals(choice)) {
                    System.out.print("Request ID (UUID): ");
                    UUID id = UUID.fromString(scanner.nextLine().trim());
                    System.out.print("Reason: ");
                    String reason = scanner.nextLine().trim();
                    var r = service.rejectRequest(id, reason);
                    System.out.println("Rejected: " + r.getId());
                } else if ("5".equals(choice)) {
                    System.out.print("Employee ID: ");
                    String empId = scanner.nextLine().trim();
                    System.out.print("Year: ");
                    int year = Integer.parseInt(scanner.nextLine().trim());
                    int remaining = service.getAnnualRemainingDays(empId, year);
                    System.out.println("Remaining: " + remaining);
                } else if ("6".equals(choice)) {
                    var list = service.listPendingRequests();
                    for (var r : list) {
                        System.out.println(r.getId() + " " + r.getEmployeeId() + " " + r.getStartDate() + " "
                                + r.getEndDate() + " " + r.getType());
                    }
                } else if ("0".equals(choice)) {
                    break;
                } else {
                    System.out.println("Invalid choice");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
