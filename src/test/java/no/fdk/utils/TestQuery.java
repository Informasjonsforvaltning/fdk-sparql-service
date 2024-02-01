package no.fdk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestQuery {

    public static String sendQuery(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            URL url = new URL("http://localhost:8080/fuseki/fdk/query?query=" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                StringBuilder textBuilder = new StringBuilder();
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                return textBuilder.toString();
            } else {
                log.error("server response was " + conn.getResponseCode());
                return null;
            }
        } catch (Exception exception) {
            log.error("query failed", exception);
            return null;
        }
    }

}
