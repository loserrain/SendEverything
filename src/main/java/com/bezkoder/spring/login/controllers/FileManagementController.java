package com.bezkoder.spring.login.controllers;

import com.bezkoder.spring.login.service.DatabaseFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class FileManagementController {

    @Autowired
    private DatabaseFileService fileStorageService;

    @DeleteMapping("/deleteFile/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        fileStorageService.deleteFile(fileId);
        System.out.println("deleteFile"+fileId);
        return ResponseEntity.ok().build();
    }
}