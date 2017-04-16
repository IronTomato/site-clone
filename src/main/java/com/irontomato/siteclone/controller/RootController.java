package com.irontomato.siteclone.controller;

import com.irontomato.siteclone.Application;
import com.irontomato.siteclone.common.CommonUtils;
import com.irontomato.siteclone.common.DateUtils;
import com.irontomato.siteclone.entity.Origin;
import com.irontomato.siteclone.entity.WebResource;
import com.irontomato.siteclone.entity.dto.WebResourceDto;
import com.irontomato.siteclone.exception.MediaTypeNotSupportException;
import com.irontomato.siteclone.exception.ResourceNotFoundException;
import com.irontomato.siteclone.repository.OriginRepository;
import com.irontomato.siteclone.repository.WebResourceRepository;
import com.irontomato.siteclone.retriable.RetriableExecutor;
import com.irontomato.siteclone.retriable.WebResourceDownloadRetriable;
import com.irontomato.siteclone.service.WebResourceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class RootController {

    private OriginRepository originRepository;

    private WebResourceRepository webResourceRepository;

    private RetriableExecutor retriableExecutor;

    private WebResourceService webResourceService;

    @Autowired
    public void setOriginRepository(OriginRepository originRepository) {
        this.originRepository = originRepository;
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
    public void setWebResourceService(WebResourceService webResourceService) {
        this.webResourceService = webResourceService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/res/{urlDigest}")
    public String webResource(@PathVariable String urlDigest) {
        WebResourceDto res = resource(urlDigest);
        if (!res.isDownloaded()) {
            return "redirect:" + res.getUrl();
        }
        if (StringUtils.isEmpty(res.getMediaType())) {
            return "forward:/plainResource/" + urlDigest;
        }
        switch (res.getMediaType()) {
            case MediaType.TEXT_HTML_VALUE:
                return "forward:/htmlResource/" + urlDigest;
            case MediaType.IMAGE_JPEG_VALUE:
                return "forward:/imageResource/" + urlDigest;
            default:
                return "forward:/plainResource/" + urlDigest;
        }
    }

    @RequestMapping(value = "/htmlResource/{urlDigest}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public byte[] htmlResource(@PathVariable String urlDigest) {
        return resource(urlDigest).getContent();
    }

    @RequestMapping(value = "/imageResource/{urlDigest}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] imageResource(@PathVariable String urlDigest) {
        return resource(urlDigest).getContent();
    }

    @RequestMapping(value = "/plainResource/{urlDigest}", produces = {"text/css", "application/javascript", MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public byte[] plainResource(@PathVariable String urlDigest) {
        return resource(urlDigest).getContent();
    }

    @RequestMapping("/clone")
    @ResponseBody
    public String clone(String url) {
        String urlDigeset = CommonUtils.digest(url);

        Origin example = new Origin();
        example.setUrlDigest(urlDigeset);
        if (originRepository.exists(Example.of(example))) {
            return urlDigeset;
        }
        Origin origin = new Origin();
        origin.setUrl(url);
        origin.setUrlDigest(urlDigeset);
        origin.setCreateTime(DateUtils.now());
        originRepository.save(origin);

        WebResourceDownloadRetriable retriable = Application.CONTEXT.getBean(WebResourceDownloadRetriable.class);
        retriable.setUrl(url);
        retriableExecutor.execute(retriable);
        return CommonUtils.digest(url);
    }

    @GetMapping("/origins")
    @ResponseBody
    public List<Origin> origins() {
        return originRepository.findAll();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public String resourceNotFound() {
        return "404 Not Found";
    }

    @ExceptionHandler(MediaTypeNotSupportException.class)
    @ResponseBody
    public String mediaTypeNotSupport() {
        return "Media Type Not Support";
    }

    private void notSupport() {
        throw new MediaTypeNotSupportException();
    }

    private WebResourceDto resource(String urlDigest) {
        Optional<WebResourceDto> res = webResourceService.get(urlDigest);
        if (res.isPresent()) {
            return res.get();
        } else {
            throw new ResourceNotFoundException();
        }
    }
}
