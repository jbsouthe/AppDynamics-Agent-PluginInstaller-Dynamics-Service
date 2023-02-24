package main.java.com.singularity.ee.service.pluginInstaller;

import com.singularity.ee.agent.appagent.kernel.spi.data.IServiceConfig;
import com.singularity.ee.agent.util.log4j.ADLoggerFactory;
import com.singularity.ee.agent.util.log4j.IADLogger;
import com.singularity.ee.util.string.StringOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class AgentNodeProperties extends Observable {
    private static final IADLogger logger = ADLoggerFactory.getLogger((String)"com.singularity.ee.service.pluginInstaller.AgentNodeProperties");
    public static final String[] NODE_PROPERTIES = new String[]{
            "agent.isdk.installer.enabled",
            "agent.isdk.installer.url"};
    private final Map<String, String> properties = new HashMap<>();

    public void initializeConfigs(IServiceConfig serviceConfig) {
        Map configProperties = serviceConfig.getConfigProperties();
        if( configProperties != null ) {
            boolean enabled = StringOperations.safeParseBoolean((String)((String)configProperties.get("agent.isdk.installer.enabled")), (boolean)true);
            this.properties.put("agent.isdk.installer.enabled", Boolean.toString(enabled));
            String defaultURLString = (String)configProperties.get("agent.isdk.installer.url");
            if (defaultURLString == null ) defaultURLString = "https://github.com/jbsouthe/AppDynamics-Agent-PluginInstaller-Dynamics-Service/tree/main/dist";
            this.properties.put("agent.isdk.installer.url", defaultURLString);
            logger.info("Initializing the properties " + this);
        } else {
            logger.error("Config properties map is null?!?!");
        }
    }

    public String getProperty( String name ) {
        return this.properties.get(name);
    }

    public void updateProperty( String name, String value ) {
        String existingPropertyValue = this.properties.get(name);
        if( !StringOperations.isEmpty((String)value) && !value.equals(existingPropertyValue)) {
            this.properties.put(name, value);
            logger.info("updated property = " + name + " with value = " + value);
            this.notifyMonitoringService(name);
        } else {
            logger.info("did not update property = " + name + " because it was either unchanged or empty");
        }
    }

    protected void notifyMonitoringService(String name) {
        this.setChanged();
        this.notifyObservers(name);
    }

    public String toString() {
        return "AgentNodeProperties{properties=" + this.properties + '}';
    }

    public boolean isEnabled() {
        return StringOperations.safeParseBoolean((String)this.getProperty("agent.isdk.installer.enabled"), (boolean)true);
    }


    public String getInstallerURL() {
        return this.properties.get("agent.isdk.installer.url");
    }
}
