import java.sql.*;

public class Impala {

    private static String driver = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) throws Exception {
        Class.forName(driver);
        Connection con = DriverManager.getConnection(
                "jdbc:hive2://hadoop001:21050/test;auth=noSasl;", "impala", "impala");
        Statement stmt = con.createStatement();
        String sql = "select * from dept";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(1111);
            String deptno = rs.getString("deptno");
            String dname = rs.getString("dname");
            System.out.println("deptno:" + deptno);
            System.out.println("dname:" + dname);
        }
    }
}
