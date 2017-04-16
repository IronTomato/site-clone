package com.irontomato.siteclone.common;

import com.irontomato.siteclone.entity.WebResource;
import com.irontomato.siteclone.repository.WebResourceRepository;
import com.irontomato.siteclone.retriable.RetriableExecutor;
import com.irontomato.siteclone.retriable.WebResourceDowloadRetriableFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class Initializer {

    @Autowired
    private WebResourceRepository webResourceRepository;

    @Autowired
    private RetriableExecutor retriableExecutor;

    @Autowired
    private WebResourceDowloadRetriableFactory webResourceDowloadRetriableFactory;

    @PostConstruct
    public void init(){
        List<WebResource> resources = webResourceRepository.findAll();
        if (resources != null){
            for (WebResource resource : resources) {
                if (!resource.isDownloaded()){
                    retriableExecutor.execute(webResourceDowloadRetriableFactory.create(resource.getUrl()));
                }
            }
        }
    }
}
