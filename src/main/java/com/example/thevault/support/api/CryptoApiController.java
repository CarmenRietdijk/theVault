/*
Deze controller zorgt ervoor dat er API calls kunnen worden gemaakt naar de website
coinmarketcap.com/ zodat up-to-date currencies kunnen worden opgehaald voor cryptomunt

Creation date 01/12/2021
*/

package com.example.thevault.support.api;


/**
 * This example uses the Apache HTTPComponents library.
 */

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class CryptoApiController {

    private static HttpURLConnection connection;
    private static String apiKey = "b82c2a71-e4ae-4478-9c76-852e76796fa2";

    public static void main(String[] args)  {
        String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        parameters.add(new BasicNameValuePair("start","1"));
        parameters.add(new BasicNameValuePair("limit","20"));
        parameters.add(new BasicNameValuePair("convert","EUR"));

        try {
            String result = makeAPICall(uri, parameters);
            System.out.print(result);
        } catch (IOException e) {
            System.out.println("Error: cannot access content - " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.toString());
        }
    }


    /**
     * Maakt een API call naar een bepaalde meegegeven uri met de benodigde parameters
     * dit in de HTTP request worden meegegeven.
     *
     * @param uri
     * @param parameters
     * @return response content
     */
    public static String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", apiKey);

        CloseableHttpResponse response = client.execute(request);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        return response_content;
    }
}
