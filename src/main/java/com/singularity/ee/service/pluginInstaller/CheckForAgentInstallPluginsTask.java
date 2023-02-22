package main.java.com.singularity.ee.service.pluginInstaller;

import com.singularity.ee.agent.appagent.kernel.ServiceComponent;
import com.singularity.ee.agent.appagent.kernel.spi.IDynamicService;
import com.singularity.ee.agent.appagent.kernel.spi.IServiceContext;
import com.singularity.ee.agent.appagent.services.bciengine.JavaAgentManifest;
import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.util.io.FileUtil;
import com.singularity.ee.util.javaspecific.threads.IAgentRunnable;
import main.java.com.singularity.ee.service.pluginInstaller.exception.MD5ChecksumException;
import main.java.com.singularity.ee.service.pluginInstaller.json.PluginDetails;
import main.java.com.singularity.ee.service.pluginInstaller.web.Downloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckForAgentInstallPluginsTask implements IAgentRunnable {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.CheckForAgentInstallPluginsTask");

    private IDynamicService agentService;
    private AgentNodeProperties agentNodeProperties;
    private ServiceComponent serviceComponent;
    private IServiceContext serviceContext;
    private Map<Long,PluginDetails> installedPlugins;
    private Downloader downloader;

    public CheckForAgentInstallPluginsTask(IDynamicService agentService, AgentNodeProperties agentNodeProperties, ServiceComponent serviceComponent, IServiceContext iServiceContext) {
        this.agentNodeProperties=agentNodeProperties;
        this.agentService=agentService;
        this.serviceComponent=serviceComponent;
        this.serviceContext=iServiceContext;
        this.installedPlugins = new HashMap<>();
        this.downloader = new Downloader();
    }

    private boolean isClassLoaded( List<String> classNames ) {
        for( String className : classNames )
            if( this.serviceComponent.getBCIEngineService().getClassesByName(className) != null ) return true;
        return false;
    }

    public boolean isCompatible( PluginDetails pluginDetails ) {
        return (getJavaVersion() >= pluginDetails.java_min_version && getCurrentAgentVersionFromFile() >= pluginDetails.agent_min_version);
    }

    private void copyFile(File sourceLocation, File targetLocation ) throws IOException {
        logger.debug(String.format("copyFile '%s' -> '%s'",sourceLocation.getAbsolutePath(),targetLocation.getAbsolutePath()));
        FileUtil.copyFile(sourceLocation, targetLocation);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        logger.info("Running the task to check for needed plugins and install if needed");

        try {
            this.downloader.setBaseURL(agentNodeProperties.getInstallerURL());
        } catch (MalformedURLException e) {
            logger.error(String.format("Can not continue with plugin downloads, url is not correct: '%s'", agentNodeProperties.getInstallerURL()));
            return;
        }

        for( PluginDetails pluginDetails : this.downloader.getPluginListing() ) {
            try {
                //2. check to see if the plugins of that version are already installed or not, skip the ones already installed, unless newer
                if (installedPlugins.containsKey(pluginDetails.id)
                        && installedPlugins.get(pluginDetails.id).version == pluginDetails.version) {
                    continue;
                }
                //3. for each that isn't installed check to see if the classes it triggers on are found on the app classloader
                if (!isClassLoaded(pluginDetails.class_supports)) continue;
                //4. confirm prereqs: jvm version, agent version,
                if (!isCompatible(pluginDetails)) continue;
                //5. if matching, download agent plugin jar, confirm md5 checksum
                pluginDetails.file = this.downloader.getPluginFile(pluginDetails);
                //6. if file exists, delete it either the version is different or we aren't sure so replace it
                File targetFile = new File(serviceContext.getInstallDir()  + "/sdk-plugins/" + pluginDetails.filename);
                //7. copy the downloaded file to the sdk-plugins directory
                copyFile( pluginDetails.file, targetFile );
                pluginDetails.file = targetFile;
                //8. add the plugin to the list of installed plugins
                installedPlugins.put(pluginDetails.id, pluginDetails);
            } catch (MD5ChecksumException e) {
                logger.error(String.format("Error in %s MD5 checksum for new file: %s", e.getPlugin().name,e.toString()));
            } catch (IOException e) {
                logger.error(String.format("IOError in plugin install, Exception: %s",e.getMessage()));
            } catch (NoSuchAlgorithmException e) {
                logger.error(String.format("Error trying to generate MD5 checksum, no such algorithm exception: %s",e.getMessage() ));
            }
        }
    }


    private void sendInfoEvent(String message) {
        sendInfoEvent(message, new HashMap());
    }

    private void sendInfoEvent(String message, Map map) {
        logger.info("Sending Custom INFO Event with message: "+ message);
        serviceComponent.getEventHandler().publishInfoEvent(message, map);
    }

private Double _javaAgentVersion = null;
    private double getCurrentAgentVersionFromFile() {
        if( _javaAgentVersion == null ) {
            JavaAgentManifest javaAgentManifest = JavaAgentManifest.parseManifest(serviceContext.getInstallDir());
            String version = javaAgentManifest.getJavaAgentVersion(); //Javaagent-Version: 22.12.0.34603
            int firstDot = version.indexOf(".");
            int secondDot = version.indexOf(".", firstDot);
            if (secondDot != -1) {
                version = version.substring(0, secondDot);
            }
            _javaAgentVersion = Double.parseDouble(version);
        }
        return _javaAgentVersion;
    }

    private Integer _javaVMVersion = null;
    private int getJavaVersion() {
        if( _javaVMVersion == null ) {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf(".");
                if (dot != -1) {
                    version = version.substring(0, dot);
                }
            }
            _javaVMVersion = Integer.parseInt(version);
        }
        return _javaVMVersion;
    }
    
}
