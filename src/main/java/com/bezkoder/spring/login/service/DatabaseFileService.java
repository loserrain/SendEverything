package com.bezkoder.spring.login.service;

import com.bezkoder.spring.login.exception.FileNotFoundException;
import com.bezkoder.spring.login.exception.FileStorageException;
import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.models.QRCode;
import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.repository.DatabaseFileRepository;
import com.bezkoder.spring.login.repository.QRCodeRepository;
import com.bezkoder.spring.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DatabaseFileService {

    @Autowired
    private DatabaseFileRepository dbFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;


    public DatabaseFile storeDatabaseFile(DatabaseFile dbFile ,Optional<User> optionalUser) {
        dbFile.setVerificationCode(generateUniqueVerificationCode());
        optionalUser.ifPresent(dbFile::setUser);
        dbFile.setShortUrl(generateUniqueShortURL());
        return dbFileRepository.save(dbFile);
    }

    public DatabaseFile storeFile(MultipartFile file, Optional<User> optionalUser) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Blob blob = new SerialBlob(file.getBytes());

            DatabaseFile dbFile = new DatabaseFile(fileName, file.getContentType(), blob, Instant.now());
            dbFile.setVerificationCode(generateUniqueVerificationCode());
            // 如果用户存在，则关联用户
            optionalUser.ifPresent(dbFile::setUser);
            dbFile.setShortUrl(generateUniqueShortURL());

            return dbFileRepository.save(dbFile);
        } catch (IOException | SQLException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public DatabaseFile getFile(String filename) {
        System.out.println("getFile: " + filename);
        return dbFileRepository.findByFileName(filename)
                .orElseThrow(() -> new FileNotFoundException("File not found with filename " + filename));
    }

    public List<String> getAllFileNames() {
        List<DatabaseFile> files = dbFileRepository.findAllByOrderByTimestampDesc();
        return files.stream()
                .map(DatabaseFile::getFileName)
                .collect(Collectors.toList());
    }
    public DatabaseFile getFileByVerificationCode(String verificationCode) {
        return dbFileRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new FileNotFoundException("File not found with verification code " + verificationCode));
    }


    public List<String> getAllData() {
        List<DatabaseFile> files = dbFileRepository.findAllByOrderByTimestampDesc();
        return files.stream()
                .map(file -> {
                    try {
                        long fileSizeInBytes = file.getData().length();
                        long fileSizeInKB = fileSizeInBytes / 1024;
                        return fileSizeInKB + " KB";
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    private String generateUniqueVerificationCode() {
        Random random = new Random();
        String verificationCode;
        do {
            int code = random.nextInt(900000) + 100000;
            verificationCode = String.valueOf(code);
        } while (isCodeExists(verificationCode));

        return verificationCode;
    }
    public void deleteFile(String fileId) {
        DatabaseFile file = dbFileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id " + fileId));

        // 删除或更新 qrcode 表中的相关记录
        qrCodeRepository.deleteByDatabaseFile(file);

        // 删除 files 表中的记录
        dbFileRepository.deleteById(fileId);
    }

    private boolean isCodeExists(String code) {
        return dbFileRepository.existsById(code);
    }

    private String generateUniqueShortURL() {
        // 示例：生成一个随机的 6 位字符串
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder shortId = new StringBuilder(6);
        for (int i = 0; i < 8; i++) {
            shortId.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortId.toString();
    }
    public DatabaseFile getFileByShortUrl(String shortUrl) {
        return dbFileRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new FileNotFoundException("File not found with short URL " + shortUrl));
    }

}
