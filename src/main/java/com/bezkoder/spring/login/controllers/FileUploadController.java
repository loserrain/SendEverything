package com.bezkoder.spring.login.controllers;

import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.payload.response.FileListResponse;
import com.bezkoder.spring.login.payload.response.FileResponse;
import com.bezkoder.spring.login.repository.UserRepository;
import com.bezkoder.spring.login.service.CodeGenerator;
import com.bezkoder.spring.login.service.DatabaseFileService;
import com.bezkoder.spring.login.service.FileProcessor;
import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:8080"}, allowCredentials = "true")
@RequestMapping("/api/auth")
@RestController
public class FileUploadController {

    @Autowired
    private DatabaseFileService fileStorageService;

    @Autowired
    private CodeGenerator qrCodeService;

    @Autowired
    private UserRepository userRepository;

//    @CachePut(cacheNames = "fileCache", key = "#result.fileName  ")
    @PostMapping("/uploadFile")
    public FileResponse uploadFile(@RequestParam("file") MultipartFile file,Principal principal) throws SQLException, IOException, WriterException {
        Optional<User> optionalUser = Optional.empty();
        if (principal != null) {
            optionalUser = userRepository.findByUsername(principal.getName());
        }

        DatabaseFile dbFile = fileStorageService.storeFile(file,optionalUser);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/auth/downloadFile/")
                .path(dbFile.getFileName())
                .toUriString();
        String shortUrl="http:/localhost:8080/surl/"+dbFile.getShortUrl();
        System.out.println(shortUrl);


        Blob qrCodeBlob = qrCodeService.generateAndStoreQRCode(fileDownloadUri, 350, 350, dbFile);
        String qrCodeBase64 = qrCodeService.blobToBase64(qrCodeBlob);
        String verificationCode = dbFile.getVerificationCode();

        return new FileResponse(dbFile.getFileName(), shortUrl,
                file.getContentType(), (int) file.getSize(), verificationCode, qrCodeBase64);
    }

//    @GetMapping("/getFile")
//    public FileListResponse getFile(Principal principal){
//        System.out.println(principal);
//        List<String> fileNames = fileStorageService.getAllFileNames();
//        List<String> fileData = fileStorageService.getAllData();
//        return new FileListResponse(fileNames, fileData);
//    }


    @GetMapping("/getFile")
    public FileListResponse getFile(Principal principal){

        List<String> fileNames = fileStorageService.getAllFileNames();
        List<String> fileData = fileStorageService.getAllData();
        System.out.println(fileNames);
        return new FileListResponse(fileNames, fileData);
    }

    @GetMapping("/classify")
    public ResponseEntity<String> classify() throws Exception {

        List<String> fileNames = fileStorageService.getAllFileNames();
        FileProcessor fileProcessor = new FileProcessor();
        String jsonString = fileProcessor.generateContent(fileNames.toString());
        jsonString = jsonString.replace("```json\n", "").replace("```", "");
        JSONObject jsonObject = new JSONObject(jsonString);

        System.out.println("inputText: " + fileNames.toString());
        return ResponseEntity.ok().body(jsonObject.toString());
    }



//    @PostMapping("/uploadMultipleFiles")S
//    public List<Response> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
//        return Arrays.asList(files)
//                .stream()
//                .map(file -> uploadFile(file))
//                .collect(Collectors.toList());
//    }



    @PostMapping("/uploadMultipleFiles")
    public FileResponse uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,Principal principal) {
        try {
            // Create a ByteArrayOutputStream to hold the zipped files
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (MultipartFile file : files) {
                // Create a zip entry for each file
                ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(file.getOriginalFilename()));
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
            Optional<User> optionalUser = Optional.empty();
            if (principal != null) {
                optionalUser = userRepository.findByUsername(principal.getName());
            }

            // Create a DatabaseFile object for the ZIP file
            DatabaseFile dbFile = new DatabaseFile("uploadedFiles.zip","application/zip", blob, Instant.now());

            // Save the DatabaseFile object to the database
            dbFile = fileStorageService.storeDatabaseFile(dbFile,optionalUser);
            String shortUrl="http:/localhost:8080/surl/"+dbFile.getShortUrl();

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(dbFile.getFileName())
                    .toUriString();

            String verificationCode = dbFile.getVerificationCode();

            Blob qrCodeBlob = qrCodeService.generateAndStoreQRCode(fileDownloadUri, 350, 350, dbFile);

            String qrCodeBase64 = qrCodeService.blobToBase64(qrCodeBlob);

            return new FileResponse(dbFile.getFileName(), shortUrl,
                    dbFile.getFileType(), bytes.length,  verificationCode, qrCodeBase64);
        } catch (IOException | SQLException | WriterException e) {
            throw new RuntimeException("Could not store file. Please try again!", e);
        }
    }
}
