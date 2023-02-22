package main.java.com.singularity.ee.service.pluginInstaller.json;

import java.util.List;

public class PluginDetails {
    public long id;
    public String name, description, release_notes_url, download_url, md5_checksum;
    public List<String> class_supports;
    public double version = 0.0, agent_min_version = 99.0;
    public int java_min_version = 99;

    public boolean isCompatible( int javaVersion, double agentVersion ) {
        return (javaVersion >= java_min_version && agentVersion >= agent_min_version);
    }
}
