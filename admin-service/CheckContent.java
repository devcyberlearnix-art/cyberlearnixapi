import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckContent {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/lms_instructor_db";
        String user = "cyberlearnix";
        String password = "cyberlearnix123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT id, course_id, title FROM content";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            boolean hasData = false;
            System.out.println("Contents in DB:");
            while (rs.next()) {
                hasData = true;
                System.out.println("Content ID: " + rs.getString("id") + " | Course ID: " + rs.getString("course_id") + " | Title: " + rs.getString("title"));
            }
            if (!hasData) {
                System.out.println("The content table is completely EMPTY!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
