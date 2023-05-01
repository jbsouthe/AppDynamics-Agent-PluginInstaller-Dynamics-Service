package com.singularity.ee.service.pluginInstaller;

import com.singularity.ee.agent.appagent.kernel.ServiceComponent;
import com.singularity.ee.agent.appagent.kernel.spi.IDynamicService;
import com.singularity.ee.agent.appagent.kernel.spi.IServiceContext;
import com.singularity.ee.agent.appagent.services.bciengine.JavaAgentManifest;
import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.util.io.FileUtil;
import com.singularity.ee.util.javaspecific.threads.IAgentRunnable;
import com.singularity.ee.service.pluginInstaller.exception.MD5ChecksumException;
import com.singularity.ee.service.pluginInstaller.json.PluginDetails;
import com.singularity.ee.service.pluginInstaller.web.Downloader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CheckForAgentInstallPluginsTask implements IAgentRunnable {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.CheckForAgentInstallPluginsTask");

    private IDynamicService agentService;
    private AgentNodeProperties agentNodeProperties;
    private ServiceComponent serviceComponent;
    private IServiceContext serviceContext;
    private Downloader downloader;

    public CheckForAgentInstallPluginsTask(IDynamicService agentService, AgentNodeProperties agentNodeProperties, ServiceComponent serviceComponent, IServiceContext iServiceContext) {
        this.agentNodeProperties=agentNodeProperties;
        this.agentService=agentService;
        this.serviceComponent=serviceComponent;
        this.serviceContext=iServiceContext;
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

        Map<String,PluginDetails> serverPluginMap = this.downloader.getPluginListing();
        int count = serverPluginMap.size();
        //1. update list of installed plugins from the jar files in the isdk plugins directory of the agent
        Map<String,PluginDetails> installedPlugins = null;
        try {
            installedPlugins = inventoryPlugins( new File(serviceContext.getInstallDir()  + "/sdk-plugins/"), serverPluginMap);
        } catch (Exception e) {
            logger.error("Exception thrown while attempting to process installed plugins, can not continue! Exception: "+ e.getMessage(),e);
            return;
        }

        //2. check to see if the plugins of that version are already installed or not, skip the ones already installed, unless newer -- did this in the last method
        logger.debug(String.format("Pruned %d plugins already installed from the server list, now examining %d for fitness", count-serverPluginMap.size(), serverPluginMap.size()));
        for( PluginDetails pluginDetails : serverPluginMap.values() ) {
            try {
                //3. for each that isn't installed check to see if the classes it triggers on are found on the app classloader
                if (!isClassLoaded(pluginDetails.class_supports)) {
                    logger.debug(String.format("Skipping %s because classes it supports are not found in the application: %s", pluginDetails, pluginDetails.class_supports));
                    continue;
                }
                //4. confirm prereqs: jvm version, agent version,
                if (!isCompatible(pluginDetails)) {
                    logger.debug(String.format("Skipping %s because it is not listed as compatible with the jvm (%f) or the agent(%f)", pluginDetails, pluginDetails.java_min_version, pluginDetails.agent_min_version));
                    continue;
                }
                // check to see if the plugin we want to install already has an older version installed, or a filename that matches another plugin
                if (!isBestVersion(pluginDetails, serverPluginMap)) {
                    logger.debug(String.format("Skipping %s because it is old and a newer version exists in the list from the server", pluginDetails));
                    //so tempted to remove this, but i'm in the middle of iterating the list
                    continue;
                }
                //5. if matching, download agent plugin jar, confirm md5 checksum
                File downloadedPluginFile = this.downloader.getPluginFile(pluginDetails);
                //6. if file exists, delete it either the version is different or we aren't sure so replace it
                File targetFile = new File(serviceContext.getInstallDir()  + "/sdk-plugins/" + pluginDetails.filename);

                // check to see if this is an upgrade to an already installed plugin, and remove that version before continuing
                List<PluginDetails> oldPlugins = findOldVersionAlreadyInstalled(pluginDetails, installedPlugins.values());
                for( PluginDetails oldVersionOfPlugin : oldPlugins ) {
                    oldVersionOfPlugin.file.delete();
                    installedPlugins.remove(oldVersionOfPlugin.md5_checksum);
                }
                //7. copy the downloaded file to the sdk-plugins directory
                copyFile( downloadedPluginFile, targetFile );
                pluginDetails.file = targetFile;
                //8. add the plugin to the list of installed plugins
                installedPlugins.put(pluginDetails.md5_checksum, pluginDetails);
            } catch (MD5ChecksumException e) {
                logger.error(String.format("Error in %s MD5 checksum for new file: %s", e.getPlugin().name,e.toString()));
            } catch (IOException e) {
                logger.error(String.format("IOError in plugin install, Exception: %s",e.getMessage()));
            } catch (NoSuchAlgorithmException e) {
                logger.error(String.format("Error trying to generate MD5 checksum, no such algorithm exception: %s",e.getMessage() ));
            }
        }
    }

    private List<PluginDetails> findOldVersionAlreadyInstalled(PluginDetails pluginDetails, Collection<PluginDetails> installedPlugins) {
        List<PluginDetails> listOfOldVersionPlugins = new ArrayList<>();
        for( PluginDetails installedPlugin : installedPlugins )
            if( installedPlugin.name.equals(pluginDetails.name) && installedPlugin.version < pluginDetails.version )
                listOfOldVersionPlugins.add(installedPlugin);
        return listOfOldVersionPlugins;
    }

    private boolean isBestVersion(PluginDetails pluginDetails, Map<String, PluginDetails> serverPluginMap) {
        for( PluginDetails otherPlugin : serverPluginMap.values() ) {
            if( otherPlugin.name.equals( pluginDetails.name ) && otherPlugin.version > pluginDetails.version ) {
                return false; //tada
            }
        }
        return true; //we made it through the list, and no plugin with the same name had a higher version
    }

    private Map<String, PluginDetails> inventoryPlugins(File pluginDir, Map<String, PluginDetails> serverPluginMap) throws IOException, NoSuchAlgorithmException {
        String[] fileNameList = pluginDir.list( //it feels like this should be easier, i bet a stream does this gooder
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".jar");
                    }
                }
        );
        Map<String, PluginDetails> installedPluginsList = new HashMap<>();
        List<PluginDetails> prunePluginList = new ArrayList<>();
        for( String fileName : fileNameList ) {
            File existingPluginFile = new File(pluginDir, fileName);
            String md5_checksum = MD5Checksum.generate(existingPluginFile);
            //the rest of this is going to look confusing, until you take it all in, trust me this changes O(n^2) to O(n); though you haven't seen the bad version
            PluginDetails matchingServerPlugin = serverPluginMap.remove(md5_checksum); //i don't want to process server side that is already installed
            if( matchingServerPlugin != null ) { //if i removed one, then i got a match and i better add it to the installed list
                matchingServerPlugin.setFile(existingPluginFile); //go ahead and point to the plugin file for later
                installedPluginsList.put(md5_checksum, matchingServerPlugin); //add this plugin to the list of installed plugins
            } else { //if the matching plugin is null, it wasn't in the list on the server, so ...
                installedPluginsList.put(md5_checksum, new PluginDetails(existingPluginFile, md5_checksum) ); //add it to the installed list with not much info yet
            }
        }
        return installedPluginsList;
    }




    private void sendInfoEvent(String message) {
        sendInfoEvent(message, MetaData.getAsMap());
    }

    private void sendInfoEvent(String message, Map map) {
        logger.info("Sending Custom INFO Event with message: "+ message);
        if( !map.containsKey("agent-isdk-installer-version")) map.putAll(MetaData.getAsMap());
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
