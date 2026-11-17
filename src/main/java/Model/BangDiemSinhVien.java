package Model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Dùng cho giảng viên. Dữ liệu một sinh viên dùng để hiển thị bảng điểm của lớp học phần. */
public class BangDiemSinhVien {
    private final int maDangKy;
    private final String maSV;
    private final String hoTen;
    private final String trangThai;
    private final Map<Integer, Float> diemThanhPhan = new LinkedHashMap<>();
    private Float diemTongKet;

    public BangDiemSinhVien(int maDangKy, String maSV, String hoTen,
                            Float diemTongKet, String trangThai) {
        this.maDangKy = maDangKy;
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.diemTongKet = diemTongKet;
        this.trangThai = trangThai;
    }

    public int getMaDangKy() {
        return maDangKy;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public Float getDiemTongKet() {
        return diemTongKet;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public Float getDiemThanhPhan(int maTP) {
        return diemThanhPhan.get(maTP);
    }

    public void setDiemThanhPhan(int maTP, Float diem) {
        if (diem != null) {
            diemThanhPhan.put(maTP, diem);
        }
    }
}
