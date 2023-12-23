package com.bezkoder.spring.login.models;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Blob;

@Entity
@Table(name = "files")
public class DatabaseFile {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	private String fileName;

	private String fileType;

	@Lob
	private Blob data;

	public DatabaseFile() {

	}

	public DatabaseFile(String fileName, String fileType, Blob data) {
		this.fileName = fileName;
		this.fileType = fileType;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public Blob getData() {
		return data;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setData(Blob data) {
		this.data = data;
	}
}