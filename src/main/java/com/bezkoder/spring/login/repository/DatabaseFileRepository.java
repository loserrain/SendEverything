package com.bezkoder.spring.login.repository;

import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatabaseFileRepository extends JpaRepository<DatabaseFile, String> {
    List<DatabaseFile> findAllByOrderByTimestampDesc();

    Optional<DatabaseFile> findByFileName(String fileName);

    Optional<DatabaseFile> findByVerificationCode(String verificationCode);
    void deleteById(String id);
    Optional<DatabaseFile> findByShortUrl(String shortUrl);


}