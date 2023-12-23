package com.bezkoder.spring.login.controllers;

import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.payload.response.Response;
import com.bezkoder.spring.login.service.DatabaseFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequestMapping("/api/auth")
@RestController
@CrossOrigin(origins =  {"http://localhost:8081", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
public class FileUploadController {

    @Autowired
    private DatabaseFileService fileStorageService;
    Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @CachePut(cacheNames = "fileCache", key = "#result.fileName")
    @PostMapping("/uploadFile")
    public Response uploadFile(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis(); // Capture start time
        logger.info(String.valueOf(startTime));
        DatabaseFile fileName = fileStorageService.storeFile(file);

        long endTime = System.currentTimeMillis(); // Capture end time
        logger.info(String.valueOf(endTime));
        // Calculate elapsed time in milliseconds
        long elapsedTime = endTime - startTime;

        // Calculate upload speed in bytes per second
        long fileSize = file.getSize();
        double uploadSpeed = (double) fileSize / elapsedTime * 1000; // Convert to bytes per second
        logger.info(String.valueOf(uploadSpeed));

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName.getFileName())
                .toUriString();

        return new Response(fileName.getFileName(), fileDownloadUri,
                file.getContentType(), file.getSize());
    }


//    @PostMapping("/uploadMultipleFiles")
//    public List<Response> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
//        return Arrays.asList(files)
//                .stream()
//                .map(file -> uploadFile(file))
//                .collect(Collectors.toList());
//    }

    @PostMapping("/uploadMultipleFiles")
    public Response uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            // Create a ByteArrayOutputStream to hold the zipped files
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (MultipartFile file : files) {
                // Create a zip entry for each file
                ZipEntry zipEntry = new ZipEntry(file.getOriginalFilename());
                zos.putNextEntry(zipEntry);

                // Write the file bytes to the zip output stream
                zos.write(file.getBytes());

                zos.closeEntry();
            }

            // Close the ZipOutputStream to finish creating the ZIP file
            zos.close();

            // Convert the ByteArrayOutputStream to a byte array
            byte[] bytes = baos.toByteArray();

            // Convert byte[] to Blob
            Blob blob = new SerialBlob(bytes);

            // Create a DatabaseFile object for the ZIP file
            DatabaseFile dbFile = new DatabaseFile("uploadedFiles.zip", "application/zip", blob);

            // Save the DatabaseFile object to the database
            dbFile = fileStorageService.storeDatabaseFile(dbFile);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(dbFile.getFileName())
                    .toUriString();

            return new Response(dbFile.getFileName(), fileDownloadUri,
                    dbFile.getFileType(), bytes.length);
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Could not store file. Please try again!", e);
        }
    }
}
