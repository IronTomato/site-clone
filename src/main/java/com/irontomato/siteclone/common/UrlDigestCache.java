package com.irontomato.siteclone.common;

import com.irontomato.siteclone.entity.WebResource;
import com.irontomato.siteclone.repository.WebResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component
public class UrlDigestCache {

    private Set<String> store = new HashSet<>();

    private WebResourceRepository webResourceRepository;


    @Autowired
    public void setWebResourceRepository(WebResourceRepository webResourceRepository) {
        this.webResourceRepository = webResourceRepository;
    }

    public boolean contains(String urlDigest) {
        return store.contains(urlDigest);
    }

    public void add(String urlDigest){
        store.add(urlDigest);
    }

    @PostConstruct
    public void init(){
        webResourceRepository.findAll()
                .stream()
                .map(WebResource::getUrlDigest)
                .forEach(store::add);
    }
}
