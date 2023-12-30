package com.bezkoder.spring.login.service;

import com.bezkoder.spring.login.models.DatabaseFile;
import com.bezkoder.spring.login.models.QRCode;
import com.bezkoder.spring.login.repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;

@Component
public class CodeGenerator {

    private  final DatabaseFileService fileStorageService;
    private final QRCodeRepository qrCodeRepository;

    @Autowired
    public CodeGenerator(DatabaseFileService fileStorageService, QRCodeRepository qrCodeRepository) {
        this.fileStorageService = fileStorageService;
        this.qrCodeRepository = qrCodeRepository;
    }
    public static Blob generateQRCodeImage(String text, int width, int height) throws WriterException, IOException, SQLException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Convert BufferedImage to byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageInByte = baos.toByteArray();

        // Convert byte[] to Blob
        Blob blob = new SerialBlob(imageInByte);

        return blob;
    }

    public Blob generateAndStoreQRCode(String text, int width, int height, DatabaseFile dbFile) throws WriterException, IOException, SQLException {
        // 生成 QR 码
        Blob qrCodeData = generateQRCodeImage(text, width, height);

        // 创建并保存 QRCode 实体
        QRCode qrCode = new QRCode();
        qrCode.setData(qrCodeData);
        qrCode.setDatabaseFile(dbFile);
        qrCodeRepository.save(qrCode);
        return qrCodeData;
    }

    public String blobToBase64(Blob blob) throws SQLException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try (InputStream in = blob.getBinaryStream()) {
            int n = 0;
            while ((n = in.read(buf)) >= 0) {
                baos.write(buf, 0, n);
            }
        }
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

//    public String generateAndStoreQRCode(String text, int width, int height,String qrcodeUrlName) {
//        // Generate QRCode image
//        Blob qrCodeImage;
//        try {
//            qrCodeImage = CodeGenerator.generateQRCodeImage(text, width, height);
//        } catch (WriterException | IOException e) {
//            throw new RuntimeException("Could not generate QR Code: " + e.getMessage());
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Create a DatabaseFile object for the QRCode image
//        DatabaseFile qrCodeDbFile = new DatabaseFile(qrcodeUrlName+".png", "image/png", qrCodeImage, Instant.now());
//
//        // Save the DatabaseFile object to the database
//        qrCodeDbFile = fileStorageService.storeDatabaseFile(qrCodeDbFile);
//
//        // Generate file download URI
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/downloadFile/")
//                .path(qrCodeDbFile.getFileName())
//                .toUriString();
//
//        return fileDownloadUri;
//    }








    public static String generateDownloadCode() {
        Random random = new Random();
        int downloadCode = random.nextInt(900000) + 100000; // This will generate a random number between 100000 and 999999
        return String.valueOf(downloadCode);
    }
}
