package com.bezkoder.spring.login.repository;

import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.models.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    // 可以添加特定的查询方法（如果需要）

    void deleteByDatabaseFile(DatabaseFile databaseFile);
}
