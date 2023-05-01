package com.singularity.ee.service.pluginInstaller;

import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.MD5Checksum");


    public static String generate( File file ) throws IOException, NoSuchAlgorithmException {
        byte[] b = Files.readAllBytes(file.toPath());
        byte[] hash = MessageDigest.getInstance("MD5").digest(b);
        return new BigInteger(1, hash).toString(16).toString();
    }

    public static boolean equals( File file, String otherMD5Checksum)  {
        try {
            return MD5Checksum.generate(file).equals(otherMD5Checksum);
        } catch (IOException e) {
            logger.warn(String.format("IOException generating MD5 Checksum on file %s, exception: %s", file.getAbsolutePath(), e.getMessage()));
        } catch (NoSuchAlgorithmException e) {
            logger.warn(String.format("NoSuchAlgorithmException generating MD5 Checksum on file %s, exception: %s", file.getAbsolutePath(), e.getMessage()));
        }
        return false;
    }
}
