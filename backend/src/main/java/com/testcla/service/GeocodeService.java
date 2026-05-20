package com.testcla.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeocodeService {

    private static final String KEY = "d6eaf3a2b5ac64cb71684ffc70690a09";
    private static final String API = "https://restapi.amap.com/v3/geocode/regeo";

    public String reverseGeocode(double lat, double lng) {
        try {
            String url = API + "?key=" + KEY + "&location=" + lng + "," + lat + "&output=JSON";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            // Simple JSON parse to extract formatted_address
            String json = sb.toString();
            int idx = json.indexOf("\"formatted_address\"");
            if (idx < 0) return "";
            int start = json.indexOf("\"", idx + 20) + 1;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}
