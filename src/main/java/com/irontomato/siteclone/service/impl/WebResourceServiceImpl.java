package com.irontomato.siteclone.service.impl;

import com.irontomato.siteclone.analyze.AnalyzeResult;
import com.irontomato.siteclone.analyze.HtmlAnalyzer;
import com.irontomato.siteclone.common.CommonUtils;
import com.irontomato.siteclone.common.UrlDigestCache;
import com.irontomato.siteclone.entity.WebResource;
import com.irontomato.siteclone.entity.dto.WebResourceDto;
import com.irontomato.siteclone.repository.WebResourceRepository;
import com.irontomato.siteclone.retriable.RetriableExecutor;
import com.irontomato.siteclone.retriable.WebResourceDowloadRetriableFactory;
import com.irontomato.siteclone.service.WebResourceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.locks.Lock;

@Service("webResourceService")
public class WebResourceServiceImpl implements WebResourceService {

    private WebResourceRepository webResourceRepository;

    private HtmlAnalyzer htmlAnalyzer;

    private UrlDigestCache urlDigestCache;

    private RetriableExecutor retriableExecutor;

    private WebResourceDowloadRetriableFactory webResourceDowloadRetriableFactory;

    @Autowired
    public void setWebResourceRepository(WebResourceRepository webResourceRepository) {
        this.webResourceRepository = webResourceRepository;
    }

    @Autowired
    public void setHtmlAnalyzer(HtmlAnalyzer htmlAnalyzer) {
        this.htmlAnalyzer = htmlAnalyzer;
    }

    @Autowired
    public void setUrlDigestCache(UrlDigestCache urlDigestCache) {
        this.urlDigestCache = urlDigestCache;
    }

    @Autowired
    public void setRetriableExecutor(RetriableExecutor retriableExecutor) {
        this.retriableExecutor = retriableExecutor;
    }

    @Autowired
    public void setWebResourceDowloadRetriableFactory(WebResourceDowloadRetriableFactory webResourceDowloadRetriableFactory) {
        this.webResourceDowloadRetriableFactory = webResourceDowloadRetriableFactory;
    }

    @Override
    public Optional<WebResourceDto> get(String urlDigest) {
        WebResource res = webResourceRepository.findByUrlDigest(urlDigest);
        if (res == null) {
            return Optional.empty();
        }

        WebResourceDto dto = new WebResourceDto();
        BeanUtils.copyProperties(res, dto);
        if (res.isDownloaded() && MediaType.TEXT_HTML_VALUE.equalsIgnoreCase(res.getMediaType())) {
            String html = new String(res.getContent());

            AnalyzeResult analyzeResult = htmlAnalyzer.analyze(res.getUrl(), html);
            dto.setContent(analyzeResult.getParsedHtml().getBytes());

            analyzeResult.getRelatedUrls().forEach(u -> {
                String digest = CommonUtils.digest(u);
                if (!urlDigestCache.contains(digest)) {
                    retriableExecutor.execute(webResourceDowloadRetriableFactory.create(u));
                    urlDigestCache.add(digest);
                }
            });
        }
        return Optional.of(dto);
    }
}
