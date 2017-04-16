package com.irontomato.siteclone.analyze;

import java.util.Collection;

public class AnalyzeResult {

    private String parsedHtml;

    private Collection<String> relatedUrls;

    public String getParsedHtml() {
        return parsedHtml;
    }

    public void setParsedHtml(String parsedHtml) {
        this.parsedHtml = parsedHtml;
    }

    public Collection<String> getRelatedUrls() {
        return relatedUrls;
    }

    public void setRelatedUrls(Collection<String> relatedUrls) {
        this.relatedUrls = relatedUrls;
    }
}
