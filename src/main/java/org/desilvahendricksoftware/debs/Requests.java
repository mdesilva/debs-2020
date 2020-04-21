package org.desilvahendricksoftware.debs;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Requests implements Serializable{

    final String BENCHMARK_SYSTEM_URL = "BENCHMARK_SYSTEM_URL";
    final int RECORD_START_INDEX = 13;
    final int QUERY_1 = 1;
    final int QUERY_2 = 2;

    Map JSON_WRITER_ARGS = new HashMap();
    String endpoint;
    String host;

    public Requests(int query) throws InvalidQueryException {
        if (query == QUERY_1 || query == QUERY_2) {
            this.endpoint = "/data/" + query + "/";
        } else {
            throw new InvalidQueryException();
        }
        this.host = System.getenv(BENCHMARK_SYSTEM_URL) != null ? "http://" + System.getenv(BENCHMARK_SYSTEM_URL) : "http://localhost";
        System.out.println("Using host: " + this.host);
        JSON_WRITER_ARGS.put(JsonWriter.TYPE, false);

    }

    public Sample[] serializeRecordsToSamples(String records) {
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

    public Sample[] get() {
        // System.out.println("Making batch request");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet(this.host + this.endpoint);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(getRequest);
            String responseContent = EntityUtils.toString(response.getEntity());
            //Empty record set is returned as {'records': ''} with status code 200
            if (!responseContent.contains("voltage") || !responseContent.contains("current")) {
//                System.out.println("Reached end of records");
                return null;
            }
            return serializeRecordsToSamples(responseContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void post(Result result) throws PostRequestFailure {
//       System.out.println("Posting " + result.toString());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(this.host + this.endpoint);
        postRequest.setEntity(new StringEntity(JsonWriter.objectToJson(result, JSON_WRITER_ARGS), ContentType.APPLICATION_JSON));
        try {
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new PostRequestFailure(response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class InvalidQueryException extends Exception {
        public InvalidQueryException() {
            super("Invalid query provided. Valid queries include Query 1. Query 2 is not ready yet");
        }
    }

    public class PostRequestFailure extends Exception {
        public PostRequestFailure(int statusCode) {
            super("Post request returned status code " + statusCode);
        }
    }

    public static void main(String[] args){};
}
