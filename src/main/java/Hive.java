import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Hive {

    // 注意：此处不能使用"org.apache.hive.jdbc.HiveDriver.class"，否则会报错。
    private static String driver = "org.apache.hive.jdbc.HiveDriver";

    // hive默认的端口是10000，default是要连接的hive的数据库的名称
    private static String url = "jdbc:hive2://hadoop001:10000/test";

    // 注册驱动
    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // 获取连接
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, "", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        ResultSet rs = getConnection().createStatement().executeQuery("select * from dept");
        while (rs.next()) {
            String deptno = rs.getString("deptno");
            String dname = rs.getString("dname");
            System.out.println("deptno:" + deptno);
            System.out.println("dname:" + dname);
        }

    }

}
