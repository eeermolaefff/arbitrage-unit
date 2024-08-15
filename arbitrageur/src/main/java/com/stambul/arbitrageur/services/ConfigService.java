package com.stambul.arbitrageur.services;

import com.stambul.library.tools.IOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConfigService {
    private final IOService ioService;
    private Map<String, Object> config;
    @Value("${config.filename}")
    private String configFileName;

    @Autowired
    public ConfigService(IOService ioService) {
        this.ioService = ioService;
    }

    @SuppressWarnings("unchecked")
    public Object getConfig(String field) {
        if (config == null)
            config = (Map<String, Object>) ioService.parseJsonFromResourceFile(configFileName);
        return config.get(field);
    }
}
