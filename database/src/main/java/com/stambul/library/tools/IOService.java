package com.stambul.library.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IOService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;

    @Autowired
    public IOService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Object parseJsonFromByteArrayOutputStream(ByteArrayOutputStream stream) {
        return parseJsonFromInputStream(new ByteArrayInputStream(stream.toByteArray()));
    }

    public Object parseJsonFromInputStream(InputStream stream) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(stream);
            return parseJSON(reader);
        } finally {
            closeReader(reader);
        }
    }

    public Object parseJsonFromResourceFile(String filePathFromResourcesFolder) {
        BufferedReader reader = null;
        try {
            reader = getBufferedReaderFromResourceFile(filePathFromResourcesFolder);
            return parseJSON(reader);
        } finally {
            closeReader(reader);
        }
    }

    public boolean fileExist(String filePath) {
        return (new File(filePath)).exists();
    }

    public void createFileIfNotExist(String filePath) {
        try {
            (new File(filePath)).createNewFile();
        } catch (Exception e) {
            String message = String.format("Can not create file=[%s]", filePath);
            throw new RuntimeException(message);
        }
    }

    public Object parseJsonFromFile(String filePath) {
        BufferedReader reader = null;
        try {
            reader = getBufferedReaderFromFile(filePath);
            return parseJSON(reader);
        } finally {
            closeReader(reader);
        }
    }

    public Object extractDataFromJson(Map<String, Object> jsonObject, String jsonPath) {
        Map<String, Object> json = jsonHardCopy(jsonObject);
        String[] tokens = jsonPath.split("\\.");
        int last = tokens.length - 1;
        for (int i = 0; i < last; i++) {
            if (json == null)
                return null;
            json = (Map<String, Object>) json.get(tokens[i]);
        }

        if (json == null)
            return null;
        return json.get(tokens[last]);
    }

    public Object parseJsonFromString(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Can not parse JSON: [%s]", jsonContent), e);
        }
    }

    public void writeToFile(String content, String filePath) {
        Writer writer = null;
        try {
            createFileIfNotExist(filePath);
            writer = getBufferedWriterFromFile(filePath);
            writer.write(content);
        } catch (Exception e) {
            String message = String.format("Can not write content=[%s] to file=[%s]", content, filePath);
            throw new RuntimeException(message, e);
        }
        finally {
            closeWriter(writer);
        }
    }

    public void writeToResourceFile(String content, String filePathFromResourcesFolder) {
        BufferedWriter writer = null;
        try {
            writer = getBufferedWriterFromResourceFile(filePathFromResourcesFolder);
            writer.write(content);
        } catch (IOException e) {
            String message = String.format("Can not write content=[%s] to file=[%s]", content, filePathFromResourcesFolder);
            throw new RuntimeException(message, e);
        }
        finally {
            closeWriter(writer);
        }
    }

    public String toJSONString(List<Object> json) {
        try {
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Can not cast map to JSON string: [%s]", json), e);
        }
    }

    public String toJSONString(Map<String, Object> json) {
        try {
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Can not cast map to JSON string: [%s]", json), e);
        }
    }

    public Map<String, Object> jsonHardCopy(Map<String, Object> json) {
        try {
            return (Map<String, Object>) parseJsonFromString(toJSONString(json));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Can not cast map to JSON string: [%s]", json), e);
        }
    }

    public Document parseXmlFromString(String xmlContent) {
        return Jsoup.parse(xmlContent);
    }

    public ResponseEntity<Resource> request(
            String url, HttpMethod method, Map<String, Object> httpProperties
    ) {
        return request(url, method, null, httpProperties);
    }

    public ResponseEntity<Resource> request(
            String url, HttpMethod method, String body, Map<String, Object> httpProperties
    ) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String property : httpProperties.keySet())
            httpHeaders.set(property, (String) httpProperties.get(property));
        return restTemplate.exchange(url, method, new HttpEntity<>(body, httpHeaders), Resource.class);
    }

    public ByteArrayOutputStream encodeGzip(ResponseEntity<Resource> responseEntity) {
        String requiredContentEncoding = "gzip";
        String contentEncoding = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (!requiredContentEncoding.equalsIgnoreCase(contentEncoding)) {
            String message = "Content encoding mismatch: required=" + requiredContentEncoding;
            message += " received=" + contentEncoding;
            throw new RuntimeException(message);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(responseEntity.getBody().getInputStream())) {
            gzipInputStream.transferTo(byteArrayOutputStream);
        } catch (Exception e) {
            throw new RuntimeException("Can not parse gzip", e);
        }
        return byteArrayOutputStream;
    }

    public Object parseInfoAsJSON(String url) {
        return parseInfoAsJSON(url, HttpMethod.GET, null, new TreeMap<>());
    }

    public Object parseInfoAsJSON(String url, String body) {
        return parseInfoAsJSON(url, HttpMethod.POST, body, new TreeMap<>());
    }

    public Object parseInfoAsJSON(
            String url,
            HttpMethod method,
            String body,
            Map<String, Object> httpProperties
    ) {
        ResponseEntity<Resource> response = request(url, method, body, httpProperties);

        HttpStatusCode code = response.getStatusCode();
        if (!code.is2xxSuccessful())
            throw new RuntimeException("Connection error: " + code);
        if (response.getBody() == null) {
            String message = "Null response body: response=%s, body=%s";
            throw new RuntimeException(String.format(message, response, response.getBody()));
        }

        InputStream inputStream;
        try {
            inputStream = response.getBody().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Can not get response body input stream", e);
        }

        return parseJsonFromInputStream(inputStream);
    }


    private BufferedReader getBufferedReaderFromResourceFile(String filePathFromResourcesFolder) {
        try {
            Resource res = resourceLoader.getResource("classpath:" + filePathFromResourcesFolder);
            return new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not open file=[" + filePathFromResourcesFolder + "]", e);
        }
    }

    private BufferedReader getBufferedReaderFromFile(String filePath) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not open file=[" + filePath + "]", e);
        }
    }

    private BufferedWriter getBufferedWriterFromResourceFile(String filePathFromResourcesFolder) {
        try {
            Resource res = resourceLoader.getResource("classpath:" + filePathFromResourcesFolder);
            FileOutputStream fileOutputStream = new FileOutputStream(res.getFile());
            return new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not open file=[" + filePathFromResourcesFolder + "]", e);
        }
    }

    private BufferedWriter getBufferedWriterFromFile(String filePath) {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not open file=[" + filePath + "]", e);
        }
    }

    private void closeReader(Reader reader) {
        try {
            if (reader != null)
                reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Can not close reader", e);
        }
    }

    private void closeWriter(Writer writer) {
        try {
            if (writer != null)
                writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Can not close writer", e);
        }
    }

    private Object parseJSON(Reader reader) {
        try {
            return objectMapper.readValue(reader, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not parse JSON", e);
        }
    }
}
