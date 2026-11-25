package DAO;

import Model.LichHoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Truy vấn các buổi học thuộc lớp học phần. */
public class LichHocDAO extends BaseDAO {
    public List<LichHoc> layTheoMaLop(String maLop) throws SQLException {
        try (Connection conn = getConnection()) {
            return layTheoMaLop(conn, maLop);
        }
    }

    public List<LichHoc> layTheoMaLop(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT MaLich, MaLop, Thu, GioBatDau, GioKetThuc, PhongHoc "
                + "FROM LichHoc WHERE MaLop = ? ORDER BY Thu, GioBatDau, MaLich";
        List<LichHoc> lichHoc = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lichHoc.add(docLichHoc(rs));
                }
            }
        }
        return lichHoc;
    }

    public Map<String, List<LichHoc>> layTheoMaMon(Connection conn, String maMon)
            throws SQLException {
        String sql = "SELECT lich.MaLich, lich.MaLop, lich.Thu, lich.GioBatDau, "
                + "lich.GioKetThuc, lich.PhongHoc "
                + "FROM LichHoc lich JOIN LopHocPhan lh ON lh.MaLop = lich.MaLop "
                + "WHERE lh.MaMon = ? ORDER BY lich.MaLop, lich.Thu, lich.GioBatDau, lich.MaLich";
        Map<String, List<LichHoc>> lichTheoLop = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichHoc lich = docLichHoc(rs);
                    lichTheoLop.computeIfAbsent(lich.getMaLop(), ignored -> new ArrayList<>())
                            .add(lich);
                }
            }
        }
        return lichTheoLop;
    }

    private LichHoc docLichHoc(ResultSet rs) throws SQLException {
        Time gioBatDau = rs.getTime("GioBatDau");
        Time gioKetThuc = rs.getTime("GioKetThuc");
        return new LichHoc(rs.getInt("MaLich"), rs.getString("MaLop"), rs.getString("Thu"),
                gioBatDau == null ? null : gioBatDau.toLocalTime(),
                gioKetThuc == null ? null : gioKetThuc.toLocalTime(),
                rs.getString("PhongHoc"));
    }
}
