package com.bezkoder.spring.login.models;

import com.bezkoder.spring.login.repository.DatabaseFileRepository;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Blob;
import java.time.Instant;
import java.util.Random;

@Entity
@Data
@Table(name = "files")
public class DatabaseFile {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private String fileName;

	private String fileType;
	private String verificationCode;
	@Lob
	private Blob data;
	@OrderBy("timestamp DESC")
	Instant timestamp = Instant.now();
	private String shortUrl;

	private transient DatabaseFileRepository databaseFileRepository;


	public DatabaseFile() {

	}

	public DatabaseFile(String fileName, String fileType, Blob data ,Instant timestamp) {
		this.fileName = fileName;
		this.fileType = fileType;
		this.data = data;
		this.timestamp = timestamp;
	}





	}

