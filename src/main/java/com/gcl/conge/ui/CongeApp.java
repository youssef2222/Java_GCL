package com.gcl.conge.ui;

import com.gcl.conge.model.LeaveType;
import com.gcl.conge.model.LeaveRequest;
import com.gcl.conge.repo.EmployeeRepository;
import com.gcl.conge.service.LeaveService;
import com.gcl.conge.ui.LogoLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CongeApp extends JFrame {
    private final LeaveService service;
    private final EmployeeRepository employeeRepo;

    public CongeApp(LeaveService service, EmployeeRepository employeeRepo) {
        this.service = service;
        this.employeeRepo = employeeRepo;
        setTitle("Gestion Congé");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        ImageIcon icon = LogoLoader.loadLogoIcon();
        if (icon != null) {
            setIconImage(icon.getImage());
        }
        JPanel root = new JPanel(new BorderLayout());
        if (icon != null) {
            JLabel logo = new JLabel(new ImageIcon(LogoLoader.scaleForHeader(icon.getImage(), 160, 60)));
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
            header.add(logo);
            JLabel title = new JLabel("CHU Ibn Sina – Gestion Congé");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            header.add(title);
            root.add(header, BorderLayout.NORTH);
        }
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Employés", buildEmployeesPanel());
        tabs.add("Demande Congé", buildRequestPanel());
        tabs.add("En Attente", buildPendingPanel());
        tabs.add("Solde Annuel", buildBalancePanel());
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildEmployeesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 2, 8, 8));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField entField = new JTextField();
        JButton addBtn = new JButton("Ajouter");
        form.add(new JLabel("ID"));
        form.add(idField);
        form.add(new JLabel("Nom"));
        form.add(nameField);
        form.add(new JLabel("Droit annuel (jours)"));
        form.add(entField);
        form.add(new JLabel(""));
        form.add(addBtn);
        DefaultTableModel model = new DefaultTableModel(new Object[] { "ID", "Nom", "Droit Annuel" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JButton refreshBtn = new JButton("Rafraîchir");
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            employeeRepo.findAll().forEach(
                    emp -> model.addRow(new Object[] { emp.getId(), emp.getName(), emp.getAnnualEntitlementDays() }));
        });
        addBtn.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                int ent = Integer.parseInt(entField.getText().trim());
                service.addEmployee(id, name, ent);
                refreshBtn.doClick();
                JOptionPane.showMessageDialog(panel, "Employé ajouté");
                nameField.setText("");
                entField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRequestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 8, 8));
        JTextField empIdField = new JTextField();
        empIdField.setText("22");
        JTextField startField = new JTextField();
        JTextField endField = new JTextField();
        JComboBox<LeaveType> typeBox = new JComboBox<>(LeaveType.values());
        JButton submitBtn = new JButton("Soumettre");
        JLabel result = new JLabel("");
        result.setFont(result.getFont().deriveFont(Font.BOLD));
        panel.add(new JLabel("ID Employé"));
        panel.add(empIdField);
        panel.add(new JLabel("Date début (yyyy-MM-dd)"));
        panel.add(startField);
        panel.add(new JLabel("Date fin (yyyy-MM-dd)"));
        panel.add(endField);
        panel.add(new JLabel("Type"));
        panel.add(typeBox);
        panel.add(new JLabel(""));
        panel.add(submitBtn);
        panel.add(new JLabel("Résultat"));
        panel.add(result);
        submitBtn.addActionListener(e -> {
            try {
                String empId = empIdField.getText().trim();
                LocalDate start = LocalDate.parse(startField.getText().trim());
                LocalDate end = LocalDate.parse(endField.getText().trim());
                LeaveType type = (LeaveType) typeBox.getSelectedItem();
                LeaveRequest r = service.requestLeave(empId, start, end, type);
                String msg = "Demande enregistrée\nN°: " + r.getDisplayId() + "\nJours: " + r.getDays() + "\nType: "
                        + r.getType();
                result.setText("N°: " + r.getDisplayId());
                JOptionPane.showMessageDialog(panel, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
                startField.setText("");
                endField.setText("");
                typeBox.setSelectedIndex(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private JPanel buildPendingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[] { "N°", "Employé", "Début", "Fin", "Type" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JButton refreshBtn = new JButton("Rafraîchir");
        JButton approveBtn = new JButton("Approuver");
        JButton rejectBtn = new JButton("Rejeter");
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<LeaveRequest> list = service.listPendingRequests();
            list.forEach(r -> model.addRow(new Object[] { r.getDisplayId(), r.getEmployeeId(),
                    r.getStartDate().toString(), r.getEndDate().toString(), r.getType().name() }));
        });
        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                try {
                    Object idVal = model.getValueAt(row, 0);
                    String idStr = idVal == null ? "" : String.valueOf(idVal).trim();
                    if (idStr.isEmpty() || idStr.equalsIgnoreCase("null")) {
                        JOptionPane.showMessageDialog(panel, "N° demande manquant. Rafraîchissez la liste.", "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    long displayId = Long.parseLong(idStr);
                    service.approveByDisplayId(displayId);
                    refreshBtn.doClick();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String reason = JOptionPane.showInputDialog(panel, "Raison du rejet");
                if (reason != null) {
                    try {
                        Object idVal = model.getValueAt(row, 0);
                        String idStr = idVal == null ? "" : String.valueOf(idVal).trim();
                        if (idStr.isEmpty() || idStr.equalsIgnoreCase("null")) {
                            JOptionPane.showMessageDialog(panel, "N° demande manquant. Rafraîchissez la liste.",
                                    "Erreur",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        long displayId = Long.parseLong(idStr);
                        service.rejectByDisplayId(displayId, reason);
                        refreshBtn.doClick();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        JPanel pendingSection = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("En Attente"));
        top.add(refreshBtn);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(approveBtn);
        bottom.add(rejectBtn);
        pendingSection.add(top, BorderLayout.NORTH);
        pendingSection.add(new JScrollPane(table), BorderLayout.CENTER);
        pendingSection.add(bottom, BorderLayout.SOUTH);

        DefaultTableModel approvedModel = new DefaultTableModel(
                new Object[] { "N°", "Employé", "Début", "Fin", "Type", "Jours" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable approvedTable = new JTable(approvedModel);
        JButton refreshApprovedBtn = new JButton("Rafraîchir");
        refreshApprovedBtn.addActionListener(e -> {
            approvedModel.setRowCount(0);
            List<LeaveRequest> list = service.listApprovedRequests();
            list.forEach(r -> approvedModel.addRow(new Object[] { r.getDisplayId(), r.getEmployeeId(),
                    r.getStartDate().toString(), r.getEndDate().toString(), r.getType().name(), r.getDays() }));
        });
        JPanel approvedTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        approvedTop.add(new JLabel("Approuvés"));
        approvedTop.add(refreshApprovedBtn);
        JPanel approvedSection = new JPanel(new BorderLayout());
        approvedSection.add(approvedTop, BorderLayout.NORTH);
        approvedSection.add(new JScrollPane(approvedTable), BorderLayout.CENTER);

        JPanel listsPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        listsPanel.add(pendingSection);
        listsPanel.add(approvedSection);

        panel.add(listsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBalancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 8, 8));
        JTextField empIdField = new JTextField();
        JTextField yearField = new JTextField();
        JButton calcBtn = new JButton("Calculer");
        JLabel result = new JLabel("");
        panel.add(new JLabel("ID Employé"));
        panel.add(empIdField);
        panel.add(new JLabel("Année"));
        panel.add(yearField);
        panel.add(new JLabel(""));
        panel.add(calcBtn);
        panel.add(new JLabel("Solde"));
        panel.add(result);
        calcBtn.addActionListener(e -> {
            try {
                String empId = empIdField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                int remaining = service.getAnnualRemainingDays(empId, year);
                result.setText(String.valueOf(remaining));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
}
