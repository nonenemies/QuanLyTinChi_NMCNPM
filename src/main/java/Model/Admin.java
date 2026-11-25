package Model;

import java.util.Date;

public class Admin extends Users {
        private String chucVu;

        public Admin(Integer userID, String hoTen, String email,
                     String password, String vaiTro, String maSo,
                     Date ngayTao, String trangThai, String chucVu) {
                super(userID, hoTen, email, password, vaiTro, maSo, ngayTao, trangThai);
                this.chucVu = chucVu;
        }

        @Override
        public String getTenVaiTroHienThi() {
                return "QUẢN TRỊ VIÊN";
        }

        @Override
        public String getMaDinhDanh() {
                return getMaSo();
        }

        public String getChucVu() {
                return chucVu;
        }

        public void setChucVu(String chucVu) {
                this.chucVu = chucVu;
        }
}
