package Controllers;

import DAO.LopHocPhanDAO;
import Model.LopHocDangKy;
import Model.SinhVien;
import Model.Users;
import Model.YeuCauDoiLop;
import Services.ServiceException;
import Services.YeuCauDoiLopService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoiLopController {
    private static final Logger NHAT_KY = Logger.getLogger(DoiLopController.class.getName());
    private final YeuCauDoiLopService dichVuDoiLop = new YeuCauDoiLopService();
    private final LopHocPhanDAO lopHocPhanDAO = new LopHocPhanDAO();
    private final ObservableList<YeuCauDoiLop> danhSachSan = FXCollections.observableArrayList();
    private final FilteredList<YeuCauDoiLop> danhSachSanDaLoc =
            new FilteredList<>(danhSachSan, muc -> true);
    private SinhVien sinhVien;

    @FXML private Label lblTrangThai;
    @FXML private Label lblSanTrong;
    @FXML private Label lblCuaToiTrong;
    @FXML private ComboBox<LopHocDangKy> cmbLopDangHoc;
    @FXML private ComboBox<LopHocDangKy> cmbLopMuonVao;
    @FXML private TextArea txtLyDo;
    @FXML private TextField txtTimKiem;
    @FXML private TableView<YeuCauDoiLop> tblSanGiaoDich;
    @FXML private TableView<YeuCauDoiLop> tblYeuCauCuaToi;
    @FXML private TableColumn<YeuCauDoiLop, Integer> cotMaSan;
    @FXML private TableColumn<YeuCauDoiLop, String> cotSinhVienSan;
    @FXML private TableColumn<YeuCauDoiLop, String> cotLopRoiSan;
    @FXML private TableColumn<YeuCauDoiLop, String> cotLopVaoSan;
    @FXML private TableColumn<YeuCauDoiLop, String> cotLyDoSan;
    @FXML private TableColumn<YeuCauDoiLop, String> cotNgaySan;
    @FXML private TableColumn<YeuCauDoiLop, Integer> cotMaCuaToi;
    @FXML private TableColumn<YeuCauDoiLop, String> cotLopRoiCuaToi;
    @FXML private TableColumn<YeuCauDoiLop, String> cotLopVaoCuaToi;
    @FXML private TableColumn<YeuCauDoiLop, String> cotTrangThaiCuaToi;
    @FXML private TableColumn<YeuCauDoiLop, String> cotLyDoCuaToi;
    @FXML private TableColumn<YeuCauDoiLop, String> cotNgayCuaToi;

    @FXML
    private void initialize() {
        cotMaSan.setCellValueFactory(new PropertyValueFactory<>("maYeuCau"));
        cotSinhVienSan.setCellValueFactory(new PropertyValueFactory<>("sinhVienHienThi"));
        cotLopRoiSan.setCellValueFactory(new PropertyValueFactory<>("tenLopCu"));
        cotLopVaoSan.setCellValueFactory(new PropertyValueFactory<>("tenLopMoi"));
        cotLyDoSan.setCellValueFactory(new PropertyValueFactory<>("lyDo"));
        cotNgaySan.setCellValueFactory(new PropertyValueFactory<>("ngayTaoHienThi"));
        cotMaCuaToi.setCellValueFactory(new PropertyValueFactory<>("maYeuCau"));
        cotLopRoiCuaToi.setCellValueFactory(new PropertyValueFactory<>("tenLopCu"));
        cotLopVaoCuaToi.setCellValueFactory(new PropertyValueFactory<>("tenLopMoi"));
        cotTrangThaiCuaToi.setCellValueFactory(new PropertyValueFactory<>("trangThaiHienThi"));
        cotLyDoCuaToi.setCellValueFactory(new PropertyValueFactory<>("lyDo"));
        cotNgayCuaToi.setCellValueFactory(new PropertyValueFactory<>("ngayTaoHienThi"));
        tblSanGiaoDich.setItems(danhSachSanDaLoc);
        cmbLopDangHoc.setConverter(boChuyenDoiLop());
        cmbLopMuonVao.setConverter(boChuyenDoiLop());
        txtTimKiem.textProperty().addListener(
                (thuocTinh, giaTriCu, giaTriMoi) -> apDungTimKiem());
    }

    public void ganSinhVien(Users nguoiDung) {
        this.sinhVien = nguoiDung instanceof SinhVien sv ? sv : null;
        if (sinhVien == null || sinhVien.getMaSinhVien() == null
                || sinhVien.getMaSinhVien().isBlank()) {
            datTrangThai("Không xác định được mã sinh viên. Vui lòng quay lại và đăng nhập lại.", true);
            return;
        }
        taiDuLieu();
    }

    @FXML
    private void taiLai(ActionEvent suKien) {
        taiDuLieu();
    }

    @FXML
    private void dangBai(ActionEvent suKien) {
        LopHocDangKy lopRoi = cmbLopDangHoc.getValue();
        LopHocDangKy lopVao = cmbLopMuonVao.getValue();
        if (sinhVien == null || lopRoi == null || lopVao == null) {
            datTrangThai("Vui lòng chọn lớp đang học và lớp mong muốn.", true);
            return;
        }
        if (lopRoi.getMaLop().equals(lopVao.getMaLop())) {
            datTrangThai("Lớp muốn vào phải khác lớp bạn đang học.", true);
            return;
        }
        String lyDo = txtLyDo.getText() == null ? "" : txtLyDo.getText().trim();
        if (lyDo.length() > 200) {
            datTrangThai("Lý do không được vượt quá 200 ký tự.", true);
            return;
        }
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận đăng bài");
        xacNhan.setHeaderText("Đăng đề nghị trao đổi lớp?");
        xacNhan.setContentText("Bạn đang học " + lopRoi.getMaLop() + " · " + lopRoi.getTenMon()
                + " và muốn vào " + lopVao.getMaLop() + " · " + lopVao.getTenMon()
                + ". Sinh viên chốt phải đang học lớp bạn muốn vào. Khác môn học vẫn được phép.");
        if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean thanhCong = dichVuDoiLop.dangBaiDoiLop(
                    sinhVien.getMaSinhVien(), lopRoi.getMaLop(), lopVao.getMaLop(), lyDo);
            datTrangThai(thanhCong ? "Đã đăng yêu cầu đổi lớp." : "Không thể đăng yêu cầu đổi lớp.",
                    !thanhCong);
            if (thanhCong) {
                txtLyDo.clear();
                taiDuLieu();
            }
        } catch (ServiceException e) {
            datTrangThai("Không thể đăng bài do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            datTrangThai("Đã xảy ra lỗi khi đăng bài. Vui lòng thử lại sau.", true);
        }
    }

    @FXML
    private void chotBai(ActionEvent suKien) {
        YeuCauDoiLop duocChon = tblSanGiaoDich.getSelectionModel().getSelectedItem();
        if (duocChon == null || sinhVien == null) {
            datTrangThai("Hãy chọn một bài đăng của sinh viên khác để chốt.", true);
            return;
        }
        if (sinhVien.getMaSinhVien().equals(duocChon.getMaSinhVien())) {
            datTrangThai("Bạn không thể tự chốt bài đăng của mình.", true);
            return;
        }
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận chốt giao dịch");
        xacNhan.setHeaderText("Hoán đổi hai lớp?");
        xacNhan.setContentText("Bài đăng #" + duocChon.getMaYeuCau() + " cần bạn đang học lớp "
                + duocChon.getLopMoi() + ". Khi thành công, bạn sẽ sang lớp " + duocChon.getLopCu()
                + " và người đăng sẽ sang lớp " + duocChon.getLopMoi()
                + ". Hệ thống kiểm tra lại điều kiện và thực hiện hoán đổi trong một giao dịch.");
        if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean thanhCong = dichVuDoiLop.xacNhanDoiLop(
                    duocChon.getMaYeuCau(), sinhVien.getMaSinhVien());
            datTrangThai(thanhCong ? "Đổi lớp thành công." : "Không thể hoàn tất yêu cầu đổi lớp.",
                    !thanhCong);
            taiDuLieu();
        } catch (ServiceException e) {
            datTrangThai("Không thể xử lý giao dịch do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            datTrangThai("Đã xảy ra lỗi khi xử lý giao dịch. Vui lòng thử lại sau.", true);
        }
    }

    @FXML
    private void huyBai(ActionEvent suKien) {
        YeuCauDoiLop duocChon = tblYeuCauCuaToi.getSelectionModel().getSelectedItem();
        if (duocChon == null || !"CHO_XAC_NHAN".equals(duocChon.getTrangThai())) {
            datTrangThai("Chỉ có thể hủy bài đăng của bạn đang ở trạng thái chờ xác nhận.", true);
            return;
        }
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận hủy bài");
        xacNhan.setHeaderText("Hủy bài đăng #" + duocChon.getMaYeuCau() + "?");
        xacNhan.setContentText("Bài sẽ không còn xuất hiện trên sàn. Không thể hủy sau khi đã được xử lý.");
        if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            boolean thanhCong = dichVuDoiLop.huyYeuCau(
                    duocChon.getMaYeuCau(), sinhVien.getMaSinhVien());
            datTrangThai(thanhCong ? "Đã hủy bài đăng đổi lớp." : "Không thể hủy bài đăng đổi lớp.",
                    !thanhCong);
            taiDuLieu();
        } catch (ServiceException e) {
            datTrangThai("Không thể hủy bài do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            datTrangThai("Đã xảy ra lỗi khi hủy bài. Vui lòng thử lại sau.", true);
        }
    }

    private void taiDuLieu() {
        if (sinhVien == null || sinhVien.getMaSinhVien() == null
                || sinhVien.getMaSinhVien().isBlank()) return;
        try {
            List<LopHocDangKy> danhSachLop =
                    lopHocPhanDAO.layDanhSachLopDangKy(sinhVien.getMaSinhVien());
            cmbLopDangHoc.setItems(FXCollections.observableArrayList(
                    danhSachLop.stream().filter(muc -> muc.isDaDangKy()
                            && "DANG_HOC".equals(muc.getTrangThaiDangKy())).toList()));
            cmbLopMuonVao.setItems(FXCollections.observableArrayList(
                    danhSachLop.stream().filter(muc -> !muc.isDaDangKy()).toList()));
            List<YeuCauDoiLop> baiDang = dichVuDoiLop.moSanGiaoDich();
            danhSachSan.setAll(baiDang.stream()
                    .filter(muc -> !sinhVien.getMaSinhVien().equals(muc.getMaSinhVien())).toList());
            tblYeuCauCuaToi.setItems(FXCollections.observableArrayList(
                    dichVuDoiLop.xemYeuCauCuaSinhVien(sinhVien.getMaSinhVien())));
            apDungTimKiem();
            capNhatNhanTrong();
        } catch (SQLException | ServiceException e) {
            NHAT_KY.log(Level.SEVERE, "Không thể tải dữ liệu đổi lớp.", e);
            cmbLopDangHoc.getItems().clear();
            cmbLopMuonVao.getItems().clear();
            danhSachSan.clear();
            tblYeuCauCuaToi.setItems(FXCollections.observableArrayList());
            apDungTimKiem();
            capNhatNhanTrong();
            datTrangThai("Không thể tải dữ liệu đổi lớp. Kiểm tra kết nối cơ sở dữ liệu rồi thử tải lại.", true);
        } catch (RuntimeException e) {
            NHAT_KY.log(Level.SEVERE, "Lỗi không xác định khi tải dữ liệu đổi lớp.", e);
            datTrangThai("Đã xảy ra lỗi khi tải dữ liệu. Vui lòng thử tải lại.", true);
        }
    }

    private void apDungTimKiem() {
        String tuKhoa = txtTimKiem.getText() == null
                ? "" : txtTimKiem.getText().trim().toLowerCase(Locale.ROOT);
        danhSachSanDaLoc.setPredicate(muc -> {
            String noiDung = (muc.getMaYeuCau() + " " + muc.getMaSinhVien() + " " + muc.getTenSinhVien()
                    + " " + muc.getLopCu() + " " + muc.getTenLopCu() + " " + muc.getLopMoi()
                    + " " + muc.getTenLopMoi() + " " + muc.getLyDo()).toLowerCase(Locale.ROOT);
            return noiDung.contains(tuKhoa);
        });
        capNhatNhanTrong();
    }

    private void capNhatNhanTrong() {
        lblSanTrong.setVisible(danhSachSanDaLoc.isEmpty());
        lblSanTrong.setManaged(danhSachSanDaLoc.isEmpty());
        boolean khongCoBaiCuaToi = tblYeuCauCuaToi.getItems() == null
                || tblYeuCauCuaToi.getItems().isEmpty();
        lblCuaToiTrong.setVisible(khongCoBaiCuaToi);
        lblCuaToiTrong.setManaged(khongCoBaiCuaToi);
    }

    private StringConverter<LopHocDangKy> boChuyenDoiLop() {
        return new StringConverter<>() {
            @Override
            public String toString(LopHocDangKy muc) {
                return muc == null ? "" : muc.getMaLop() + " · " + muc.getTenMon()
                        + " (" + muc.getMaMon() + ", HK " + muc.getHocKy() + ")";
            }

            @Override
            public LopHocDangKy fromString(String giaTri) {
                return null;
            }
        };
    }

    private void datTrangThai(String thongBao, boolean loi) {
        lblTrangThai.setText(thongBao);
        lblTrangThai.getStyleClass().removeAll("doi-lop-thong-bao-thanh-cong", "doi-lop-thong-bao-loi");
        lblTrangThai.getStyleClass().add(
                loi ? "doi-lop-thong-bao-loi" : "doi-lop-thong-bao-thanh-cong");
        lblTrangThai.setVisible(true);
        lblTrangThai.setManaged(true);
    }
}
