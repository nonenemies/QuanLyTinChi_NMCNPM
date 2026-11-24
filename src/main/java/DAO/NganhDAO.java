package DAO;

import Model.Nganh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NganhDAO extends BaseDAO {
    public boolean tonTai(String maNganh) throws SQLException {
        String sql = "SELECT 1 FROM Nganh WHERE MaNganh = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNganh);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Nganh> layTatCa() throws SQLException {
        String sql = "SELECT MaNganh, TenNganh FROM Nganh ORDER BY MaNganh";
        List<Nganh> danhSach = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new Nganh(rs.getString("MaNganh"), rs.getString("TenNganh")));
            }
        }
        return danhSach;
    }
}
