package Controllers;

import Model.SinhVien;
import Model.Users;
import Services.GeminiAIService;
import Services.NguCanhTroLyAIService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.Normalizer;
import java.util.Locale;

public class TroLyAIController {
    private final GeminiAIService dichVuGeminiAI = new GeminiAIService();
    private final NguCanhTroLyAIService dichVuNguCanh = new NguCanhTroLyAIService();

    @FXML private ScrollPane khungCuonChat;
    @FXML private VBox hopTinNhan;
    @FXML private TextField txtCauHoi;
    @FXML private Button btnGui;
    @FXML private Label lblTrangThaiChat;

    private SinhVien sinhVien;
    private boolean dangXuLyYeuCau;

    public void ganSinhVien(Users nguoiDung) {
        if (nguoiDung instanceof SinhVien sv) {
            sinhVien = sv;
            themBongBong("Xin chào "
                            + giaTriHoacChuaRo(sv.getHoTen())
                            + "! Mình có thể hỗ trợ về thông tin cá nhân, chương trình đào tạo, "
                            + "đăng ký lớp, điểm số, lịch học và kế hoạch học.",
                    false);
        } else {
            themBongBong("Không xác định được tài khoản sinh viên. Vui lòng đăng nhập lại.", false);
            datTrangThaiDangXuLy(true);
        }
    }

    @FXML
    private void guiTinNhan() {
        String cauHoi = txtCauHoi.getText() == null ? "" : txtCauHoi.getText().trim();
        if (cauHoi.isEmpty() || dangXuLyYeuCau) {
            return;
        }
        if (sinhVien == null) {
            datTrangThai("Không thể xác định tài khoản sinh viên.", true);
            return;
        }

        txtCauHoi.clear();
        themBongBong(cauHoi, true);
        if (hoiTenTroLy(cauHoi)) {
            themBongBong("Mình là Trợ lý AI học tập của ứng dụng Quản lý tín chỉ. "
                    + "Mình có thể hỗ trợ bạn về thông tin cá nhân, chương trình đào tạo, "
                    + "học phần, điểm số, lịch học và kế hoạch học tập.", false);
            return;
        }
        if (hoiTenSinhVien(cauHoi)) {
            themBongBong("Tên bạn là " + giaTriHoacChuaRo(sinhVien.getHoTen()) + ".", false);
            return;
        }
        Label tinDangGo = themBongBong("AI đang gõ...", false);
        datTrangThaiDangXuLy(true);
        datTrangThai("", false);

        Task<String> tacVu = new Task<>() {
            @Override
            protected String call() throws Exception {
                String nguCanh = dichVuNguCanh.xayDungNguCanhDayDu(sinhVien);
                return dichVuGeminiAI.askAI(cauHoi, nguCanh);
            }
        };
        tacVu.setOnSucceeded(suKien -> {
            hopTinNhan.getChildren().remove(tinDangGo.getParent());
            themBongBong(tacVu.getValue(), false);
            datTrangThaiDangXuLy(false);
        });
        tacVu.setOnFailed(suKien -> {
            hopTinNhan.getChildren().remove(tinDangGo.getParent());
            themBongBong("Không thể tải dữ liệu hoặc kết nối AI. Vui lòng thử lại.", false);
            datTrangThaiDangXuLy(false);
        });

        Thread luongXuLy = new Thread(tacVu, "yeu-cau-tro-ly-ai");
        luongXuLy.setDaemon(true);
        luongXuLy.start();
    }

    @FXML
    private void khiGuiCauHoi() {
        guiTinNhan();
    }

    private String giaTriHoacChuaRo(String giaTri) {
        return giaTri == null || giaTri.isBlank() ? "chưa rõ" : giaTri;
    }

    private boolean hoiTenSinhVien(String cauHoi) {
        String cauHoiKhongDau = boDau(cauHoi);
        return cauHoiKhongDau.contains("ten toi")
                || cauHoiKhongDau.contains("ten minh")
                || cauHoiKhongDau.contains("toi ten gi")
                || cauHoiKhongDau.contains("minh ten gi")
                || cauHoiKhongDau.contains("ten cua toi")
                || cauHoiKhongDau.contains("ten cua minh")
                || cauHoiKhongDau.contains("toi la ai")
                || cauHoiKhongDau.contains("minh la ai")
                || cauHoiKhongDau.contains("ho ten toi")
                || cauHoiKhongDau.contains("ho ten minh");
    }

    private boolean hoiTenTroLy(String cauHoi) {
        String cauHoiKhongDau = boDau(cauHoi);
        return cauHoiKhongDau.contains("ban ten gi")
                || cauHoiKhongDau.contains("ten ban la gi")
                || cauHoiKhongDau.contains("ten cua ban")
                || cauHoiKhongDau.contains("ten tro ly")
                || cauHoiKhongDau.contains("ban la ai");
    }

    private String boDau(String vanBan) {
        return Normalizer.normalize(vanBan, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private Label themBongBong(String noiDung, boolean tuNguoiDung) {
        Label bongBong = new Label(noiDung);
        bongBong.setWrapText(true);
        bongBong.setMaxWidth(560);
        bongBong.getStyleClass().add(tuNguoiDung ? "tro-ly-ai-bong-bong-nguoi-dung" : "tro-ly-ai-bong-bong-ai");

        HBox hang = new HBox(bongBong);
        hang.setAlignment(tuNguoiDung ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        hang.getStyleClass().add("tro-ly-ai-hang-tin-nhan");
        hopTinNhan.getChildren().add(hang);
        khungCuonChat.layout();
        khungCuonChat.setVvalue(1.0);
        return bongBong;
    }

    private void datTrangThaiDangXuLy(boolean dangXuLy) {
        dangXuLyYeuCau = dangXuLy;
        btnGui.setDisable(dangXuLy);
        txtCauHoi.setDisable(dangXuLy);
        btnGui.setText(dangXuLy ? "Đang gửi..." : "Gửi");
    }

    private void datTrangThai(String thongBao, boolean loi) {
        lblTrangThaiChat.setText(thongBao);
        lblTrangThaiChat.getStyleClass().removeAll("tro-ly-ai-trang-thai-loi", "tro-ly-ai-trang-thai-info");
        if (thongBao != null && !thongBao.isBlank()) {
            lblTrangThaiChat.getStyleClass().add(
                    loi ? "tro-ly-ai-trang-thai-loi" : "tro-ly-ai-trang-thai-info");
        }
    }
}
