package com.irontomato.siteclone.repository;

import com.irontomato.siteclone.entity.WebResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebResourceRepository extends JpaRepository<WebResource, Integer> {
    WebResource findByUrl(String url);

    WebResource findByUrlDigest(String urlDigest);
}
