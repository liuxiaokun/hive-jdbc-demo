package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HbaseDemo {

    public static void main(String[] args) throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "hadoop001,hadoop002,hadoop003");
        Connection conn = ConnectionFactory.createConnection(config);
        Admin admin = conn.getAdmin();
        NamespaceDescriptor[] nsList = admin.listNamespaceDescriptors();
        boolean found = false;
        for (NamespaceDescriptor ns : nsList) {
            System.out.println("Namespace:" + ns.getName());
        }
        String nsName = "default";
        String tableName = "TFRED";
        //admin.createNamespace(NamespaceDescriptor.create(nsName).build());

        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(nsName + ":" + tableName));
        if (admin.tableExists(hTableDescriptor.getTableName())) {
            System.out.println(tableName + ": 已经存在");
        }

        ///////////////////////////////////////////////////////////////

        HTableDescriptor table = new HTableDescriptor(TableName.valueOf(nsName + ":" + tableName));
        //如果表已经存在，先disable，再delete，然后重新创建
        if (admin.tableExists(table.getTableName())) {
            admin.disableTable(table.getTableName());
            admin.deleteTable(table.getTableName());
        }
        table.setDurability(Durability.SYNC_WAL);
        //创建列簇
        HColumnDescriptor cf1 = new HColumnDescriptor("cf-1");
        cf1.setCompressTags(false);
        cf1.setMaxVersions(3);

        table.addFamily(cf1);
        admin.createTable(table);
        /*HTableDescriptor,HColumnDescriptor分别用户描述表和列簇。
         * 如需删除表，需要先将表disableTable而后才能执行deleteTable操作。
         * HColumnDescriptor包含了列簇所含的最大版本个数，压缩设置等。
         */

        //remove column family
        admin.disableTable(table.getTableName());
        table.remove(Bytes.toBytes("cf-1"));

        //add
        HColumnDescriptor cf2 = new HColumnDescriptor("cf2");
        cf2.setMaxVersions(3);
        table.addFamily(cf2);

        //修改表结构&enable表
        admin.modifyTable(table.getTableName(), table);
        admin.enableTable(table.getTableName());


        // put 数据
        TableName tName = TableName.valueOf(nsName + ":" + tableName);
        if (!admin.tableExists(tName)) {
            return;
        }
        Table table2 = conn.getTable(tName);
        List<Put> puts = new ArrayList<>();

        //第一条记录
        Put put = new Put(Bytes.toBytes("row-1"));
        put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("name"), Bytes.toBytes("LinTao"));
        put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("age"), Bytes.toBytes("28"));
        put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("school"), Bytes.toBytes("NJUPT"));
        puts.add(put);

        //第二条记录
        put = new Put(Bytes.toBytes("row-2"));
        put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("name"), Bytes.toBytes("LinLei"));
        put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("age"), Bytes.toBytes("29"));
        put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("school"), Bytes.toBytes("NJU"));
        puts.add(put);

        table2.put(puts);

    }
}
