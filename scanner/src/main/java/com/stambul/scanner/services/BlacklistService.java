package com.stambul.scanner.services;

import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.Pair;
import com.stambul.library.tools.TimeTools;
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
            @Value("${blacklist.filepath}") String blacklistFilePath
    ) {
        this.ioService = ioService;
        this.blacklistFilePath = blacklistFilePath;
    }

    public <T> void addObjectsToBlacklist(String className, Iterable<Pair<T, Exception>> blockInfo) {
        synchronized (blacklistMutex) {
            Map<String, Object> blacklist = (Map<String, Object>) getJsonFromFile(blacklistFilePath);

            if (!blacklist.containsKey(className))
                blacklist.put(className, new TreeMap<>());

            Map<String, Object> objectsBlacklist = (Map<String, Object>) blacklist.get(className);
            for (Pair<T, Exception> objectInfo : blockInfo) {
                Exception reason = objectInfo.getSecond();
                T blockedObject = objectInfo.getFirst();
                objectsBlacklist.put(toKey(blockedObject), toMap(blockedObject, reason));
            }

            ioService.writeToFile(ioService.toJSONString(blacklist), blacklistFilePath);
        }
    }

    public List<Object> getBlacklist(String className) {
        synchronized (blacklistMutex) {
            Map<String, Object> blacklist = (Map<String, Object>) getJsonFromFile(blacklistFilePath);
            if (blacklist.containsKey(className))   return (List<Object>) blacklist.get(className);
            else                                    return new LinkedList<>();
        }
    }

    private Object getJsonFromFile(String fileName) {
        if (ioService.fileExist(fileName))  return ioService.parseJsonFromFile(fileName);
        else                                return new TreeMap<>();
    }

    private <T> String toKey(T blockedObject) {
        if (blockedObject instanceof Identifiable identifiable)
            return identifiable.getId().toString();
        return blockedObject.toString();
    }

    private <T> Map<String, Object> toMap(T blockedObject, Throwable reason) {
        Map<String, Object> relationInfo = new TreeMap<>();
        if (blockedObject instanceof Identifiable identifiable)
            relationInfo.put("id", identifiable.getId().toString());
        relationInfo.put("object", blockedObject.toString());
        relationInfo.put("reason", reason.toString());
        relationInfo.put("timestamp", TimeTools.currentTimestamp());
        return relationInfo;
    }
}
