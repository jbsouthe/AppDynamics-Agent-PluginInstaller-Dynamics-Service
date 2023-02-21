package main.java.com.singularity.ee.service.pluginInstaller;

import com.singularity.ee.agent.appagent.kernel.ServiceComponent;
import com.singularity.ee.agent.appagent.kernel.spi.IDynamicService;
import com.singularity.ee.agent.appagent.kernel.spi.IServiceContext;
import com.singularity.ee.agent.appagent.services.bciengine.JavaAgentManifest;
import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.util.javaspecific.threads.IAgentRunnable;

import java.util.HashMap;
import java.util.Map;

public class CheckForAgentInstallPluginsTask implements IAgentRunnable {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.CheckForAgentInstallPluginsTask");

    private IDynamicService agentService;
    private AgentNodeProperties agentNodeProperties;
    private ServiceComponent serviceComponent;
    private IServiceContext serviceContext;

    public CheckForAgentInstallPluginsTask(IDynamicService agentService, AgentNodeProperties agentNodeProperties, ServiceComponent serviceComponent, IServiceContext iServiceContext) {
        this.agentNodeProperties=agentNodeProperties;
        this.agentService=agentService;
        this.serviceComponent=serviceComponent;
        this.serviceContext=iServiceContext;
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
        String configFileURLString = agentNodeProperties.getInstallerURL();
        //TODO implement stuff
        //1. download configuration from a hopefully valid remote config yaml file
        //2. check to see if the plugins of that version are already installed or not, skip the ones already installed
        //3. for each that isn't installed check to see if the classes it triggers on are found on the app classloader
        //4. confirm prereqs: jvm version, agent version, app class version
        //5. if matching, download agent plugin jar, confirm md5 checksum, install
    }


    private void sendInfoEvent(String message) {
        sendInfoEvent(message, new HashMap());
    }

    private void sendInfoEvent(String message, Map map) {
        logger.info("Sending Custom INFO Event with message: "+ message);
        serviceComponent.getEventHandler().publishInfoEvent(message, map);
    }


    private String getCurrentAgentVersionFromFile() {
        JavaAgentManifest javaAgentManifest = JavaAgentManifest.parseManifest(serviceContext.getInstallDir());
        return javaAgentManifest.getJavaAgentVersion();
    }
    
}
