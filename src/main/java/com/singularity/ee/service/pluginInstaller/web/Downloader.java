package com.singularity.ee.service.pluginInstaller.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.service.pluginInstaller.exception.MD5ChecksumException;
import com.singularity.ee.service.pluginInstaller.json.ConfigJSON;
import com.singularity.ee.service.pluginInstaller.json.PluginDetails;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Downloader {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.web.Downloader");
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private URL baseURL;

    public Downloader() {
    }

    public void setBaseURL(String urlString) throws MalformedURLException {
        if( !urlString.endsWith("/") ) urlString += "/";
        this.baseURL = new URL(urlString);
    }

    public List<PluginDetails> getPluginListing() {
        logger.info(String.format("Fetching list of plugins available for download"));
        try {
            URL url = new URL(this.baseURL, "config.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) { response.append(inputLine); response.append("\n"); }
            in.close();
            if( connection.getResponseCode() != 200 )
                logger.warn("Response: "+ connection.getResponseMessage());
            logger.debug(String.format("Response from download list request: '%s'", response.toString()));
            return gson.fromJson( response.toString(), ConfigJSON.class ).plugins;
        } catch (IOException ioException) {
            logger.error(String.format("IO Exception: %s",ioException),ioException);
        }

        return null;
    }

    public File getPluginFile(PluginDetails pluginDetails) throws IOException, MD5ChecksumException, NoSuchAlgorithmException {
        FileOutputStream outputStream = null;
        File tempFile = null;
        try {
            URL url = new URL( this.baseURL, pluginDetails.download_url );
            tempFile = File.createTempFile("temp-agent-plugin-download", ".jar");
            outputStream = new FileOutputStream(tempFile);
            logger.info("downloading "+ url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = connection.getHeaderField("Location");
                    String cookies = connection.getHeaderField("Set-Cookie");
                    connection = (HttpURLConnection) new URL(newUrl).openConnection();
                    connection.setRequestProperty("Cookie", cookies);
                }
            }
            outputStream.getChannel().transferFrom(Channels.newChannel(connection.getInputStream()), 0, Long.MAX_VALUE);
            logger.info("Temp file downloaded to: "+ tempFile.getAbsolutePath());
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException ignored) {}
        }

        if( !pluginDetails.isChecksumMatching(tempFile) )
            throw new MD5ChecksumException("MD5 Checksum does not match expected, we can not install", pluginDetails);

        return tempFile;
    }
}
