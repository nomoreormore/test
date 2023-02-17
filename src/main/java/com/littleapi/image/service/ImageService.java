package com.littleapi.image.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public interface ImageService {
    void init() throws IOException;
    void deleteAll();
    void store(MultipartFile img);
    Stream<Path> loadAll() throws IOException;
    Path load(String imageName);
    Resource loadAsResource(String imageName) throws MalformedURLException;
}
