package com.bezkoder.spring.login.service;

import com.bezkoder.spring.login.exception.FileNotFoundException;
import com.bezkoder.spring.login.exception.FileStorageException;
import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.repository.DatabaseFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Objects;

@Service
public class DatabaseFileService {

    @Autowired
    private DatabaseFileRepository dbFileRepository;

    public DatabaseFile storeDatabaseFile(DatabaseFile dbFile) {
        return dbFileRepository.save(dbFile);
    }
    public DatabaseFile storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Convert byte[] to Blob
            Blob blob = new SerialBlob(file.getBytes());

            DatabaseFile dbFile = new DatabaseFile(fileName, file.getContentType(), blob);

            return dbFileRepository.save(dbFile);
        } catch (IOException | SQLException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public DatabaseFile getFile(String fileId) {
        return dbFileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id " + fileId));
    }



}
