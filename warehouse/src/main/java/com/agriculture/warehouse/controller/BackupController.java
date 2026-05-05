package com.agriculture.warehouse.controller;

import com.agriculture.warehouse.annotation.Log;
import com.agriculture.warehouse.annotation.RequireRole;
import com.agriculture.warehouse.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/backup")
@Tag(name = "数据备份", description = "数据库备份管理")
public class BackupController {

    @Autowired
    private BackupService backupService;

    /**
     * 执行备份
     */
    @PostMapping
    @Operation(summary = "执行数据库备份")
    @RequireRole({"ADMIN"})
    @Log(module = "数据备份", operation = "执行数据库备份")
    public String backup() {
        try {
            String fileName = backupService.backupDatabase();
            return "备份成功，文件名：" + fileName;
        } catch (Exception e) {
            return "备份失败：" + e.getMessage();
        }
    }

    /**
     * 获取备份文件列表
     */
    @GetMapping("/files")
    @Operation(summary = "获取备份文件列表")
    @RequireRole({"ADMIN"})
    public List<BackupService.BackupFileInfo> getBackupFiles() {
        return backupService.getBackupFiles();
    }

    /**
     * 下载备份文件
     */
    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载备份文件")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName) {
        File file = new File("./backups/" + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 删除备份文件
     */
    @DeleteMapping("/{fileName}")
    @Operation(summary = "删除备份文件")
    @RequireRole({"ADMIN"})
    @Log(module = "数据备份", operation = "删除备份文件")
    public String deleteBackup(@PathVariable String fileName) {
        boolean success = backupService.deleteBackupFile(fileName);
        return success ? "删除成功" : "删除失败";
    }
}