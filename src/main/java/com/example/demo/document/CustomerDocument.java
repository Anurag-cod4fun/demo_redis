package com.example.demo.document;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;

@Document(indexName = "customers")
public class CustomerDocument {

	@Id
	private String id;

	@TextIndexed
	private String name;

	@TagIndexed
	private String email;

	@TagIndexed
	private String status;

	@TextIndexed
	private String notes;

	public CustomerDocument() {
	}

	public CustomerDocument(String id, String name, String email, String status, String notes) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.status = status;
		this.notes = notes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}