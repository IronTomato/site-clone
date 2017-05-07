package com.irontomato.siteclone.retriable;

import com.irontomato.siteclone.common.CommonUtils;
import com.irontomato.siteclone.common.DateUtils;
import com.irontomato.siteclone.common.FileManager;
import com.irontomato.siteclone.entity.WebResource;
import com.irontomato.siteclone.repository.WebResourceRepository;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class WebResourceDownloadRetriable extends Retriable {

    private WebResourceRepository webResourceRepository;

    private RetriableExecutor retriableExecutor;

    private FileManager fileManager;

    private String url;

    private String urlDigeset;

    private byte[] content;

    private String mediaType;

    private WebResource webResource;

    public WebResourceDownloadRetriable() {
        super(5, 1000 * 60, 2000, -1);
    }

    @Autowired
    public void setWebResourceRepository(WebResourceRepository webResourceRepository) {
        this.webResourceRepository = webResourceRepository;
    }

    @Autowired
    public void setRetriableExecutor(RetriableExecutor retriableExecutor) {
        this.retriableExecutor = retriableExecutor;
    }

    @Autowired
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setUrl(String url) {
        this.url = url;
        this.urlDigeset = CommonUtils.digest(url);
    }

    @Override
    protected boolean call() {
        if (webResource == null) {
            webResource = webResourceRepository.findByUrlDigest(urlDigeset);
        }
        if (webResource == null) {
            webResource = new WebResource();
            webResource.setUrl(url);
            webResource.setUrlDigest(urlDigeset);
            webResource.setCreateTime(DateUtils.now());
            webResource.setDownloaded(false);
            webResourceRepository.save(webResource);
        }
        if (webResource.isDownloaded()) {
            cancel();
            return false;
        }
        log.info("Downloading :" + url);
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .build();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            mediaType = response.getFirstHeader("Content-Type").getValue();
            if (mediaType.contains(";")) {
                mediaType = mediaType.substring(0, mediaType.indexOf(";"));
            }
            content = EntityUtils.toByteArray(response.getEntity());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onCallSuccessed() {
        log.info("Download success :" + url);
        WebResource res = webResourceRepository.findByUrlDigest(urlDigeset);
        res.setUrlDigest(urlDigeset);
        res.setMediaType(mediaType);
        res.setUpdateTime(DateUtils.now());
        res.setDownloaded(true);
        String contentDigest = fileManager.store(content);
        res.setContentDigest(contentDigest);
        webResourceRepository.save(res);
    }
}
