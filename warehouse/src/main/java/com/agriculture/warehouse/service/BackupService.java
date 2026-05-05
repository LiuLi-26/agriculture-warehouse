package com.agriculture.warehouse.service;

import com.agriculture.warehouse.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BackupService {

    @Autowired
    private DataSource dataSource;

    // 备份文件存储目录
    private static final String BACKUP_DIR = "./backups/";

    // 需要备份的表
    private static final String[] TABLES = {
            "user", "product", "storage_location",
            "inbound_record", "outbound_record", "operation_log",
            "environment_monitor", "environment_threshold",
            "supplier", "waste_record", "inventory_check", "system_config"
    };

    /**
     * 执行数据库备份（Java 方式）
     */
    public String backupDatabase() throws Exception {
        // 创建备份目录
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // 生成备份文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "backup_" + timestamp + ".sql";
        String backupFilePath = BACKUP_DIR + backupFileName;

        try (Connection conn = dataSource.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFilePath))) {

            // 写入头部注释
            writer.println("-- MySQL dump");
            writer.println("-- Backup created: " + LocalDateTime.now());
            writer.println("-- Database: agriculture_warehouse");
            writer.println("-- ------------------------------------------------------");
            writer.println();

            // 备份每个表
            for (String tableName : TABLES) {
                backupTable(conn, writer, tableName);
            }

            writer.println("-- Dump completed");

        } catch (Exception e) {
            throw new BusinessException(500, "备份失败：" + e.getMessage());
        }

        return backupFileName;
    }

    /**
     * 备份单个表
     */
    private void backupTable(Connection conn, PrintWriter writer, String tableName) throws SQLException {
        // 检查表是否存在
        if (!tableExists(conn, tableName)) {
            return;
        }

        // 获取表结构
        String createTableSql = getCreateTableSql(conn, tableName);
        if (createTableSql != null) {
            writer.println("--");
            writer.println("-- Table structure for table `" + tableName + "`");
            writer.println("--");
            writer.println("DROP TABLE IF EXISTS `" + tableName + "`;");
            writer.println(createTableSql + ";");
            writer.println();
        }

        // 获取表数据
        backupTableData(conn, writer, tableName);
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(null, null, tableName, null);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }

    /**
     * 获取创建表的 SQL
     */
    private String getCreateTableSql(Connection conn, String tableName) throws SQLException {
        String sql = "SHOW CREATE TABLE " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(2);
            }
        }
        return null;
    }

    /**
     * 备份表数据
     */
    private void backupTableData(Connection conn, PrintWriter writer, String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // 获取列名
            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(meta.getColumnName(i));
            }

            // 构建 INSERT 语句
            StringBuilder insertPrefix = new StringBuilder("INSERT INTO `" + tableName + "` (`");
            insertPrefix.append(String.join("`, `", columnNames));
            insertPrefix.append("`) VALUES ");

            boolean hasData = false;
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append(insertPrefix);

            while (rs.next()) {
                if (hasData) {
                    insertBuilder.append(",");
                }
                insertBuilder.append("(");
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        insertBuilder.append(",");
                    }
                    Object value = rs.getObject(i);
                    insertBuilder.append(formatValue(value));
                }
                insertBuilder.append(")");
                hasData = true;
            }

            if (hasData) {
                writer.println("--");
                writer.println("-- Dumping data for table `" + tableName + "`");
                writer.println("--");
                writer.println(insertBuilder.toString() + ";");
                writer.println();
            }
        }
    }

    /**
     * 格式化 SQL 值
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "1" : "0";
        }
        if (value instanceof Timestamp) {
            return "'" + value.toString() + "'";
        }
        if (value instanceof Date) {
            return "'" + value.toString() + "'";
        }
        // 字符串类型，需要转义
        String str = value.toString();
        str = str.replace("\\", "\\\\");
        str = str.replace("'", "\\'");
        return "'" + str + "'";
    }

    /**
     * 获取所有备份文件列表
     */
    public List<BackupFileInfo> getBackupFiles() {
        List<BackupFileInfo> files = new ArrayList<>();
        File backupDir = new File(BACKUP_DIR);

        if (backupDir.exists()) {
            File[] fileList = backupDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile() && file.getName().endsWith(".sql")) {
                        BackupFileInfo info = new BackupFileInfo();
                        info.setFileName(file.getName());
                        info.setFileSize(file.length());
                        info.setCreateTime(LocalDateTime.ofEpochSecond(file.lastModified() / 1000, 0, java.time.ZoneOffset.ofHours(8)));
                        files.add(info);
                    }
                }
            }
        }

        // 按创建时间倒序排列
        files.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return files;
    }

    /**
     * 删除备份文件
     */
    public boolean deleteBackupFile(String fileName) {
        File file = new File(BACKUP_DIR + fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 备份文件信息内部类
     */
    public static class BackupFileInfo {
        private String fileName;
        private Long fileSize;
        private LocalDateTime createTime;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        public String getFileSizeStr() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.2f KB", fileSize / 1024.0);
            } else {
                return String.format("%.2f MB", fileSize / (1024.0 * 1024));
            }
        }
    }
}