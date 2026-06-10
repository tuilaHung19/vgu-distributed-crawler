package com.vgu.dwc.crawler.model;

import java.util.List;

public class ArticleData {
    private String url;
    private String md5Hash;
    private String workerId;
    private String sourceDomain;
    private String title;
    private String content;
    private String author;
    private String publishedDate;
    private List<String> tags;

    public ArticleData(String url, String md5Hash, String workerId, String sourceDomain, 
                       String title, String content, String author, String publishedDate, List<String> tags) {
        this.url = url;
        this.md5Hash = md5Hash;
        this.workerId = workerId;
        this.sourceDomain = sourceDomain;
        this.title = title;
        this.content = content;
        this.author = author;
        this.publishedDate = publishedDate;
        this.tags = tags;
    }

    public String getUrl() { return url; }
    public String getMd5Hash() { return md5Hash; }
    public String getWorkerId() { return workerId; }
    public String getSourceDomain() { return sourceDomain; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getPublishedDate() { return publishedDate; }
    public List<String> getTags() { return tags; }
}