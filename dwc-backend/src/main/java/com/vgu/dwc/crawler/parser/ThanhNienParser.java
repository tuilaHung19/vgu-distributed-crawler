package com.vgu.dwc.crawler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vgu.dwc.crawler.model.ArticleData;

public class ThanhNienParser extends BaseParser {

    @Override
    public boolean supports(String url) {
        return url.contains("thanhnien.vn");
    }

    @Override
    public ArticleData parse(String url, String workerId) throws IOException {
        Document doc = fetchDocumentWithRetry(url);

        String md5Hash = generateMd5(url);
        
        String title = doc.select("h1[data-role=title]").text();
        if (title.isEmpty()) title = doc.select("h1.detail-title").text();

        String author = doc.select("div.detail-author a").text();
        if (author.isEmpty()) author = doc.select(".author-info .name").text();
        if (author.isEmpty()) author = "Không rõ tác giả";

        String publishedDate = doc.select("div[data-role=publishdate]").text();
        if (publishedDate.isEmpty()) publishedDate = doc.select(".detail-time").text();

        Elements paragraphs = doc.select("div.detail-content p, div[data-role=content] p");
        
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            String text = paragraphs.get(i).text();
            if (!text.trim().isEmpty()) {
                content.append(text).append("\n");
            }
        }

        // 1. Tạo một List rỗng để chứa tag
        List<String> extractedTags = new ArrayList<>();

        // 2. Tìm CSS Selector của khu vực chứa Tag trên trang web
        // VÍ DỤ: Trên báo Thanh Niên có thể là thẻ div mang class "detail-tags" chứa các thẻ <a>
        Element tagElement = doc.selectFirst("div.detail-cate a"); // <-- Bạn F12 trên web để sửa lại chuỗi này cho đúng nhé

        // 3. Vòng lặp lấy Text của từng tag bỏ vào mảng
        if (tagElement != null) {
            extractedTags.add(tagElement.text());
        }

        return new ArticleData(
                url,
                md5Hash,
                workerId,
                "thanhnien.vn",
                title,
                content.toString(),
                author,
                publishedDate,
                extractedTags
        );
    }
}