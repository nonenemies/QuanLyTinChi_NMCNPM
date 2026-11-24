package DAO;

import Model.YeuCauDoiLop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class YeuCauDoiLopDAO extends BaseDAO {

    public int huyYeuCauHetHan(Connection conn) throws SQLException {
        String sql = "UPDATE YeuCauDoiLop yc SET yc.TrangThai = 'HUY', "
                + "yc.NgayXuLy = CURRENT_TIMESTAMP "
                + "WHERE yc.TrangThai = 'CHO_XAC_NHAN' AND NOT EXISTS ("
                + "SELECT 1 FROM SinhVien sv "
                + "JOIN DotDangKyNganh dot ON dot.MaNganh = sv.MaNganh "
                + "WHERE sv.MaSinhVien = yc.MaSinhVien "
                + "AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    public int huyYeuCauHetHan() throws SQLException {
        try (Connection conn = getConnection()) {
            return huyYeuCauHetHan(conn);
        }
    }

    public boolean coYeuCauChoXacNhan(
            Connection conn, String maSinhVien, String lopCu, String lopMoi) throws SQLException {
        String sql = "SELECT 1 FROM YeuCauDoiLop WHERE MaSinhVien = ? "
                + "AND TrangThai = 'CHO_XAC_NHAN' "
                + "AND (LopCu IN (?, ?) OR LopMoi IN (?, ?)) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            ps.setString(2, lopCu);
            ps.setString(3, lopMoi);
            ps.setString(4, lopCu);
            ps.setString(5, lopMoi);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean coYeuCauKhacChoXacNhan(
            Connection conn, String maSinhVien, int maYeuCau,
            String lopCu, String lopMoi) throws SQLException {
        String sql = "SELECT 1 FROM YeuCauDoiLop WHERE MaSinhVien = ? "
                + "AND MaYeuCau <> ? AND TrangThai = 'CHO_XAC_NHAN' "
                + "AND (LopCu IN (?, ?) OR LopMoi IN (?, ?)) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            ps.setInt(2, maYeuCau);
            ps.setString(3, lopCu);
            ps.setString(4, lopMoi);
            ps.setString(5, lopCu);
            ps.setString(6, lopMoi);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean themYeuCau(Connection conn, YeuCauDoiLop yeuCau) throws SQLException {
        String sql = "INSERT INTO YeuCauDoiLop (MaSinhVien, LopCu, LopMoi, LyDo) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yeuCau.getMaSinhVien());
            ps.setString(2, yeuCau.getLopCu());
            ps.setString(3, yeuCau.getLopMoi());
            ps.setString(4, yeuCau.getLyDo());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean capNhatTrangThaiDuyet(
            Connection conn, int maYeuCau, String maSinhVienXuLy, String trangThai)
            throws SQLException {
        String sql = "UPDATE YeuCauDoiLop yc "
                + "JOIN SinhVien sv ON sv.MaSinhVien = ? "
                + "SET yc.TrangThai = ?, yc.NguoiXuLy = sv.UserID, "
                + "yc.NgayXuLy = CURRENT_TIMESTAMP "
                + "WHERE yc.MaYeuCau = ? AND yc.TrangThai = 'CHO_XAC_NHAN'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVienXuLy);
            ps.setString(2, trangThai);
            ps.setInt(3, maYeuCau);
            return ps.executeUpdate() == 1;
        }
    }

    public YeuCauDoiLop layYeuCauTheoMaDeKhoa(Connection conn, int maYeuCau) throws SQLException {
        String sql = "SELECT * FROM YeuCauDoiLop WHERE MaYeuCau = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docYeuCau(rs, false) : null;
            }
        }
    }

    public List<YeuCauDoiLop> laySanGiaoDich() throws SQLException {
        String sql = sqlThongTinYeuCau()
                + " JOIN SinhVien svDot ON svDot.MaSinhVien = yc.MaSinhVien "
                + "JOIN DotDangKyNganh dot ON dot.MaNganh = svDot.MaNganh "
                + "WHERE yc.TrangThai = 'CHO_XAC_NHAN' "
                + "AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy "
                + "ORDER BY yc.NgayTao DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return docDanhSach(rs);
        }
    }

    public List<YeuCauDoiLop> layYeuCauCuaSinhVien(String maSinhVien) throws SQLException {
        String sql = sqlThongTinYeuCau()
                + " WHERE yc.MaSinhVien = ? ORDER BY yc.NgayTao DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSinhVien);
            try (ResultSet rs = ps.executeQuery()) {
                return docDanhSach(rs);
            }
        }
    }

    public boolean huyYeuCauCuaSinhVien(Connection conn, String maSinhVien, int maYeuCau)
            throws SQLException {
        String sql = "UPDATE YeuCauDoiLop SET TrangThai = 'HUY', NgayXuLy = CURRENT_TIMESTAMP "
                + "WHERE MaYeuCau = ? AND MaSinhVien = ? AND TrangThai = 'CHO_XAC_NHAN'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            ps.setString(2, maSinhVien);
            return ps.executeUpdate() == 1;
        }
    }

    private String sqlThongTinYeuCau() {
        return "SELECT yc.*, COALESCE(u.HoTen, yc.MaSinhVien) AS TenSinhVien, "
                + "CONCAT(yc.LopCu, ' · ', COALESCE(mhCu.MaMon, ''), ' - ', "
                + "COALESCE(mhCu.TenMon, ''), ' · HK ', COALESCE(lhCu.HocKy, '')) AS TenLopCu, "
                + "CONCAT(yc.LopMoi, ' · ', COALESCE(mhMoi.MaMon, ''), ' - ', "
                + "COALESCE(mhMoi.TenMon, ''), ' · HK ', COALESCE(lhMoi.HocKy, '')) AS TenLopMoi "
                + "FROM YeuCauDoiLop yc "
                + "LEFT JOIN SinhVien sv ON sv.MaSinhVien = yc.MaSinhVien "
                + "LEFT JOIN Users u ON u.UserID = sv.UserID "
                + "LEFT JOIN LopHocPhan lhCu ON lhCu.MaLop = yc.LopCu "
                + "LEFT JOIN MonHoc mhCu ON mhCu.MaMon = lhCu.MaMon "
                + "LEFT JOIN LopHocPhan lhMoi ON lhMoi.MaLop = yc.LopMoi "
                + "LEFT JOIN MonHoc mhMoi ON mhMoi.MaMon = lhMoi.MaMon";
    }

    private List<YeuCauDoiLop> docDanhSach(ResultSet rs) throws SQLException {
        List<YeuCauDoiLop> danhSach = new ArrayList<>();
        while (rs.next()) {
            danhSach.add(docYeuCau(rs, true));
        }
        return danhSach;
    }

    private YeuCauDoiLop docYeuCau(ResultSet rs, boolean coThongTinHienThi) throws SQLException {
        YeuCauDoiLop yeuCau = new YeuCauDoiLop();
        yeuCau.setMaYeuCau(rs.getInt("MaYeuCau"));
        yeuCau.setMaSinhVien(rs.getString("MaSinhVien"));
        yeuCau.setLopCu(rs.getString("LopCu"));
        yeuCau.setLopMoi(rs.getString("LopMoi"));
        yeuCau.setTrangThai(rs.getString("TrangThai"));
        yeuCau.setLyDo(rs.getString("LyDo"));
        yeuCau.setNgayTao(rs.getTimestamp("NgayTao"));
        yeuCau.setNgayXuLy(rs.getTimestamp("NgayXuLy"));
        int nguoiXuLy = rs.getInt("NguoiXuLy");
        yeuCau.setNguoiXuLy(rs.wasNull() ? null : nguoiXuLy);
        if (coThongTinHienThi) {
            yeuCau.setTenSinhVien(rs.getString("TenSinhVien"));
            yeuCau.setTenLopCu(rs.getString("TenLopCu"));
            yeuCau.setTenLopMoi(rs.getString("TenLopMoi"));
        }
        return yeuCau;
    }
}
