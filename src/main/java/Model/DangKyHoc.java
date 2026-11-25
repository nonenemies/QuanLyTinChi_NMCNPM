package Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

public class DangKyHoc {

    private int maDangKy;
    private String maLop;
    private String maSinhVien;
    private Date ngayDangKy;
    private BigDecimal diemTongKet;
    private String trangThai;
    private List<DiemThanhPhan> diemThanhPhan = new ArrayList<>();

    public DangKyHoc() {
    }

    public DangKyHoc(String maLop, String maSinhVien) {
        this.maLop = maLop;
        this.maSinhVien = maSinhVien;
    }

    public DangKyHoc(int maDangKy, String maLop, String maSinhVien, Date ngayDangKy, BigDecimal diemTongKet, String trangThai) {
        this.maDangKy = maDangKy;
        this.maLop = maLop;
        this.maSinhVien = maSinhVien;
        this.ngayDangKy = ngayDangKy;
        this.diemTongKet = diemTongKet;
        this.trangThai = trangThai;
    }

    public int getMaDangKy() {
        return maDangKy;
    }

    public void setMaDangKy(int maDangKy) {
        this.maDangKy = maDangKy;
    }

    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public String getMaSinhVien() {
        return maSinhVien;
    }

    public void setMaSinhVien(String maSinhVien) {
        this.maSinhVien = maSinhVien;
    }

    public Date getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(Date ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public BigDecimal getDiemTongKet() {
        return diemTongKet;
    }

    public void setDiemTongKet(BigDecimal diemTongKet) {
        this.diemTongKet = diemTongKet;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<DiemThanhPhan> getDiemThanhPhan() {
        return diemThanhPhan;
    }

    public void setDiemThanhPhan(List<DiemThanhPhan> diemThanhPhan) {
        this.diemThanhPhan = diemThanhPhan == null
                ? new ArrayList<>() : new ArrayList<>(diemThanhPhan);
    }
}
