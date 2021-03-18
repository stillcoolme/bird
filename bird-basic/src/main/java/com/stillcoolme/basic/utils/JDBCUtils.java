package com.stillcoolme.basic.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class JDBCUtils {

        //定义MySQL的数据库驱动程序
        public static final String DBDRIVER = "com.mysql.jdbc.Driver";
        //定义MySQL数据库的连接地址
        public static final String DBURL = "jdbc:mysql://localhost:3306/bos";
        //MySQL数据库的连接用户名
        public static final String DBUSER = "test";
        //MySQL数据库的连接密码
        public static final String DBPASS = "123456";

        public static Connection getConnection() {
            Connection con = null;
            try {
                //加载驱动程序
                Class.forName(DBDRIVER);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                //连接MySQL数据库时，要写上连接的用户名和密码
                con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            return con;

        }

    public static void main(String[] args) {
        System.out.println(getConnection().toString());
    }

}
