package Model;

import java.util.*;

public abstract class Users {
    private Integer userID;
    private String hoTen;
    private String email;
    private String password;
    private String vaiTro;
    private String maSo;
    private Date ngayTao;
    private String trangThai;

    public Users(Integer userID, String hoTen, String email, String password,
                 String vaiTro, String maSo, Date ngayTao, String trangThai) {
        this.userID = userID;
        this.hoTen = hoTen;
        this.email = email;
        this.password = password;
        this.vaiTro = vaiTro;
        this.maSo = maSo;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
    }

    public abstract String getTenVaiTroHienThi();

    public abstract String getMaDinhDanh();

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getMaSo() {
        return maSo;
    }

    public void setMaSo(String maSo) {
        this.maSo = maSo;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
