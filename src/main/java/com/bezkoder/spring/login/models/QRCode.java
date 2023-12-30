package com.bezkoder.spring.login.models;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Blob;

@Entity
@Data
@Table(name = "qrcodes")
public class QRCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private Blob data; // QR 码图像数据

    @OneToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private DatabaseFile databaseFile; // 关联的文件

    // 构造函数、getter 和 setter
}
