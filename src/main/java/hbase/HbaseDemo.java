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
        String nsName = "fred";
        String tableName = "T_FRED_API";
        //admin.createNamespace(NamespaceDescriptor.create(nsName).build());

        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(nsName + ":" + tableName));
        if (admin.tableExists(hTableDescriptor.getTableName())) {
            System.out.println(tableName + ": 已经存在");
            found = true;
        }

        //如果表已经存在，先disable，再delete，然后重新创建
        if (admin.tableExists(hTableDescriptor.getTableName())) {
            admin.disableTable(hTableDescriptor.getTableName());
            admin.deleteTable(hTableDescriptor.getTableName());
            System.out.println("删除表：" + tableName);
        }
        hTableDescriptor.setDurability(Durability.SYNC_WAL);
        //创建列簇
        HColumnDescriptor cf1 = new HColumnDescriptor("cf-1");
        cf1.setCompressTags(false);
        cf1.setMaxVersions(3);

        hTableDescriptor.addFamily(cf1);
        admin.createTable(hTableDescriptor);
        /*
         * HTableDescriptor, HColumnDescriptor分别用户描述表和列簇。
         * 如需删除表，需要先将表disableTable而后才能执行deleteTable操作。
         * HColumnDescriptor包含了列簇所含的最大版本个数，压缩设置等。
         */
        //remove column family
        admin.disableTable(hTableDescriptor.getTableName());
        hTableDescriptor.remove(Bytes.toBytes("cf-1"));

        //add
        HColumnDescriptor cf2 = new HColumnDescriptor("cf2");
        cf2.setMaxVersions(3);
        hTableDescriptor.addFamily(cf2);

        //修改表结构&enable表
        admin.modifyTable(hTableDescriptor.getTableName(), hTableDescriptor);
        admin.enableTable(hTableDescriptor.getTableName());


        // put 数据
        TableName tName = TableName.valueOf(nsName + ":" + tableName);
        if (!admin.tableExists(tName)) {
            return;
        }
        Table table = conn.getTable(tName);
        List<Put> puts = new ArrayList<>();

        //第一条记录
        Put put = new Put(Bytes.toBytes("row-1"));
        put.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("name"), Bytes.toBytes("LinTao"));
        put.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("age"), Bytes.toBytes("28"));
        put.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("school"), Bytes.toBytes("NJUPT"));
        puts.add(put);
        //第二条记录
        put = new Put(Bytes.toBytes("row-2"));
        put.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("name"), Bytes.toBytes("LinLei"));
        put.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("age"), Bytes.toBytes("29"));
        put.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("school"), Bytes.toBytes("NJU"));
        puts.add(put);
        // 插入数据
        table.put(puts);

        //scan data
        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes("cf2"));
        ResultScanner rs = table.getScanner(scan);
        for (Result result = rs.next(); result != null; result = rs.next()) {
            for (Cell cell : result.rawCells()) {
                System.out.println("RowKey :" + Bytes.toString(result.getRow()) +
                                "Familiy:Qualifir :" + Bytes.toString(CellUtil.cloneQualifier(cell)) +
                                "Value:" + Bytes.toString(CellUtil.cloneValue(cell)));
            }
        }

        // get data
        //get
        Get get = new Get(Bytes.toBytes("row-1"));
        Result result = table.get(get);
        for(Cell cell:result.rawCells()){
            System.out.println(
                    "RowKey :" + Bytes.toString(result.getRow()) +
                            "Familiy:Qualifir :" + Bytes.toString(CellUtil.cloneQualifier(cell))+
                            "Value:" + Bytes.toString(CellUtil.cloneValue(cell)));
        }

        //delete
        Delete delete = new Delete(Bytes.toBytes("row-1"));
        delete.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("age"));
        table.delete(delete);
    }
}
