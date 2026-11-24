package Services;

import DAO.ChuongTrinhDaoTaoDAO;
import DAO.DangKyHocDAO;
import DAO.DotDangKyNganhDAO;
import DAO.LopHocPhanDAO;
import DAO.MonTienQuyetDAO;
import DAO.YeuCauDoiLopDAO;
import Model.LopHocPhan;
import Model.MonTienQuyet;
import Model.YeuCauDoiLop;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YeuCauDoiLopService {
    private static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    private static final String DA_DUYET = "DA_DUYET";
    private static final String HOAT_DONG = "HOAT_DONG";

    private final YeuCauDoiLopDAO yeuCauDAO = new YeuCauDoiLopDAO();
    private final DangKyHocDAO dangKyDAO = new DangKyHocDAO();
    private final LopHocPhanDAO lopHocPhanDAO = new LopHocPhanDAO();
    private final ChuongTrinhDaoTaoDAO chuongTrinhDAO = new ChuongTrinhDaoTaoDAO();

    public List<YeuCauDoiLop> moSanGiaoDich() {
        try {
            yeuCauDAO.huyYeuCauHetHan();
            return yeuCauDAO.laySanGiaoDich();
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải sàn giao dịch đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể tải sàn giao dịch đổi lớp.", e);
        }
    }

    public List<YeuCauDoiLop> xemYeuCauCuaSinhVien(String maSinhVien) {
        if (maSinhVien == null || maSinhVien.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
        }
        try {
            yeuCauDAO.huyYeuCauHetHan();
            return yeuCauDAO.layYeuCauCuaSinhVien(maSinhVien);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải lịch sử đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể tải lịch sử đổi lớp.", e);
        }
    }

    public boolean dangBaiDoiLop(String maSinhVien, String lopCu, String lopMoi, String lyDo) {
        if (maSinhVien == null || maSinhVien.isBlank() || lopCu == null || lopCu.isBlank()
                || lopMoi == null || lopMoi.isBlank() || lopCu.equals(lopMoi)
                || (lyDo != null && lyDo.length() > 200)) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                yeuCauDAO.huyYeuCauHetHan(conn);
                if (!dangKyDAO.sinhVienConHoatDong(conn, maSinhVien)
                        || !chuongTrinhDAO.dangTrongDotDangKy(conn, maSinhVien)) {
                    return hoanTacVaTraVe(conn, false);
                }

                List<String> maLopTheoThuTu = sapXepMaLop(lopCu, lopMoi);
                LopHocPhan lopThuNhat = lopHocPhanDAO.layLopTheoMaForUpdate(conn, maLopTheoThuTu.get(0));
                LopHocPhan lopThuHai = lopHocPhanDAO.layLopTheoMaForUpdate(conn, maLopTheoThuTu.get(1));
                if (lopThuNhat == null || lopThuHai == null) {
                    return hoanTacVaTraVe(conn, false);
                }
                LopHocPhan lopDangHoc = lopCu.equals(lopThuNhat.getMaLop()) ? lopThuNhat : lopThuHai;
                LopHocPhan lopMuonVao = lopMoi.equals(lopThuNhat.getMaLop()) ? lopThuNhat : lopThuHai;

                if (lopDangHoc == null || lopMuonVao == null
                        || !HOAT_DONG.equals(lopDangHoc.getTrangThai())
                        || !HOAT_DONG.equals(lopMuonVao.getTrangThai())
                        || !Objects.equals(lopDangHoc.getHocKy(), lopMuonVao.getHocKy())
                        || !dangKyDAO.dangHocChuaCoDiem(conn, maSinhVien, lopCu)
                        || !chuongTrinhDAO.monThuocChuongTrinh(
                                conn, maSinhVien, lopMuonVao.getMaMon())
                        || dangKyDAO.kiemTraDaDangKy(conn, maSinhVien, lopMoi)
                        || dangKyDAO.daDatMon(conn, maSinhVien, lopMuonVao.getMaMon())
                        || dangKyDAO.daDangKyMonKhacTrongHocKy(conn, maSinhVien,
                                lopMuonVao.getMaMon(), lopDangHoc.getHocKy(), lopCu)
                        || yeuCauDAO.coYeuCauChoXacNhan(conn, maSinhVien, lopCu, lopMoi)
                        || !dangKyDAO.coLichHoc(conn, lopCu)
                        || !dangKyDAO.coLichHoc(conn, lopMoi)
                        || dangKyDAO.coLopBiTrungLich(conn, maSinhVien, lopMoi, lopCu)
                        || !coDuMonTienQuyet(conn, maSinhVien, lopMuonVao.getMaMon())) {
                    return hoanTacVaTraVe(conn, false);
                }

                int tinChiLopCu = dangKyDAO.laySoTinChiLop(conn, lopCu);
                int tinChiLopMoi = dangKyDAO.laySoTinChiLop(conn, lopMoi);
                int tinChiDuKien = dangKyDAO.tongTinChiDangHoc(
                        conn, maSinhVien, lopDangHoc.getHocKy()) - tinChiLopCu + tinChiLopMoi;
                if (tinChiLopCu <= 0 || tinChiLopMoi <= 0
                        || tinChiDuKien > DangKyHocService.MAX_TIN_CHI) {
                    return hoanTacVaTraVe(conn, false);
                }

                if (!yeuCauDAO.themYeuCau(
                        conn, new YeuCauDoiLop(maSinhVien, lopCu, lopMoi, lyDo))) {
                    return hoanTacVaTraVe(conn, false);
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                hoanTac(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể đăng yêu cầu đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể đăng yêu cầu đổi lớp.", e);
        }
    }

    public boolean xacNhanDoiLop(int maYeuCau, String maSinhVienNhan) {
        if (maYeuCau <= 0 || maSinhVienNhan == null || maSinhVienNhan.isBlank()) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                yeuCauDAO.huyYeuCauHetHan(conn);
                YeuCauDoiLop yeuCau = yeuCauDAO.layYeuCauTheoMaDeKhoa(conn, maYeuCau);
                if (yeuCau == null || !CHO_XAC_NHAN.equals(yeuCau.getTrangThai())
                        || maSinhVienNhan.equals(yeuCau.getMaSinhVien())) {
                    return hoanTacVaTraVe(conn, false);
                }

                String maSinhVienDangTin = yeuCau.getMaSinhVien();
                for (String maSinhVien : sapXepMaSinhVien(maSinhVienDangTin, maSinhVienNhan)) {
                    if (!dangKyDAO.sinhVienConHoatDong(conn, maSinhVien)
                            || !chuongTrinhDAO.dangTrongDotDangKy(conn, maSinhVien)) {
                        return hoanTacVaTraVe(conn, false);
                    }
                }

                List<String> maLopTheoThuTu = sapXepMaLop(yeuCau.getLopCu(), yeuCau.getLopMoi());
                LopHocPhan lopThuNhat = lopHocPhanDAO.layLopTheoMaForUpdate(conn, maLopTheoThuTu.get(0));
                LopHocPhan lopThuHai = lopHocPhanDAO.layLopTheoMaForUpdate(conn, maLopTheoThuTu.get(1));
                if (lopThuNhat == null || lopThuHai == null) {
                    return hoanTacVaTraVe(conn, false);
                }
                LopHocPhan lopCu = yeuCau.getLopCu().equals(lopThuNhat.getMaLop())
                        ? lopThuNhat : lopThuHai;
                LopHocPhan lopMoi = yeuCau.getLopMoi().equals(lopThuNhat.getMaLop())
                        ? lopThuNhat : lopThuHai;

                for (String maSinhVien : sapXepMaSinhVien(maSinhVienDangTin, maSinhVienNhan)) {
                    String maLopDangHoc = maSinhVien.equals(maSinhVienDangTin)
                            ? yeuCau.getLopCu() : yeuCau.getLopMoi();
                    if (!dangKyDAO.dangHocChuaCoDiem(conn, maSinhVien, maLopDangHoc)) {
                        return hoanTacVaTraVe(conn, false);
                    }
                }

                if (!HOAT_DONG.equals(lopCu.getTrangThai()) || !HOAT_DONG.equals(lopMoi.getTrangThai())
                        || !Objects.equals(lopCu.getHocKy(), lopMoi.getHocKy())
                        || yeuCauDAO.coYeuCauKhacChoXacNhan(conn, maSinhVienNhan, maYeuCau,
                                yeuCau.getLopCu(), yeuCau.getLopMoi())
                        || !duDieuKienNhanLop(conn, maSinhVienDangTin, lopCu, lopMoi)
                        || !duDieuKienNhanLop(conn, maSinhVienNhan, lopMoi, lopCu)) {
                    return hoanTacVaTraVe(conn, false);
                }

                boolean daDoiSinhVienDangTin = dangKyDAO.capNhatLopChoSinhVien(
                        conn, maSinhVienDangTin, yeuCau.getLopCu(), yeuCau.getLopMoi());
                boolean daDoiSinhVienNhan = daDoiSinhVienDangTin
                        && dangKyDAO.capNhatLopChoSinhVien(
                                conn, maSinhVienNhan, yeuCau.getLopMoi(), yeuCau.getLopCu());
                boolean daDuyet = daDoiSinhVienNhan
                        && yeuCauDAO.capNhatTrangThaiDuyet(
                                conn, maYeuCau, maSinhVienNhan, DA_DUYET);
                if (!daDuyet) {
                    return hoanTacVaTraVe(conn, false);
                }

                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                hoanTac(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể xử lý yêu cầu đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể xử lý yêu cầu đổi lớp.", e);
        }
    }

    private boolean duDieuKienNhanLop(
            Connection conn, String maSinhVien, LopHocPhan lopRoi, LopHocPhan lopNhan)
            throws SQLException {
        String maLopRoi = lopRoi.getMaLop();
        String maLopNhan = lopNhan.getMaLop();
        String maMonNhan = lopNhan.getMaMon();
        String hocKy = lopRoi.getHocKy();
        return chuongTrinhDAO.monThuocChuongTrinh(conn, maSinhVien, maMonNhan)
                && !dangKyDAO.kiemTraDaDangKy(conn, maSinhVien, maLopNhan)
                && !dangKyDAO.daDatMon(conn, maSinhVien, maMonNhan)
                && !dangKyDAO.daDangKyMonKhacTrongHocKy(
                        conn, maSinhVien, maMonNhan, hocKy, maLopRoi)
                && dangKyDAO.coLichHoc(conn, maLopNhan)
                && !dangKyDAO.coLopBiTrungLich(conn, maSinhVien, maLopNhan, maLopRoi)
                && coDuMonTienQuyet(conn, maSinhVien, maMonNhan)
                && duTinChiSauKhiDoi(conn, maSinhVien, lopRoi, lopNhan);
    }

    private boolean duTinChiSauKhiDoi(
            Connection conn, String maSinhVien, LopHocPhan lopRoi, LopHocPhan lopNhan)
            throws SQLException {
        int tinChiLopRoi = dangKyDAO.laySoTinChiLop(conn, lopRoi.getMaLop());
        int tinChiLopNhan = dangKyDAO.laySoTinChiLop(conn, lopNhan.getMaLop());
        int tinChiDuKien = dangKyDAO.tongTinChiDangHoc(
                conn, maSinhVien, lopRoi.getHocKy()) - tinChiLopRoi + tinChiLopNhan;
        return tinChiLopRoi > 0 && tinChiLopNhan > 0
                && tinChiDuKien <= DangKyHocService.MAX_TIN_CHI;
    }

    private boolean coDuMonTienQuyet(Connection conn, String maSinhVien, String maMon)
            throws SQLException {
        for (MonTienQuyet monTienQuyet : MonTienQuyetDAO.layDanhSachMonTienQuyet(conn, maMon)) {
            if (!HOAT_DONG.equals(monTienQuyet.getMonTienQuyet().getTrangThai())
                    || !dangKyDAO.kiemTraDaQuaMon(
                            conn, maSinhVien, monTienQuyet.getMaMonTienQuyet())) {
                return false;
            }
        }
        return true;
    }

    public boolean huyYeuCau(int maYeuCau, String maSinhVien) {
        if (maSinhVien == null || maSinhVien.isBlank() || maYeuCau <= 0) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                yeuCauDAO.huyYeuCauHetHan(conn);
                YeuCauDoiLop yeuCau = yeuCauDAO.layYeuCauTheoMaDeKhoa(conn, maYeuCau);
                if (yeuCau == null || !CHO_XAC_NHAN.equals(yeuCau.getTrangThai())
                        || !maSinhVien.equals(yeuCau.getMaSinhVien())
                        || !dangKyDAO.sinhVienConHoatDong(conn, maSinhVien)
                        || !chuongTrinhDAO.dangTrongDotDangKy(conn, maSinhVien)
                        || !dangKyDAO.dangHocChuaCoDiem(conn, maSinhVien, yeuCau.getLopCu())) {
                    return hoanTacVaTraVe(conn, false);
                }
                if (!yeuCauDAO.huyYeuCauCuaSinhVien(conn, maSinhVien, maYeuCau)) {
                    return hoanTacVaTraVe(conn, false);
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                hoanTac(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể hủy yêu cầu đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.chuyenThanhLoiDichVu("Không thể hủy yêu cầu đổi lớp.", e);
        }
    }

    private List<String> sapXepMaSinhVien(String maSinhVienMot, String maSinhVienHai) {
        List<String> maSinhVien = new ArrayList<>(List.of(maSinhVienMot, maSinhVienHai));
        maSinhVien.sort(String::compareTo);
        return maSinhVien;
    }

    private List<String> sapXepMaLop(String lopMot, String lopHai) {
        List<String> maLop = new ArrayList<>(List.of(lopMot, lopHai));
        maLop.sort(String::compareTo);
        return maLop;
    }

    private <T> T hoanTacVaTraVe(Connection ketNoi, T ketQua) throws SQLException {
        ketNoi.rollback();
        return ketQua;
    }

    private void hoanTac(Connection ketNoi, Exception loi) {
        try {
            ketNoi.rollback();
        } catch (SQLException loiHoanTac) {
            loi.addSuppressed(loiHoanTac);
        }
    }
}
