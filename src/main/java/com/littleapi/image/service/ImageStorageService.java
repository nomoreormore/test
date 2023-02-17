package com.littleapi.image.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService implements ImageService {

    private final Path rootLocation;

    @Autowired
    public ImageStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void init() throws IOException {
        Files.createDirectories(rootLocation);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void store(MultipartFile img) {
        Path destinationImg = this.rootLocation.resolve(
                Paths.get(Objects.requireNonNull(img.getOriginalFilename()))).normalize().toAbsolutePath();
        try (InputStream inputStream = img.getInputStream()) {
            Files.copy(inputStream, destinationImg,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<Path> loadAll() throws IOException {
        return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
    }

    @Override
    public Path load(String imageName) {
        return rootLocation.resolve(imageName);
    }

    @Override
    public Resource loadAsResource(String imageName) throws MalformedURLException {
        Path img = load(imageName);
        Resource resource = new UrlResource(img.toUri());
        return resource;
    }


}
