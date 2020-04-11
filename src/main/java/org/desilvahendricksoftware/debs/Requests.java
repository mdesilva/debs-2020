package org.desilvahendricksoftware.debs;
import com.cedarsoftware.util.io.JsonReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

public class Requests {

    static final int RECORD_START_INDEX = 13;
    static final String HOST = "http://localhost/";


    public static Sample[] serializeRecordsToSamples(String records) {
        records = records.substring(RECORD_START_INDEX, records.length() - 1);
        ArrayList<Sample> samples = new ArrayList<Sample>();
        StringBuilder currentSampleBuilder = new StringBuilder();
        int index = 0;
        while (index != records.length() - 1) {
            char currentChar = records.charAt(index);
            if (currentChar != '}') {
                currentSampleBuilder.append(currentChar);
                if (currentChar == '{') {
                    currentSampleBuilder.append("\"@type\": \"org.desilvahendricksoftware.debs.Sample\", ");
                }
                index++;
            } else {
                currentSampleBuilder.append('}');
                samples.add((Sample) JsonReader.jsonToJava(currentSampleBuilder.toString()));
                currentSampleBuilder.delete(0, currentSampleBuilder.length());
                if (records.charAt(index + 1) == ']') {
                    break;
                }
                index += 3;
            }
        }

        return samples.toArray(new Sample[samples.size()]);
    }

    public static Sample[] get(String endpoint) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet(HOST + endpoint);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(getRequest);
            String responseContent = EntityUtils.toString(response.getEntity());
            if (!responseContent.contains("record")) {
                return null;
            }
            return serializeRecordsToSamples(responseContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
