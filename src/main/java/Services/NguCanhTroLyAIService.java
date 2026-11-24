package Services;

import Model.SinhVien;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Gom toàn bộ dữ liệu các bảng (trừ mật khẩu) để đưa vào ngữ cảnh Trợ lý AI. */
public class NguCanhTroLyAIService {
    private static final int SO_DONG_TOI_DA_MOI_BANG = 500;

    public String xayDungNguCanhDayDu(SinhVien sinhVien) throws SQLException {
        StringBuilder nguCanh = new StringBuilder();
        if (sinhVien != null) {
            nguCanh.append("Sinh viên đang đăng nhập: ")
                    .append(chu(sinhVien.getHoTen()))
                    .append(" (").append(chu(sinhVien.getMaSinhVien())).append(").\n")
                    .append("Khi câu hỏi dùng 'tôi/mình', hãy tra dữ liệu theo mã sinh viên này.\n\n");
        }
        nguCanh.append("=== TOÀN BỘ DỮ LIỆU CÁC BẢNG TRONG HỆ THỐNG ===\n");
        nguCanh.append("(Không gồm cột Password. Dùng dữ liệu này làm nguồn sự kiện cứng.)\n\n");

        try (Connection ketNoi = DBConnection.getConnection()) {
            themBang(nguCanh, ketNoi, "Users",
                    "SELECT UserID, HoTen, Email, VaiTro, MaSo, NgayTao, TrangThai FROM Users ORDER BY UserID");
            themBang(nguCanh, ketNoi, "Nganh",
                    "SELECT MaNganh, TenNganh FROM Nganh ORDER BY MaNganh");
            themBang(nguCanh, ketNoi, "SinhVien",
                    "SELECT sv.MaSinhVien, u.HoTen, u.Email, sv.MaNganh, n.TenNganh, "
                            + "sv.NgaySinh, sv.GioiTinh, sv.Lop, sv.KhoaHoc, sv.HeDaoTao, u.TrangThai "
                            + "FROM SinhVien sv "
                            + "JOIN Users u ON u.UserID = sv.UserID "
                            + "LEFT JOIN Nganh n ON n.MaNganh = sv.MaNganh "
                            + "ORDER BY sv.MaSinhVien");
            themBang(nguCanh, ketNoi, "GiangVien",
                    "SELECT gv.MaGiangVien, u.HoTen, u.Email, gv.ChuyenNganh, gv.HocVi, u.TrangThai "
                            + "FROM GiangVien gv JOIN Users u ON u.UserID = gv.UserID "
                            + "ORDER BY gv.MaGiangVien");
            themBang(nguCanh, ketNoi, "Admin",
                    "SELECT a.UserID, u.HoTen, u.Email, a.ChucVu, u.TrangThai "
                            + "FROM Admin a JOIN Users u ON u.UserID = a.UserID ORDER BY a.UserID");
            themBang(nguCanh, ketNoi, "MonHoc",
                    "SELECT MaMon, TenMon, SoTinChi, LoaiMon, MoTa, TrangThai "
                            + "FROM MonHoc ORDER BY MaMon");
            themBang(nguCanh, ketNoi, "MonTienQuyet",
                    "SELECT mtq.MaMon, mh1.TenMon AS TenMon, mtq.MaMonTienQuyet, "
                            + "mh2.TenMon AS TenMonTienQuyet "
                            + "FROM MonTienQuyet mtq "
                            + "JOIN MonHoc mh1 ON mh1.MaMon = mtq.MaMon "
                            + "JOIN MonHoc mh2 ON mh2.MaMon = mtq.MaMonTienQuyet "
                            + "ORDER BY mtq.MaMon, mtq.MaMonTienQuyet");
            themBang(nguCanh, ketNoi, "CauHinhHocKy",
                    "SELECT HocKy, KetThucHocKy, HanNhapDiem FROM CauHinhHocKy ORDER BY KetThucHocKy");
            themBang(nguCanh, ketNoi, "LopHocPhan",
                    "SELECT lh.MaLop, lh.MaMon, mh.TenMon, lh.MaGiangVien, "
                            + "COALESCE(u.HoTen, 'Chưa phân công') AS TenGiangVien, "
                            + "lh.HocKy, lh.SiSoToiDa, lh.SiSoHienTai, lh.TrangThai "
                            + "FROM LopHocPhan lh "
                            + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                            + "LEFT JOIN GiangVien gv ON gv.MaGiangVien = lh.MaGiangVien "
                            + "LEFT JOIN Users u ON u.UserID = gv.UserID "
                            + "ORDER BY lh.HocKy DESC, lh.MaLop");
            themBang(nguCanh, ketNoi, "ChuongTrinhDaoTao",
                    "SELECT ct.MaNganh, n.TenNganh, ct.MaMon, mh.TenMon, mh.SoTinChi, mh.LoaiMon "
                            + "FROM ChuongTrinhDaoTao ct "
                            + "JOIN Nganh n ON n.MaNganh = ct.MaNganh "
                            + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon "
                            + "ORDER BY ct.MaNganh, ct.MaMon");
            themBang(nguCanh, ketNoi, "DotDangKyNganh",
                    "SELECT d.MaNganh, n.TenNganh, d.MoDangKy, d.DongDangKy, "
                            + "CASE WHEN CURRENT_TIMESTAMP BETWEEN d.MoDangKy AND d.DongDangKy "
                            + "THEN 'DANG_MO' ELSE 'DA_DONG' END AS TrangThaiDot "
                            + "FROM DotDangKyNganh d "
                            + "JOIN Nganh n ON n.MaNganh = d.MaNganh "
                            + "ORDER BY d.MaNganh");
            themBang(nguCanh, ketNoi, "LichHoc",
                    "SELECT lich.MaLich, lich.MaLop, lh.MaMon, mh.TenMon, lh.HocKy, "
                            + "lich.Thu, TIME_FORMAT(lich.GioBatDau, '%H:%i') AS GioBatDau, "
                            + "TIME_FORMAT(lich.GioKetThuc, '%H:%i') AS GioKetThuc, lich.PhongHoc "
                            + "FROM LichHoc lich "
                            + "JOIN LopHocPhan lh ON lh.MaLop = lich.MaLop "
                            + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                            + "ORDER BY lh.HocKy DESC, lich.MaLop, lich.Thu, lich.GioBatDau");
            themBang(nguCanh, ketNoi, "ThanhPhanDanhGia",
                    "SELECT tp.MaThanhPhan, tp.MaMon, mh.TenMon, tp.TenThanhPhan, "
                            + "tp.TrongSo, tp.ThuTu, tp.TrangThai "
                            + "FROM ThanhPhanDanhGia tp "
                            + "JOIN MonHoc mh ON mh.MaMon = tp.MaMon "
                            + "ORDER BY tp.MaMon, tp.ThuTu, tp.MaThanhPhan");
            themBang(nguCanh, ketNoi, "DangKyHoc",
                    "SELECT dk.MaDangKy, dk.MaSinhVien, u.HoTen AS TenSinhVien, dk.MaLop, "
                            + "lh.MaMon, mh.TenMon, lh.HocKy, dk.NgayDangKy, dk.DiemTongKet, dk.TrangThai "
                            + "FROM DangKyHoc dk "
                            + "JOIN SinhVien sv ON sv.MaSinhVien = dk.MaSinhVien "
                            + "JOIN Users u ON u.UserID = sv.UserID "
                            + "JOIN LopHocPhan lh ON lh.MaLop = dk.MaLop "
                            + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                            + "ORDER BY dk.MaSinhVien, lh.HocKy DESC, dk.MaDangKy");
            themBang(nguCanh, ketNoi, "DiemThanhPhan",
                    "SELECT d.MaDiemThanhPhan, d.MaDangKy, dk.MaSinhVien, dk.MaLop, "
                            + "tp.MaMon, tp.TenThanhPhan, tp.TrongSo, d.DiemDatDuoc, d.NgayNhap "
                            + "FROM DiemThanhPhan d "
                            + "JOIN DangKyHoc dk ON dk.MaDangKy = d.MaDangKy "
                            + "JOIN ThanhPhanDanhGia tp ON tp.MaThanhPhan = d.MaThanhPhan "
                            + "ORDER BY d.MaDangKy, tp.ThuTu, d.MaDiemThanhPhan");
            themBang(nguCanh, ketNoi, "YeuCauDoiLop",
                    "SELECT y.MaYeuCau, y.MaSinhVien, u.HoTen AS TenSinhVien, "
                            + "y.LopCu, y.LopMoi, y.LyDo, y.TrangThai, y.NgayTao, y.NgayXuLy, y.NguoiXuLy "
                            + "FROM YeuCauDoiLop y "
                            + "JOIN SinhVien sv ON sv.MaSinhVien = y.MaSinhVien "
                            + "JOIN Users u ON u.UserID = sv.UserID "
                            + "ORDER BY y.NgayTao DESC, y.MaYeuCau DESC");
        }
        return nguCanh.toString();
    }

    private void themBang(StringBuilder nguCanh, Connection ketNoi, String tenBang, String sql)
            throws SQLException {
        nguCanh.append("--- BẢNG ").append(tenBang).append(" ---\n");
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql);
             ResultSet ketQua = lenh.executeQuery()) {
            ResultSetMetaData meta = ketQua.getMetaData();
            int soCot = meta.getColumnCount();
            List<String> tieuDe = new ArrayList<>(soCot);
            for (int i = 1; i <= soCot; i++) {
                tieuDe.add(meta.getColumnLabel(i));
            }
            nguCanh.append(String.join(" | ", tieuDe)).append('\n');

            int soDong = 0;
            boolean daCat = false;
            while (ketQua.next()) {
                if (soDong >= SO_DONG_TOI_DA_MOI_BANG) {
                    daCat = true;
                    break;
                }
                List<String> giaTri = new ArrayList<>(soCot);
                for (int i = 1; i <= soCot; i++) {
                    Object giaTriCot = ketQua.getObject(i);
                    giaTri.add(giaTriCot == null
                            ? "NULL" : String.valueOf(giaTriCot).replace('\n', ' '));
                }
                nguCanh.append(String.join(" | ", giaTri)).append('\n');
                soDong++;
            }
            if (soDong == 0) {
                nguCanh.append("(không có dữ liệu)\n");
            } else if (daCat) {
                nguCanh.append("(... còn nhiều dòng, đã cắt ở ").append(SO_DONG_TOI_DA_MOI_BANG)
                        .append(" dòng)\n");
            }
            nguCanh.append('\n');
        }
    }

    private String chu(String giaTri) {
        return giaTri == null || giaTri.isBlank() ? "chưa rõ" : giaTri;
    }
}
