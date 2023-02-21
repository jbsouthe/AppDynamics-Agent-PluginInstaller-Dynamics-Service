# AppDynamics agent plugin download dynamic service extension


Some setup. This should be installed in the <agent install dir>/ver22.###/external-services/plugin-installer directory
the <agent intall dir>/ver22.###/conf/app-agent-config.xml has to have signing disabled:

    <configuration-properties>
        <property name="external-service-directory" value="external-services"/>
        <property name="enable-jar-signing" value="false"/>
    </configuration-properties>


Agents running this dynamic service will be able to download iSDK plugins dynamically as needed to support technology they support from a github repo that is configurable as a node property


how does it work:
The plugin installer dynamic service needs to be installed and then node properties from the controller ui will dictate how it acts
setting the following will cause it to perform the installs:

    "agent.isdk.installer.enabled" - boolean, setting this to true causes this service to come alive
    "agent.isdk.installer.url" - github repo config file with everything needed to allow download and install

![](doc-images/AgentUpdaterNodeProperties.png)
