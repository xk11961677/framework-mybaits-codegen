package com.sky.framework.codegen.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
@Slf4j
public class TableUtils {

    private static String url;
    private static String username;
    private static String password;
    private static String databaseName;

    static {
        url = PropertyUtils.datasourceConfig.getString("url");
        username = PropertyUtils.datasourceConfig.getString("username");
        password = PropertyUtils.datasourceConfig.getString("password");
        databaseName = PropertyUtils.datasourceConfig.getString("database_name");
    }

    /**
     * 获取连接
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 获取所有表名
     *
     * @return
     * @throws SQLException
     */
    public static List<String> getTables() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            con = getConnection();
            ps = con.prepareStatement("show tables");
            rs = ps.executeQuery();
            String tableName;
            for (; rs.next(); list.add(tableName)) {
                tableName = rs.getString(1).toLowerCase();
            }
        } catch (Exception e) {
            log.error("getTables exception:{}", e.getMessage(), e);
        } finally {
            close(con, ps, rs);
        }
        return list;
    }


    /**
     * 获取表信息
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static Map<String, String> getTableInfo(String tableName) throws SQLException {
        String SQLTables = (new StringBuilder("select table_name tableName, engine, table_comment tableComment, create_time createTime from information_schema.tables " +
                " where table_schema = (select database()) and table_name = '" + tableName + "'")).toString();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<>();
        try {
            con = getConnection();
            ps = con.prepareStatement(SQLTables);
            rs = ps.executeQuery();
            for (; rs.next(); ) {
                map.put("tableName", rs.getString(1).toLowerCase());
                map.put("tableComment", rs.getString(2));
            }
        } catch (Exception e) {
            log.error("getTableInfo exception:{}", e.getMessage(), e);
        } finally {
            close(con, ps, rs);
        }
        return map;
    }

    /**
     * 获取表字段信息
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static List<Map<String, String>> getTableColumns(String tableName) throws SQLException {
        tableName = tableName.toLowerCase();
        String SQLColumns = (new StringBuilder(
                "select column_name columnName, data_type dataType, column_comment columnComment, column_key columnKey, extra from information_schema.columns" +
                        " where table_name = '" + tableName + "' and table_schema = (select database()) order by ordinal_position")).toString();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> columnList = new ArrayList<>();
        try {
            con = getConnection();
            ps = con.prepareStatement(SQLColumns);
            rs = ps.executeQuery();
            Map<String, String> map;
            for (; rs.next(); columnList.add(map)) {
                map = new HashMap<>();
                map.put("columnName", rs.getString(1).toUpperCase());
                map.put("dataType", rs.getString(2));
                map.put("columnComment", ObjectUtils.toString(rs.getString(3)));
                map.put("columnKey", rs.getString(4));
                map.put("extra", rs.getString(5));
            }
        } catch (Exception e) {
            log.error("getTableColumns exception:{}", e.getMessage(), e);
        } finally {
            close(con, ps, rs);
        }
        return columnList;
    }

    /**
     * 关闭连接
     *
     * @param con
     * @param ps
     * @param rs
     */
    private static void close(Connection con, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            log.error("close rs exception:{}", e.getMessage(), e);
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            log.error("close ps exception:{}", e.getMessage(), e);
        }
        try {
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
            log.error("close con exception:{}", e.getMessage(), e);
        }
    }
}
