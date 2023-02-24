# AppDynamics agent plugin download dynamic service extension


## Theory of Operation

This dynamic service runs on the agent and uses a configuration file as the control plane located on a remote web server. The default configuration is on github, here.
It will ignore files already in the sdk-plugins directory, unless they conflict, and if they do then this service will overwrite the existing.

### Agent Configuration

The plugin installer dynamic service needs to be installed and then node properties from the controller ui will dictate how it acts
setting the following will cause it to perform the installations:

    "agent.isdk.installer.enabled" - boolean, setting this to true causes this service to come alive
    "agent.isdk.installer.url" - github repo config file with everything needed to allow download and install, default is https://github.com/jbsouthe/AppDynamics-Agent-PluginInstaller-Dynamics-Service/tree/main/dist

![](doc-images/AgentUpdaterNodeProperties.png)

### Configuration File

The agent.isdk.installer.url node property points to a base url which then looks for "config.json" in that location. The format for this is:

    {
        "plugins": [
            {
            "id": 1,
            "name": "SSL Certificate Monitor",
            "description": "Monitor SSL Certificates loaded by the JVM for expiration",
            "release_notes_url": "https://github.com/jbsouthe/AppDynamics-SSL-Certificate-Agent_Plugin/blob/main/README.md",
            "download_url": "SSLCertAgentPlugin-1.0.jar",
            "filename": "SSLCertAgentPlugin-1.0.jar",
            "md5_checksum": "210d4f0212716e3cec527b1317fd07a9",
            "version": 1.0,
            "class_supports": ["java.security.cert.X509Certificate"],
            "agent_min_version": 22.1,
            "java_min_version": 8
            },
            { .. }
        ]
    }

* id - must be unique in this file, we index these plugins by this
* name - Short descriptive name of Plugin
* description - Longer description of Plugin
* release_notes_url - Really long description of Plugin ;)
* download_url - this is the jar file of the plugin relative to the base url of the config file
* filename - the filename to write to the sdk-plugins directory, it may be different from the download_url name, or it may be the same, i wasn't trying to spare config options here
* md5_checksum - 

## Configuration

Some setup. This should be installed in the <agent install dir>/ver22.###/external-services/plugin-installer directory
the <agent intall dir>/ver22.###/conf/app-agent-config.xml has to have signing disabled:

    <configuration-properties>
        <property name="external-service-directory" value="external-services"/>
        <property name="enable-jar-signing" value="false"/>
    </configuration-properties>


Agents running this dynamic service will be able to download iSDK plugins dynamically as needed to support technology they support from a github repo that is configurable as a node property


