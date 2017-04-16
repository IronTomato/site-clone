package com.irontomato.siteclone.analyze;

import com.irontomato.siteclone.common.CommonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

@Component
public class HtmlAnalyzer {

    public AnalyzeResult analyze(String url, String html) {
        UrlResource base;
        try {
            base = new UrlResource(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("base url illegal");
        }
        AnalyzeResult result = new AnalyzeResult();
        Document doc = Jsoup.parse(html);
        doc.select(".ad-list").remove();
        Set<String> relatedUrls = new HashSet<>();

        doc.select("a[href],link[href]").forEach(e -> {
            String href = e.attr("href");
            if (validUrl(href)) {
                try {
                    String related = base.createRelative(href).getURI().toString();
                    e.attr("href", "/res/" + CommonUtils.digest(related));
                    relatedUrls.add(related);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        doc.select("img[src],script[src]").forEach(e -> {
            String src = e.attr("src");
            if (validUrl(src)) {
                try {
                    String related = base.createRelative(src).getURI().toString();
                    e.attr("src", "/res/" + CommonUtils.digest(related));
                    relatedUrls.add(related);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        result.setRelatedUrls(relatedUrls);
        result.setParsedHtml(doc.html());
        return result;
    }

    private boolean validUrl(String url) {
        return !(url.startsWith("#") || url.startsWith("javascript"));
    }


}
