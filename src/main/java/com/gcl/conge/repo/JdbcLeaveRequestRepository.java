package com.gcl.conge.repo;

import com.gcl.conge.db.Database;
import com.gcl.conge.model.ApprovalStatus;
import com.gcl.conge.model.LeaveRequest;
import com.gcl.conge.model.LeaveType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class JdbcLeaveRequestRepository implements LeaveRequestRepository {
    private final Database db;

    public JdbcLeaveRequestRepository(Database db) {
        this.db = db;
    }

    @Override
    public LeaveRequest save(LeaveRequest r) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO leave_requests(id, employee_id, start_date, end_date, type, year, days, status, rejection_reason, display_id) "
                                +
                                "VALUES(?,?,?,?,?,?,?,?,?,?) " +
                                "ON CONFLICT(id) DO UPDATE SET employee_id=excluded.employee_id, start_date=excluded.start_date, end_date=excluded.end_date, "
                                +
                                "type=excluded.type, year=excluded.year, days=excluded.days, status=excluded.status, rejection_reason=excluded.rejection_reason, display_id=COALESCE(leave_requests.display_id, excluded.display_id)")) {
            if (r.getDisplayId() == null) {
                long next = db.nextSequence("leave_requests");
                r.setDisplayId(next);
            }
            ps.setString(1, r.getId().toString());
            ps.setString(2, r.getEmployeeId());
            ps.setString(3, r.getStartDate().toString());
            ps.setString(4, r.getEndDate().toString());
            ps.setString(5, r.getType().name());
            ps.setInt(6, r.getYear());
            ps.setInt(7, r.getDays());
            ps.setString(8, r.getStatus().name());
            ps.setString(9, r.getRejectionReason());
            if (r.getDisplayId() != null) {
                ps.setLong(10, r.getDisplayId());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
            return r;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public LeaveRequest findById(UUID id) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM leave_requests WHERE id=?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public LeaveRequest findByDisplayId(long displayId) {
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM leave_requests WHERE display_id=?")) {
            ps.setLong(1, displayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<LeaveRequest> findByEmployee(String employeeId) {
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM leave_requests WHERE employee_id=?")) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        list.sort(Comparator.comparing(LeaveRequest::getStartDate));
        return list;
    }

    @Override
    public List<LeaveRequest> findByYearAndEmployee(int year, String employeeId) {
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c
                        .prepareStatement("SELECT * FROM leave_requests WHERE year=? AND employee_id=?")) {
            ps.setInt(1, year);
            ps.setString(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        list.sort(Comparator.comparing(LeaveRequest::getStartDate));
        return list;
    }

    @Override
    public List<LeaveRequest> findPending() {
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM leave_requests WHERE status='PENDING'")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        list.sort(Comparator.comparing(LeaveRequest::getStartDate));
        return list;
    }

    @Override
    public List<LeaveRequest> findApproved() {
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = db.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM leave_requests WHERE status='APPROVED'")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        list.sort(Comparator.comparing(LeaveRequest::getStartDate));
        return list;
    }

    private LeaveRequest map(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String employeeId = rs.getString("employee_id");
        LocalDate start = LocalDate.parse(rs.getString("start_date"));
        LocalDate end = LocalDate.parse(rs.getString("end_date"));
        LeaveType type = LeaveType.valueOf(rs.getString("type"));
        int year = rs.getInt("year");
        int days = rs.getInt("days");
        ApprovalStatus status = ApprovalStatus.valueOf(rs.getString("status"));
        String rejection = rs.getString("rejection_reason");
        LeaveRequest lr = new LeaveRequest(id, employeeId, start, end, type, days, year, status, rejection);
        long displayId = rs.getLong("display_id");
        if (!rs.wasNull()) {
            lr.setDisplayId(displayId);
        }
        return lr;
    }

}
