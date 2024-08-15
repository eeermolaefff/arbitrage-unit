package com.stambul.initializers.services;

import com.stambul.library.tools.IOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class BlacklistService {
    private final Object blacklistMutex = new Object();
    private final IOService ioService;
    private final String blacklistFilePath;

    @Autowired
    public BlacklistService(
            IOService ioService,
            @Value("${coinmarketcap.blacklist.filepath}") String blacklistFilePath
    ) {
        this.ioService = ioService;
        this.blacklistFilePath = blacklistFilePath;
    }

    public void addObjectsToBlacklist(String className, Iterable<Map<String, Object>> blocks, String section) {
        synchronized (blacklistMutex) {
            Map<String, Object> blacklist = (Map<String, Object>) getJsonFromFile(blacklistFilePath);
            Map<String, Object> objectsBlacklist = (Map<String, Object>) blacklist.computeIfAbsent(className, x -> new HashMap<>());
            Map<String, Object> sectionBlacklist = (Map<String, Object>) objectsBlacklist.computeIfAbsent(section, x -> new HashMap<>());

            for (Map<String, Object> block : blocks) {
                Object id = block.get("id");
                if (id == null)
                    throw new RuntimeException("Block info object doesn't contain the \"id\" key: block=" + block);
                ((List<Object>) sectionBlacklist.computeIfAbsent(id.toString(), x -> new LinkedList<>())).add(block);
            }

            ioService.writeToFile(ioService.toJSONString(blacklist), blacklistFilePath);
        }
    }

    public Map<String, Object> getBlacklist(String className, String section) {
        synchronized (blacklistMutex) {
            Map<String, Object> blacklist = (Map<String, Object>) getJsonFromFile(blacklistFilePath);

            Map<String, Object> classBlacklist = ((Map<String, Object>) blacklist.get(className));
            if (classBlacklist == null)     return new HashMap<>();

            Map<String, Object> sectionBlacklist = (Map<String, Object>) classBlacklist.get(section);
            return Objects.requireNonNullElseGet(sectionBlacklist, HashMap::new);
        }
    }

    private Object getJsonFromFile(String fileName) {
        if (ioService.fileExist(fileName))  return ioService.parseJsonFromFile(fileName);
        else                                return new HashMap<>();
    }
}
