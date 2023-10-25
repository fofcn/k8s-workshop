package com.epam.workshop.sales.product.client.app;

import com.epam.workshop.sales.product.infrastructure.config.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/v1/order/file/upload")
public class FileUploadController {

    @Autowired
    private StorageConfig storageConfig;

    @PostMapping
    public String upload(@RequestParam("file")MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, Paths.get(storageConfig.getDir() + file.getOriginalFilename()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        return "OK";
    }
}
