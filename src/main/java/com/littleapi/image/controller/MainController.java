package com.littleapi.image.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.littleapi.image.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
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

    @GetMapping
    public List<String> getImages() throws IOException {
        return imageService.loadAll().map(
                        path -> MvcUriComponentsBuilder.fromMethodName(MainController.class,
                                "imageInfo", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/detailed")
    @ResponseBody
    public List<Resource> detailedInfo() throws IOException {
        return imageService.loadAll().map(path -> {
            try {
                return imageService.loadAsResource(path.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
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
