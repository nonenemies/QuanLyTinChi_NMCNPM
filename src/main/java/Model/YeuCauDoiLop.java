package Model;
import java.util.Date;
import java.text.SimpleDateFormat;

public class YeuCauDoiLop {
    private int maYeuCau;
    private String maSinhVien;
    private String lopCu;
    private String lopMoi;
    private String trangThai;
    private String lyDo;
    private Date ngayTao;
    private Date ngayXuLy;
    private Integer nguoiXuLy;
    private String tenSinhVien;
    private String tenLopCu;
    private String tenLopMoi;

    public YeuCauDoiLop() {
    }

    public YeuCauDoiLop(String maSinhVien, String lopCu, String lopMoi, String lyDo) {
        this.maSinhVien = maSinhVien;
        this.lopCu = lopCu;
        this.lopMoi = lopMoi;
        this.lyDo = lyDo;
    }

    public int getMaYeuCau() { return maYeuCau; }
    public void setMaYeuCau(int maYeuCau) { this.maYeuCau = maYeuCau; }
    public String getMaSinhVien() { return maSinhVien; }
    public void setMaSinhVien(String maSinhVien) { this.maSinhVien = maSinhVien; }
    public String getLopCu() { return lopCu; }
    public void setLopCu(String lopCu) { this.lopCu = lopCu; }
    public String getLopMoi() { return lopMoi; }
    public void setLopMoi(String lopMoi) { this.lopMoi = lopMoi; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public Date getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(Date ngayXuLy) { this.ngayXuLy = ngayXuLy; }
    public Integer getNguoiXuLy() { return nguoiXuLy; }
    public void setNguoiXuLy(Integer nguoiXuLy) { this.nguoiXuLy = nguoiXuLy; }
    public String getTenSinhVien() { return tenSinhVien; }
    public void setTenSinhVien(String tenSinhVien) { this.tenSinhVien = tenSinhVien; }
    public String getSinhVienHienThi() {
        return (tenSinhVien == null ? "Sinh viên" : tenSinhVien) + " · " + maSinhVien;
    }
    public String getTenLopCu() { return tenLopCu; }
    public void setTenLopCu(String tenLopCu) { this.tenLopCu = tenLopCu; }
    public String getTenLopMoi() { return tenLopMoi; }
    public void setTenLopMoi(String tenLopMoi) { this.tenLopMoi = tenLopMoi; }
    public String getTrangThaiHienThi() {
        if ("CHO_XAC_NHAN".equals(trangThai)) return "Đang chờ";
        if ("DA_DUYET".equals(trangThai)) return "Đã trao đổi";
        if ("TU_CHOI".equals(trangThai)) return "Bị từ chối";
        if ("HUY".equals(trangThai)) return "Đã hủy";
        return trangThai == null ? "Không rõ" : trangThai;
    }
    public String getNgayTaoHienThi() {
        return ngayTao == null ? "—" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ngayTao);
    }
}