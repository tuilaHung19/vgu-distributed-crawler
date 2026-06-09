package com.vgu.dwc.crawler.parser;

import com.vgu.dwc.crawler.model.ArticleData;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VnExpressParser extends BaseParser {

    @Override
    public boolean supports(String url) {
        return url.contains("vnexpress.net");
    }

    @Override
    public ArticleData parse(String url, String workerId) throws IOException {
        Document doc = fetchDocumentWithRetry(url);

        String title = doc.select("h1.title-detail").text();
        if (title.isEmpty()) title = doc.select("h1").text();

        String author = doc.select("p.author_mail strong, p.author_mail a").text();
        if (author.isEmpty()) author = doc.select("article.fck_detail p.Normal[style*='text-align: right']").text();
        if (author.isEmpty()) author = "Không rõ tác giả";

        String publishedDate = doc.select("span.date").text();

        Elements paragraphs = doc.select("article.fck_detail p.Normal");
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            String text = paragraphs.get(i).text();
            if (!text.trim().isEmpty() && !text.equals(author)) {
                content.append(text).append("\n");
            }
        }

        String md5Hash = generateMd5(url);
        
        List<String> extractedTags = new ArrayList<>();

        // 2. CSS Selector đặc trưng của báo VnExpress (thường nằm ở thẻ div có class tags hoặc detail-tags)
        Elements tagElements = doc.select("div.tags a, span.tag a, div.detail-tags a");

        // 3. Lọc và lấy Text
        if (tagElements != null) {
        	for (Element tagEl : tagElements) {
        		String tagText = tagEl.text().trim();
        		// Đôi khi VnExpress có thêm dấu phẩy hoặc icon, hàm trim() sẽ dọn sạch
        		if (!tagText.isEmpty()) {
        			extractedTags.add(tagText);
             }
         }
     }

        return new ArticleData(
                url,
                md5Hash,
                workerId,
                "vnexpress.net",
                title,
                content.toString(),
                author,
                publishedDate,
                extractedTags
        );
    }
}