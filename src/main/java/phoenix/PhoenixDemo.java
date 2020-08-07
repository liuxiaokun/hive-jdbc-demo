package phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class PhoenixDemo {


    public static void main(String[] args) throws Exception {
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop001:2181:/hbase", "", "");
        Statement stat = conn.createStatement();
        String sql="create table test_user(id integer not null primary key , name varchar , age integer)";
        stat.executeUpdate(sql);

        String sql1="upsert into test_user values(1,'zhangsan',22)";
        String sql2="upsert into test_user values(2,'lisi',33)";
        String sql3="upsert into test_user values(3,'wangwu',44)";
        stat.executeUpdate(sql1);
        stat.executeUpdate(sql2);
        stat.executeUpdate(sql3);
        conn.commit();
    }
}
