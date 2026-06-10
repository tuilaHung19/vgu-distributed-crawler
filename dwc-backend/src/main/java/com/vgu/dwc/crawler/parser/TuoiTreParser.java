package com.vgu.dwc.crawler.parser;

import com.vgu.dwc.crawler.model.ArticleData;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TuoiTreParser extends BaseParser {

    @Override
    public boolean supports(String url) {
        return url.contains("tuoitre.vn");
    }

    @Override
    public ArticleData parse(String url, String workerId) throws IOException {
        Document doc = fetchDocumentWithRetry(url);

        String title = doc.select("h1.detail-title").text();
        if (title.isEmpty()) title = doc.select("h1").text();

        String author = doc.select(".author-name").text();
        if (author.isEmpty()) author = doc.select(".name").first() != null ? doc.select(".name").first().text() : "Không rõ tác giả";

        String publishedDate = doc.select(".date-time").text();
        if (publishedDate.isEmpty()) publishedDate = doc.select(".detail-time").text();

        Elements paragraphs = doc.select(".detail-content p");
        if (paragraphs.isEmpty()) paragraphs = doc.select("article p");

        StringBuilder content = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            String text = paragraphs.get(i).text();
            if (!text.trim().isEmpty()) {
                content.append(text).append("\n");
            }
        }

        String md5Hash = generateMd5(url);
        
        List<String> extractedTags = new ArrayList<>();
        // 2. CSS Selector đặc trưng của báo Tuổi Trẻ (thường nằm ở class detail__tag hoặc tags-wrapper)
        Element tagElement = doc.selectFirst("div.detail-cate a");
        if (tagElement != null) {
            // Lấy chữ "Thể thao" và đưa vào mảng Tags
            String category = tagElement.text();
            extractedTags.add(category); // Đưa vào mảng extractedTags
        } 

        return new ArticleData(
                url,
                md5Hash,
                workerId, 
                "tuoitre.vn",
                title,
                content.toString(),
                author,
                publishedDate,
                extractedTags
        );
    }
}