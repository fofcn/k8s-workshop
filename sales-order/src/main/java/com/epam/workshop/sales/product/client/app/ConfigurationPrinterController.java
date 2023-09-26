package com.epam.workshop.sales.product.client.app;

import com.epam.workshop.sales.product.client.dto.DatabaseConfigDto;
import com.epam.workshop.sales.product.infrastructure.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configuration/print")
public class ConfigurationPrinterController {

    @Autowired
    private DatabaseConfig databaseConfig;

    @GetMapping
    public DatabaseConfigDto getDatabaseConfig() {
        return new DatabaseConfigDto(databaseConfig.getJdbcUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
    }
}
