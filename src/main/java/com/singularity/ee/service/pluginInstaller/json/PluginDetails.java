package com.singularity.ee.service.pluginInstaller.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class PluginDetails {
    public long id;
    public String name, description, release_notes_url, download_url, md5_checksum, filename;
    public List<String> class_supports;
    public double version = 0.0, agent_min_version = 99.0;
    public int java_min_version = 99;
    public File file = null;

    public boolean isChecksumMatching( File testFile ) throws IOException, NoSuchAlgorithmException {
        byte[] b = Files.readAllBytes(testFile.toPath());
        byte[] hash = MessageDigest.getInstance("MD5").digest(b);
        return Arrays.toString(hash).equals(this.md5_checksum);
    }
}
