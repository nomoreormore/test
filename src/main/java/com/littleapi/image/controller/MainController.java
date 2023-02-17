package com.littleapi.image.controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.littleapi.image.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


//1) POST /api/images/ - загрузка массива картинок через один запрос во временную папку локального сервера
//2) GET /api/images/ - выдача всей мета (EXIF) информации о каждой фотографии,
// содержащейся во временной папке локального сервера

@RestController
@RequestMapping("/api/images")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MainController {
    private final ImageService imageService;

    @Autowired
    public MainController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping()
    @ResponseBody
    public List<String> getImages() throws IOException, ImageProcessingException {
        List<Resource> images = imageService.loadAll().map(path -> {
            try {
                return imageService.loadAsResource(path.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        List<String> imageMeta = new ArrayList<>();
        for (Resource img : images) {
            Metadata metadata = ImageMetadataReader.readMetadata(img.getFile());
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (directory.getName().contains("Exif")) imageMeta.add(img.getFilename() + " " + tag.getTagName() + " " + tag.getDescription());
                }
            }
        }
        return imageMeta;
    }

    @GetMapping("/{imageName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> imageInfo(@PathVariable String imageName) throws IOException {
        Resource img = imageService.loadAsResource(imageName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; imageName=\"" + img.getFilename() + "\"" +
                        img.getFilename() + "" + "\"" + img.getDescription() + "\"").body(img);
    }

    @PostMapping
    public void addImages(@RequestParam("img") MultipartFile[] images){
        for (MultipartFile image: images){
            imageService.store(image);
        }
    }
}
