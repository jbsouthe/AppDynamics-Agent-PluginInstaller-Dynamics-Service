# AppDynamics Agent Plugin Download Dynamic Service Extension

This extension allows the agent to make decisions about what iSDK plugins it can use and download them automatically, it will also upgrade those extensions as new ones are available, so long as it can determine the old plugin matches one it knows about. MD5 Checksums are all over the place in this, so if you install many plugins and this config doesn't knnow of them, we will only ignore them.

## Theory of Operation

This dynamic service runs on the agent and uses a configuration file as the control plane located on a remote web server. The default configuration is on github, here.
It will ignore files already in the sdk-plugins directory, unless they match MD5 checksum of a known plugin and then they may be upgraded.

## Installation - You only have to do this once

Some setup. This should be installed in the < agent install dir >/ver22.###/external-services/agent-updater directory
the < agent intall dir >/ver22.###/conf/app-agent-config.xml at line 120 has to have signing disabled in the "Dynamic Services" section:

    <configuration-properties>
        <property name="external-service-directory" value="external-services"/>
        <property name="enable-jar-signing" value="false"/>
    </configuration-properties>

![Agent Config File Example](doc-images/none.png)

Agents now will be downloaded from our download site, but if needed an alternative URL can be set which will instead attempt to download the file name from the root of the url

### Agent Configuration

The plugin installer dynamic service needs to be installed and then node properties from the controller ui will dictate how it acts
setting the following will cause it to perform the installations:

    "agent.isdk.installer.enabled" - boolean, setting this to true causes this service to come alive
    "agent.isdk.installer.url" - github repo config file with everything needed to allow download and install, default is https://github.com/jbsouthe/AppDynamics-Agent-PluginInstaller-Dynamics-Service/raw/main/dist

![](doc-images/AgentUpdaterNodeProperties.png)

### Configuration File

The <B>agent.isdk.installer.url</B> node property points to a base url which then looks for "config.json" in that location. When adding a newer plugin version, leave the old one in place and the service will remove it before installing the upgrade. It will not upgrade to the next higher version, but instead apply the latest version that matches agent and jvm version support.

The format for this is:

    {
        "plugins": [
            {
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

* name - Short descriptive name of Plugin
* description - Longer description of Plugin
* release_notes_url - Really long description of Plugin ;)
* download_url - this is the jar file of the plugin relative to the base url of the config file
* filename - the filename to write to the sdk-plugins directory, it may be different from the download_url name, or it may be the same, i wasn't trying to spare config options here
* md5_checksum - This is the unique checksum of the file, we index by this and it is our only guaranteed id for installed plugins
* version - version of the plugin, multiple versions can exist in this file
* class_supports - A list of classes that indicate this plugin is useful, one day we will use rules, but not today
* agent_min_version - minimum agent version for this plugin
* java_min_version - minimum java vm version for this plugin

## Configuration

Some setup. This should be installed in the <agent install dir>/ver22.###/external-services/plugin-installer directory
the <agent intall dir>/ver22.###/conf/app-agent-config.xml has to have signing disabled:

    <configuration-properties>
        <property name="external-service-directory" value="external-services"/>
        <property name="enable-jar-signing" value="false"/>
    </configuration-properties>


Agents running this dynamic service will be able to download iSDK plugins dynamically as needed to support technology they support from a github repo that is configurable as a node property


