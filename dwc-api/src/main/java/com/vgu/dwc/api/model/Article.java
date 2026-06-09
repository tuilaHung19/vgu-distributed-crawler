package com.vgu.dwc.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;
import java.util.List;

@Document(collection = "articles")
public class Article {
    
	// Primary key 
    @Id
    private String id;	    

    @Indexed(unique = true)
    private String url;

    @Indexed(unique = true)
    @Field("md5_hash")
    private String md5Hash;

    @Field("worker_id")
    private String workerId;

    @Field("source_domain")
    private String sourceDomain;

    @Field("crawled_at")
    private Date crawledAt = new Date(); 

    private String title;
    private String content;
    private String author;

    @Field("published_date")
    private String publishedDate;

    private List<String> tags;

    // Spring Boot requires an empty Constructor
    public Article() {}

	public String getId() { return id; }
	public String getUrl() { return url; }
	public String getMd5Hash() { return md5Hash; }
	public String getWorkerId() { return workerId; }
	public String getSourceDomain() { return sourceDomain; }
	public Date getCrawledAt() { return crawledAt; }
	public String getTitle() { return title; }
	public String getContent() { return content; }
	public String getAuthor() { return author; }
	public String getPublishedDate() { return publishedDate; }
	public List<String> getTags() { return tags; }
	
	public void setId(String id) { this.id = id; }
	public void setUrl(String url) { this.url = url; }
	public void setMd5Hash(String md5Hash) { this.md5Hash = md5Hash; }
	public void setWorkerId(String workerId) { this.workerId = workerId; }
	public void setSourceDomain(String sourceDomain) { this.sourceDomain = sourceDomain; }
	public void setCrawledAt(Date crawledAt) { this.crawledAt = crawledAt; }
	public void setTitle(String title) { this.title = title; }
	public void setContent(String content) { this.content = content; }
	public void setAuthor(String author) { this.author = author; }
	public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
	public void setTags(List<String> tags) { this.tags = tags; }
	
}