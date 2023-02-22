package main.java.com.singularity.ee.service.pluginInstaller.exception;

import main.java.com.singularity.ee.service.pluginInstaller.json.PluginDetails;

public class MD5ChecksumException extends Exception {
    private final PluginDetails plugin;

    public MD5ChecksumException(String message, PluginDetails pluginDetails ) {
        super(message);
        this.plugin = pluginDetails;
    }

    public PluginDetails getPlugin() { return plugin; }
}
