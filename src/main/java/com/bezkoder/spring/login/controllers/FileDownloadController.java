package com.bezkoder.spring.login.controllers;

import com.bezkoder.spring.login.service.FileProcessor;
import jakarta.servlet.http.HttpServletRequest;

import com.bezkoder.spring.login.service.DatabaseFileService;

import com.bezkoder.spring.login.models.DatabaseFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:8080"}, allowCredentials = "true")
@RequestMapping("/api/auth")
@RestController
public class FileDownloadController {

    @Autowired
    private DatabaseFileService fileStorageService;



    @GetMapping("/downloadFileByCode/{verificationCode}")
    public ResponseEntity<Resource> downloadFileByCode(@PathVariable String verificationCode, HttpServletRequest request) {
        DatabaseFile databaseFile = fileStorageService.getFileByVerificationCode(verificationCode);

        try {
            byte[] data = databaseFile.getData().getBytes(1, (int) databaseFile.getData().length());
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(databaseFile.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + databaseFile.getFileName() + "\"")
                    .body(resource);
        } catch (SQLException e) {
            throw new RuntimeException("Error while reading blob data", e);
        }
    }

//    @CachePut(cacheNames = "file",  key = "#fileName")
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        DatabaseFile databaseFile = fileStorageService.getFile(fileName);

        try {
            byte[] data = databaseFile.getData().getBytes(1, (int) databaseFile.getData().length());
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(databaseFile.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + databaseFile.getFileName() + "\"")
                    .body(resource);
        } catch (SQLException e) {
            throw new RuntimeException("Error while reading blob data", e);
        }
    }






    // The generateContent method is commented out. If needed, it can be implemented.
    // @PostMapping("/a/{inputText:.+}")
    // public String generateContent(@PathVariable String inputText) throws Exception {
    //     // Implementation here
    // }

    @GetMapping("/a")
    public String generateContent() throws Exception {
        String input = "inputText";
        FileProcessor fileProcessor = new FileProcessor();
        String output = fileProcessor.generateContent(input);
        System.out.println("inputText: " + input);
        return input;
    }
}