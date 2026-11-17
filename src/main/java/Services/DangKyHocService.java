package Services;

import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import DAO.DangKyHocDAO;
import DAO.DotDangKyNganhDAO;
import DAO.LopHocDAO;
import DAO.MonTienQuyetDAO;
import Model.ChuongTrinhDaoTao;
import Model.DotDangKyNganh;
import Model.MonTienQuyet;
import Model.DangKyHoc;
import Model.LopHocDangKy;
import Model.LopHocThoiKhoaBieu;
import DAO.ChuongTrinhDaoTaoDAO;
import Util.DBConnection;

public class DangKyHocService {
    public static final int MIN_TIN_CHI = 11;
    public static final int MAX_TIN_CHI = 26;

    private final DangKyHocDAO dangKyDAO = new DangKyHocDAO();
    private final LopHocDAO lopHocDAO = new LopHocDAO();
    private final ChuongTrinhDaoTaoDAO chuongTrinhDaoTaoDAO = new ChuongTrinhDaoTaoDAO();
    private final DotDangKyNganhDAO dotDangKyNganhDAO = new DotDangKyNganhDAO();

    public String dangKyHoc(String maSV, String maLop) {
        if (maSV == null || maSV.isBlank() || maLop == null || maLop.isBlank()) {
            return "KHONG_HOP_LE";
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String ketQuaKiemTra = kiemTraDieuKienDangKy(conn, maSV, maLop);
                if (!"THANH_CONG".equals(ketQuaKiemTra)) {
                    return rollbackAndReturn(conn, ketQuaKiemTra);
                }

                if (!lopHocDAO.tangSiSo(conn, maLop)) {
                    return rollbackAndReturn(conn, "LOP_KHONG_HOAT_DONG");
                }

                DangKyHoc dk = new DangKyHoc(maLop, maSV);
                if (!dangKyDAO.themDangKyMoi(conn, dk)) {
                    return rollbackAndReturn(conn, "KHONG_HOP_LE");
                }

                conn.commit();
                return "THANH_CONG";

            } catch (SQLException e) {
                boolean rollbackFailed = false;
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                    rollbackFailed = true;
                }
                if (!rollbackFailed && e.getErrorCode() == 1062) {
                    return "DA_DANG_KY_MON_TRONG_HOC_KY";
                }
                throw e;
            } catch (RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể xử lý đăng ký học.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể xử lý đăng ký học.", e);
        }
    }

    private String kiemTraDieuKienDangKy(Connection conn, String maSV, String maLop)
            throws SQLException {
        if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSV)) {
            return "SINH_VIEN_KHONG_HOAT_DONG";
        }

        var lopHoc = lopHocDAO.layLopTheoMaForUpdate(conn, maLop);
        if (lopHoc == null || !"HOAT_DONG".equals(lopHoc.getTrangThai())) {
            return "LOP_KHONG_HOAT_DONG";
        }
        DotDangKyNganh dotDangKy = dotDangKyNganhDAO.layDotDangKyHienTai(conn, maSV);
        if (dotDangKy == null) {
            return "DOT_DANG_KY_DA_DONG";
        }
        ChuongTrinhDaoTao chuongTrinh = chuongTrinhDaoTaoDAO.layChuongTrinhMonHoc(
                conn, maSV, lopHoc.getMaMon());
        if (chuongTrinh == null) {
            return "KHONG_HOP_LE";
        }
        if (dangKyDAO.kiemTraDaDangKy(conn, maSV, maLop)
                || dangKyDAO.daDangKyMonTrongHocKy(conn, maSV, lopHoc.getMaMon(), lopHoc.getHocKy())) {
            return "DA_DANG_KY_MON_TRONG_HOC_KY";
        }
        if (dangKyDAO.daDatMon(conn, maSV, lopHoc.getMaMon())) {
            return "DA_QUA_MON";
        }
        if (!dangKyDAO.coLichHoc(conn, maLop)) {
            return "LOP_CHUA_XEP_LICH";
        }
        if (dangKyDAO.coLopBiTrungLich(conn, maSV, maLop)) {
            return "TRUNG_LICH";
        }
        for (MonTienQuyet prerequisite
                : MonTienQuyetDAO.layDanhSachMonTienQuyet(conn, lopHoc.getMaMon())) {
            if (!"HOAT_DONG".equals(prerequisite.getMonTienQuyet().getTrangThai())
                    || !dangKyDAO.kiemTraDaQuaMon(conn, maSV,
                            prerequisite.getMaMonTienQuyet())) {
                return "THIEU_MON_TIEN_QUYET";
            }
        }

        int credits = dangKyDAO.laySoTinChi(conn, lopHoc.getMaMon());
        if (credits <= 0 || dangKyDAO.tongTinChiDangHoc(conn, maSV, lopHoc.getHocKy()) + credits
                > MAX_TIN_CHI) {
            return "VUOT_QUA_TIN_CHI";
        }
        return "THANH_CONG";
    }

    public boolean huyDangKyHoc(String maSV, String maLop) {
        if (maSV == null || maSV.isBlank() || maLop == null || maLop.isBlank()) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, false);
                }
                if (lopHocDAO.layLopTheoMaForUpdate(conn, maLop) == null) {
                    return rollbackAndReturn(conn, false);
                }

                var lopHoc = lopHocDAO.layLopTheoMa(conn, maLop);
                if (lopHoc == null || dotDangKyNganhDAO.layDotDangKyHienTai(conn, maSV) == null) {
                    return rollbackAndReturn(conn, false);
                }
                String trangThai = dangKyDAO.layTrangThaiDangKyForUpdate(conn, maSV, maLop);
                if (trangThai == null || "HUY".equals(trangThai)) {
                    return rollbackAndReturn(conn, false);
                }
                if (!"DANG_HOC".equals(trangThai)
                        || dangKyDAO.coDiem(conn, maSV, maLop)) {
                    return rollbackAndReturn(conn, false);
                }
                if (!dangKyDAO.huyDangKy(conn, maSV, maLop)) {
                    return rollbackAndReturn(conn, false);
                }

                if (!lopHocDAO.giamSiSo(conn, maLop)) {
                    return rollbackAndReturn(conn, false);
                }

                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể hủy đăng ký lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể hủy đăng ký lớp.", e);
        }
    }

    public List<LopHocThoiKhoaBieu> xemLichHocTheoTuan(String maSV) {
        if (maSV == null || maSV.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
        }
        try {
            return dangKyDAO.layLichHocTheoTuan(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải thời khóa biểu.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải thời khóa biểu.", e);
        }
    }

    public List<LopHocDangKy> xemDanhSachLopDangKy(String maSV) {
        try {
            return lopHocDAO.layDanhSachLopDangKy(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách lớp đăng ký.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải danh sách lớp đăng ký.", e);
        }
    }

    public List<LopHocDangKy> xemDanhSachLopDaDangKy(String maSV) {
        if (maSV == null || maSV.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
        }
        try {
            return lopHocDAO.layDanhSachLopDaDangKy(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách lớp đã đăng ký.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải danh sách lớp đã đăng ký.", e);
        }
    }

    public int layTongTinChiDangKy(String maSV, String hocKy) {
        if (maSV == null || maSV.isBlank() || hocKy == null || hocKy.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên và học kỳ là bắt buộc.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            return dangKyDAO.tongTinChiDangHoc(conn, maSV, hocKy);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải tổng tín chỉ của học kỳ.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải tổng tín chỉ của học kỳ.", e);
        }
    }

    private <T> T rollbackAndReturn(Connection conn, T result) throws SQLException {
        conn.rollback();
        return result;
    }
}