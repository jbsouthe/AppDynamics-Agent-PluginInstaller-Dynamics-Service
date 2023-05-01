package com.singularity.ee.service.pluginInstaller.json;

import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.service.pluginInstaller.MD5Checksum;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class PluginDetails {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.json.PluginDetails");

    public long id;
    public String name, description, release_notes_url, download_url, md5_checksum, filename;
    public List<String> class_supports;
    public double version = 0.0, agent_min_version = 0.0;
    public int java_min_version = 0;
    public transient File file = null;

    public PluginDetails() {}
    public PluginDetails(File existingPluginFile, String md5Checksum) {
        this.file = existingPluginFile;
        this.md5_checksum = md5Checksum;
        this.name = "Unknown";
        this.filename = file.getName();
        class_supports = new ArrayList<>();
    }

    public String toString() {
        return String.format("PluginDetails name: '%s' file: '%s' version: '%'", name, filename, version);
    }


    public void setFile(File existingPluginFile) {
        this.file=existingPluginFile;
        if( !this.file.getName().equals(filename) ) logger.warn("Installed plugin checksum matches "+ this.toString() +" but filename does not: "+ this.file.getName() );
    }
}
