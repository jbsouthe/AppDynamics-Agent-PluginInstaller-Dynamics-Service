package com.singularity.ee.service.pluginInstaller.web;

import com.singularity.ee.service.pluginInstaller.MD5Checksum;
import com.singularity.ee.service.pluginInstaller.json.PluginDetails;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class DownloaderTest {

    @Test
    public void getPluginFile() throws Exception {
        Downloader downloader = new Downloader();
        downloader.setBaseURL("https://github.com/jbsouthe/AppDynamics-Agent-PluginInstaller-Dynamics-Service/raw/main/dist");
        assert true; //didn't throw an exception for the url
        List<PluginDetails> pluginDetailsList = downloader.getPluginListing();
        System.out.println("Downloaded list of plugins, size= "+ pluginDetailsList.size());
        PluginDetails pluginDetails = pluginDetailsList.get(0);
        File downloadedFile = downloader.getPluginFile( pluginDetails );
        System.out.println(String.format("File downloaded: %s size %s", downloadedFile.getAbsolutePath(), downloadedFile.length()));
        assert MD5Checksum.equals(downloadedFile, pluginDetails.md5_checksum);
    }
}