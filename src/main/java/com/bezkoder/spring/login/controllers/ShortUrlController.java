package com.bezkoder.spring.login.controllers;

import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.repository.DatabaseFileRepository;
import com.bezkoder.spring.login.service.DatabaseFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequestMapping("/surl")
@RestController
@CrossOrigin(origins =  {"http://localhost:8081", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
public class ShortUrlController {
    @Autowired
    private DatabaseFileService fileStorageService;



    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortUrl) {
        DatabaseFile databaseFile = fileStorageService.getFileByShortUrl(shortUrl);
        if (databaseFile == null) {
            return ResponseEntity.notFound().build();
        }

        String verificationCode = databaseFile.getVerificationCode();
        String redirectUrl = "http://localhost:8080/api/auth/downloadFileByCode/" + verificationCode;
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}

