import java.sql.*;

public class InspectSchema {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/lms_instructor_db";
        String user = "cyberlearnix";
        String password = "cyberlearnix123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Columns in 'content' table:");
            try (ResultSet rs = meta.getColumns(null, null, "content", null)) {
                while (rs.next()) {
                    System.out.println(rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
                }
            }
            
            System.out.println("\nSampling 5 rows from 'content':");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM content LIMIT 5")) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int cols = rsmd.getColumnCount();
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        System.out.print(rsmd.getColumnName(i) + ": " + rs.getObject(i) + " | ");
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
