package com.singularity.ee.service.pluginInstaller.json;

import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.service.pluginInstaller.MD5Checksum;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PluginDetails {
    public long id;
    public String name, description, release_notes_url, download_url, md5_checksum, filename;
    public List<String> class_supports;
    public double version = 0.0, agent_min_version = 99.0;
    public int java_min_version = 99;
    public transient File file = null;

    public String toString() {
        return String.format("PluginDetails name: '%s' file: '%s' version: '%'", name, filename, version);
    }


}
