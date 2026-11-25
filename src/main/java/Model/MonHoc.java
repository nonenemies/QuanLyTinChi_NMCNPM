package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model ánh xạ tới bảng MonHoc trong database.
 */
public class MonHoc {
    private String maMon;
    private String tenMon;
    private Integer soTinChi;
    private String loaiMon;
    private String moTa;
    private String trangThai;
    private List<LopHocPhan> hocPhan = new ArrayList<>();
    private List<ThanhPhanDanhGia> thanhPhanDanhGias = new ArrayList<>();
    private List<MonTienQuyet> monTienQuyets = new ArrayList<>();

    public MonHoc() {
    }

    public MonHoc(String maMon, String tenMon, Integer soTinChi,
                  String loaiMon, String moTa, String trangThai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.loaiMon = loaiMon;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public Integer getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(Integer soTinChi) {
        this.soTinChi = soTinChi;
    }

    public String getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<LopHocPhan> getHocPhan() {
        return hocPhan;
    }

    public void setHocPhan(List<LopHocPhan> hocPhan) {
        this.hocPhan = hocPhan == null ? new ArrayList<>() : new ArrayList<>(hocPhan);
    }

    public List<ThanhPhanDanhGia> getThanhPhanDanhGias() {
        return thanhPhanDanhGias;
    }

    public void setThanhPhanDanhGias(List<ThanhPhanDanhGia> thanhPhanDanhGias) {
        this.thanhPhanDanhGias = thanhPhanDanhGias == null
                ? new ArrayList<>() : new ArrayList<>(thanhPhanDanhGias);
    }

    public List<MonTienQuyet> getMonTienQuyets() {
        return monTienQuyets;
    }

    public void setMonTienQuyets(List<MonTienQuyet> monTienQuyets) {
        this.monTienQuyets = monTienQuyets == null ? new ArrayList<>() : new ArrayList<>(monTienQuyets);
    }
}
