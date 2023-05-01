package com.singularity.ee.service.pluginInstaller;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class MD5ChecksumTest {

    @Test
    public void testEquals() throws Exception {
        File someFile = new File("src/test/resources/someFile.txt");
        String checkSum = MD5Checksum.generate(someFile);
        System.out.println(String.format("Checksum for %s is '%s'", someFile.getName(), checkSum));
        assert MD5Checksum.equals(someFile, checkSum);
        assert !MD5Checksum.equals(someFile, checkSum.replaceAll("0", "1"));
    }
}