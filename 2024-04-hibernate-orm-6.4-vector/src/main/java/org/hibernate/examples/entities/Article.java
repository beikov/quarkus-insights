package org.hibernate.examples.entities;

import java.net.URI;
import java.net.URL;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "articles")
public class Article {
	@Id
	@JavaType(URIJavaType.class)
	@Column(nullable = false)
	public URI url;
	@Column(nullable = false)
	public String title;
	@Column(nullable = false)
	public String author;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(nullable = false)
	public String content;
	@Column
	@Array(length = 768)
	@JdbcTypeCode(SqlTypes.VECTOR)
	public float[] embedding;

	public Article() {
	}

	public Article(URI url, String title, String author, String content) {
		this.url = url;
		this.title = title;
		this.author = author;
		this.content = content;
	}
}
