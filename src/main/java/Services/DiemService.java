package Services;

import DAO.DiemDAO;
import DAO.LopHocDAO;
import Model.BangDiemSinhVien;
import Model.KetQuaHocTap;
import Model.GiangVien;
import Model.LopHocPhan;
import Model.SinhVien;
import Model.ThanhPhanDanhGia;
import java.sql.SQLException;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiemService {
    private static final Pattern YEAR_TERM = Pattern.compile("^(\\d{4})-(\\d+)$");
    private final DiemDAO diemDAO = new DiemDAO();
    private final LopHocDAO lopHocDAO = new LopHocDAO();

    public List<KetQuaHocTap> layKetQuaHocTap(SinhVien sinhVien) {
        if (sinhVien == null || !"SINHVIEN".equalsIgnoreCase(sinhVien.getRole())
                || sinhVien.getMaSinhVien() == null || sinhVien.getMaSinhVien().isBlank()
                || !"HOAT_DONG".equals(sinhVien.getTrangThai())) {
            throw new ServiceException("Tài khoản sinh viên không hợp lệ hoặc đã ngừng hoạt động.");
        }

        try {
            return diemDAO.layKetQuaHocTap( sinhVien.getMaSinhVien());
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải bảng điểm tổng kết.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể tải bảng điểm tổng kết.", e);
        }
    }

    /** GPA tích lũy dùng điểm cao nhất của mỗi môn, tính đến học kỳ được yêu cầu. */
    public double tinhGpaTichLuy(List<KetQuaHocTap> results, String denHocKy) {
        Map<String, KetQuaHocTap> bestAttemptByCourse = new HashMap<>();
        for (KetQuaHocTap attempt : results) {
            if ((denHocKy != null && soSanhHocKy(attempt.getHocKy(), denHocKy) > 0)
                    || attempt.getDiemTongKet() == null
                    || attempt.getSoTinChi() == null
                    || attempt.getSoTinChi() <= 0) {
                continue;
            }
            bestAttemptByCourse.merge(attempt.getMaMon(), attempt,
                    (current, candidate) -> candidate.getDiemTongKet() > current.getDiemTongKet()
                            ? candidate : current);
        }

        double weightedScore = 0;
        int credits = 0;
        for (KetQuaHocTap attempt : bestAttemptByCourse.values()) {
            weightedScore += attempt.getDiemTongKet() * attempt.getSoTinChi();
            credits += attempt.getSoTinChi();
        }
        return credits == 0 ? Double.NaN : weightedScore / credits;
    }

    private int soSanhHocKy(String first, String second) {
        Matcher firstMatch = YEAR_TERM.matcher(first == null ? "" : first);
        Matcher secondMatch = YEAR_TERM.matcher(second == null ? "" : second);
        if (firstMatch.matches() && secondMatch.matches()) {
            int yearComparison = Integer.compare(
                    Integer.parseInt(firstMatch.group(1)), Integer.parseInt(secondMatch.group(1)));
            if (yearComparison != 0) return yearComparison;
            return Integer.compare(Integer.parseInt(firstMatch.group(2)), Integer.parseInt(secondMatch.group(2)));
        }
        return Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER).compare(first, second);
    }

    public List<LopHocPhan> layLopPhuTrach(GiangVien giangVien) {
        kiemTraGiangVien(giangVien);
        try {
            return lopHocDAO.layDanhSachLopCuaGiangVien(giangVien.getMaGiangVien());
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách lớp phụ trách.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể tải danh sách lớp phụ trách.", e);
        }
    }

    public List<BangDiemSinhVien> layBangDiem(GiangVien giangVien, String maLop) {
        kiemTraGiangVien(giangVien);
        try {
            kiemTraPhuTrach(giangVien, maLop);
            return diemDAO.layBangDiemTheoLop( maLop);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải bảng điểm lớp học phần.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể tải bảng điểm lớp học phần.", e);
        }
    }

    /**
     * Lưu mới hoặc cập nhật điểm thành phần, sau đó tính lại điểm tổng kết nếu đã đủ điểm.
     * Kết quả nghiệp vụ không hợp lệ được trả về false; lỗi cơ sở dữ liệu được ném lên caller.
     */
    public boolean nhapDiemVaTinhTongKet(GiangVien giangVien, String maSinhVien, String maLop,
                                        ThanhPhanDanhGia thanhPhan, float diemDatDuoc) {
        kiemTraGiangVien(giangVien);
        if (maSinhVien == null || maSinhVien.isBlank() || maLop == null || maLop.isBlank()
                || thanhPhan == null || thanhPhan.getMaTP() <= 0
                || !Float.isFinite(diemDatDuoc) || diemDatDuoc < 0 || diemDatDuoc > 10) {
            return false;
        }

        try {
            kiemTraPhuTrach(giangVien, maLop);
            if (!thanhPhan.getTrangThai()) return false;
            if (!diemDAO.nhapDiem(maSinhVien, maLop, thanhPhan.getMaTP(), diemDatDuoc)) {
                return false;
            }
            diemDAO.capNhatDiemTongKet(maSinhVien, maLop);
            return true;
        } catch (SQLException e) {
            throw new ServiceException("Không thể lưu điểm và cập nhật điểm tổng kết.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể lưu điểm và cập nhật điểm tổng kết.", e);
        }
    }

    private void kiemTraPhuTrach(GiangVien giangVien, String maLop) throws SQLException {
        if (maLop == null || maLop.isBlank()) {
            throw new ServiceException("Mã lớp không hợp lệ.");
        }
        if (!diemDAO.giangVienConHoatDong( giangVien.getMaGiangVien())) {
            throw new ServiceException("Tài khoản giảng viên không còn hoạt động.");
        }
        LopHocPhan lopHoc = lopHocDAO.layLopTheoMa( maLop);
        if (lopHoc == null || !giangVien.getMaGiangVien().equals(lopHoc.getMaGiangVien())) {
            throw new ServiceException("Bạn không được phân công phụ trách lớp này.");
        }
    }

    private void kiemTraGiangVien(GiangVien giangVien) {
        if (giangVien == null || !"GIAOVIEN".equalsIgnoreCase(giangVien.getRole())
                || giangVien.getMaGiangVien() == null || giangVien.getMaGiangVien().isBlank()
                || !"HOAT_DONG".equals(giangVien.getTrangThai())) {
            throw new ServiceException("Tài khoản giảng viên không hợp lệ hoặc đã ngừng hoạt động.");
        }
    }
}
