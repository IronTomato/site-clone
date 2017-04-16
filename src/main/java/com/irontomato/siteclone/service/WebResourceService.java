package com.irontomato.siteclone.service;

import com.irontomato.siteclone.entity.WebResource;
import com.irontomato.siteclone.entity.dto.WebResourceDto;

import java.util.Optional;

public interface WebResourceService {
    Optional<WebResourceDto> get(String urlDigest);
}
