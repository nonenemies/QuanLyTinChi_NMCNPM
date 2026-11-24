package DAO;

import Model.ChuongTrinhDaoTao;
import Model.MonHoc;
import Model.Nganh;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChuongTrinhDaoTaoDAO extends BaseDAO {
    /** Lấy danh sách môn thuộc chương trình đào tạo của một ngành. */
    public List<MonHoc> layMonTheoMaNganh(String maNganh) throws SQLException {
        String sql = "SELECT mh.MaMon, mh.TenMon, mh.SoTinChi, mh.LoaiMon, mh.MoTa, mh.TrangThai "
                + "FROM ChuongTrinhDaoTao ct "
                + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon AND mh.TrangThai = 'HOAT_DONG' "
                + "WHERE ct.MaNganh = ? ORDER BY mh.MaMon";
        List<MonHoc> danhSach = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNganh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new MonHoc(
                            rs.getString("MaMon"),
                            rs.getString("TenMon"),
                            rs.getObject("SoTinChi", Integer.class),
                            rs.getString("LoaiMon"),
                            rs.getString("MoTa"),
                            rs.getString("TrangThai")));
                }
            }
        }
        return danhSach;
    }

    /** Lấy chương trình đào tạo theo mã sinh viên (dựa trên ngành của SV). */
    public List<MonHoc> layMonTheoMaSinhVien(String maSinhVien) throws SQLException {
        String sql = "SELECT mh.MaMon, mh.TenMon, mh.SoTinChi, mh.LoaiMon, mh.MoTa, mh.TrangThai "
                + "FROM SinhVien sv "
                + "JOIN ChuongTrinhDaoTao ct ON ct.MaNganh = sv.MaNganh "
                + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon AND mh.TrangThai = 'HOAT_DONG' "
                + "WHERE sv.MaSinhVien = ? ORDER BY mh.MaMon";
        List<MonHoc> danhSach = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new MonHoc(
                            rs.getString("MaMon"),
                            rs.getString("TenMon"),
                            rs.getObject("SoTinChi", Integer.class),
                            rs.getString("LoaiMon"),
                            rs.getString("MoTa"),
                            rs.getString("TrangThai")));
                }
            }
        }
        return danhSach;
    }

    public boolean dangTrongDotDangKy(Connection conn, String maSinhVien) throws SQLException {
        String sql = "SELECT 1 FROM SinhVien sv "
                + "JOIN DotDangKyNganh dot ON dot.MaNganh = sv.MaNganh "
                + "WHERE sv.MaSinhVien = ? "
                + "AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean monThuocChuongTrinh(Connection conn, String maSinhVien, String maMon)
            throws SQLException {
        String sql = "SELECT 1 FROM SinhVien sv "
                + "JOIN ChuongTrinhDaoTao ct ON ct.MaNganh = sv.MaNganh "
                + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon AND mh.TrangThai = 'HOAT_DONG' "
                + "WHERE sv.MaSinhVien = ? AND ct.MaMon = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            ps.setString(2, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public ChuongTrinhDaoTao layChuongTrinhMonHoc(
            String maSinhVien, String maMon) throws SQLException {
        String sql = "SELECT n.MaNganh, n.TenNganh, ct.MaMon FROM SinhVien sv "
                + "JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN Nganh n ON n.MaNganh = sv.MaNganh "
                + "JOIN ChuongTrinhDaoTao ct ON ct.MaNganh = n.MaNganh "
                + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon AND mh.TrangThai = 'HOAT_DONG' "
                + "WHERE sv.MaSinhVien = ? AND ct.MaMon = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            ps.setString(2, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new ChuongTrinhDaoTao(
                                new Nganh(rs.getString("MaNganh"), rs.getString("TenNganh")),
                                rs.getString("MaMon"))
                        : null;
            }
        }
    }
}
