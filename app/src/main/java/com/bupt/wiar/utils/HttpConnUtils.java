package com.bupt.wiar.utils;

/**
 * Created by xue on 2015/12/6.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpConnUtils {

    private static HttpURLConnection urlConnection;

    public static String doGet(String targetName){
        String path = "http://10.103.26.108:8080/cshttp/arServlet?targetName="+targetName;
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(path);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

        return result.toString();
    }

}
