package com.gcl.conge.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Database {
    private final String url;

    public Database(String filePath) {
        this.url = "jdbc:sqlite:" + filePath;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void initSchema() throws SQLException {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS employees (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "annual_entitlement INTEGER NOT NULL" +
                    ")");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS leave_requests (" +
                    "id TEXT UNIQUE," +
                    "employee_id TEXT NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "end_date TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "year INTEGER NOT NULL," +
                    "days INTEGER NOT NULL," +
                    "status TEXT NOT NULL," +
                    "rejection_reason TEXT," +
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ")");
            try (Statement st2 = c.createStatement();
                    ResultSet rs = st2.executeQuery("PRAGMA table_info(leave_requests)")) {
                boolean hasDisplayId = false;
                while (rs.next()) {
                    if ("display_id".equalsIgnoreCase(rs.getString("name"))) {
                        hasDisplayId = true;
                        break;
                    }
                }
                if (!hasDisplayId) {
                    try (Statement st3 = c.createStatement()) {
                        st3.executeUpdate("ALTER TABLE leave_requests ADD COLUMN display_id INTEGER");
                    }
                }
            }
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sequences (" +
                    "name TEXT PRIMARY KEY," +
                    "value INTEGER NOT NULL" +
                    ")");
        }
        ensureSequenceInitialized("leave_requests");
        backfillDisplayIds();
    }

    public long nextSequence(String name) throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement sel = c.prepareStatement("SELECT value FROM sequences WHERE name=?")) {
                sel.setString(1, name);
                long v;
                try (ResultSet rs = sel.executeQuery()) {
                    if (rs.next()) {
                        v = rs.getLong(1) + 1;
                        try (PreparedStatement upd = c.prepareStatement("UPDATE sequences SET value=? WHERE name=?")) {
                            upd.setLong(1, v);
                            upd.setString(2, name);
                            upd.executeUpdate();
                        }
                    } else {
                        v = 1;
                        try (PreparedStatement ins = c
                                .prepareStatement("INSERT INTO sequences(name,value) VALUES(?,?)")) {
                            ins.setString(1, name);
                            ins.setLong(2, v);
                            ins.executeUpdate();
                        }
                    }
                }
                c.commit();
                return v;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void ensureSequenceInitialized(String name) throws SQLException {
        try (Connection c = getConnection();
                PreparedStatement sel = c.prepareStatement("SELECT value FROM sequences WHERE name=?")) {
            sel.setString(1, name);
            try (ResultSet rs = sel.executeQuery()) {
                if (!rs.next()) {
                    long max = 0;
                    try (Statement st = c.createStatement();
                            ResultSet rsm = st.executeQuery("SELECT COALESCE(MAX(display_id),0) FROM leave_requests")) {
                        if (rsm.next()) {
                            max = rsm.getLong(1);
                        }
                    }
                    try (PreparedStatement ins = c
                            .prepareStatement("INSERT INTO sequences(name,value) VALUES(?,?)")) {
                        ins.setString(1, name);
                        ins.setLong(2, max);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    public void backfillDisplayIds() throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            long current;
            try (PreparedStatement selSeq = c.prepareStatement("SELECT value FROM sequences WHERE name=?")) {
                selSeq.setString(1, "leave_requests");
                try (ResultSet rs = selSeq.executeQuery()) {
                    current = rs.next() ? rs.getLong(1) : 0;
                }
            }
            List<String> missing = new java.util.ArrayList<>();
            try (Statement st = c.createStatement();
                    ResultSet rs = st.executeQuery(
                            "SELECT id FROM leave_requests WHERE display_id IS NULL ORDER BY start_date")) {
                while (rs.next()) {
                    missing.add(rs.getString(1));
                }
            }
            if (!missing.isEmpty()) {
                try (PreparedStatement upd = c
                        .prepareStatement("UPDATE leave_requests SET display_id=? WHERE id=?")) {
                    for (String id : missing) {
                        current += 1;
                        upd.setLong(1, current);
                        upd.setString(2, id);
                        upd.addBatch();
                    }
                    upd.executeBatch();
                }
                try (PreparedStatement updSeq = c.prepareStatement("UPDATE sequences SET value=? WHERE name=?")) {
                    updSeq.setLong(1, current);
                    updSeq.setString(2, "leave_requests");
                    updSeq.executeUpdate();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
    }
}
