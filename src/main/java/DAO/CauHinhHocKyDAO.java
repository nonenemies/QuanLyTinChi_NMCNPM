package DAO;

import Model.CauHinhHocKy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Kiểm tra khung thời gian nhập điểm của học kỳ. */
public class CauHinhHocKyDAO extends BaseDAO {
    public CauHinhHocKy layCauHinhTrongDotNhapDiem(String hocKy) throws SQLException {
        String sql = "SELECT HocKy, KetThucHocKy, HanNhapDiem FROM CauHinhHocKy "
                + "WHERE HocKy = ? AND CURRENT_TIMESTAMP >= TIMESTAMP(KetThucHocKy) "
                + "AND CURRENT_TIMESTAMP <= HanNhapDiem";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new CauHinhHocKy(
                        rs.getString("HocKy"),
                        rs.getDate("KetThucHocKy").toLocalDate(),
                        rs.getTimestamp("HanNhapDiem").toLocalDateTime());
            }
        }
    }

    /**
     * Học kỳ hiện hành: ưu tiên kỳ còn trong hạn (chưa hết HanNhapDiem),
     * nếu không có thì lấy kỳ mới nhất theo ngày kết thúc.
     */
    public String layHocKyHienTai() throws SQLException {
        String dangMo = "SELECT HocKy FROM CauHinhHocKy "
                + "WHERE CURRENT_TIMESTAMP <= HanNhapDiem "
                + "ORDER BY KetThucHocKy DESC LIMIT 1";
        try (Connection conn = getConnection()) {
            String hocKy = truyVanMotHocKy(conn, dangMo);
            if (hocKy != null) {
                return hocKy;
            }
            return truyVanMotHocKy(conn,
                    "SELECT HocKy FROM CauHinhHocKy ORDER BY KetThucHocKy DESC LIMIT 1");
        }
    }

    private String truyVanMotHocKy(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("HocKy") : null;
        }
    }
}
