package com.singularity.ee.service.pluginInstaller;

import java.util.HashMap;
import java.util.Map;

public class MetaData {
    public static final String VERSION = "v${version}";
    public static final String BUILDTIMESTAMP = "${build.time}";
    public static final String GECOS = "John Southerland josouthe@cisco.com";
    public static final String GITHUB = "https://github.com/jbsouthe/AppDynamics-Agent-PluginInstaller-Dynamics-Service";
    //public static final String DEVNET = "https://developer.cisco.com/codeexchange/github/repo/jbsouthe/AppDynamicsAgentUpdater";
    public static final String SUPPORT = "https://github.com/jbsouthe/AppDynamics-Agent-PluginInstaller-Dynamics-Service/issues";


    public static Map<String,String> getAsMap() {
        Map<String,String> map = new HashMap<>();
        map.put("agent-isdk-installer-version", VERSION);
        map.put("agent-isdk-installer-buildTimestamp", BUILDTIMESTAMP);
        map.put("agent-isdk-installer-developer", GECOS);
        map.put("agent-isdk-installer-github", GITHUB);
        //map.put("agent-isdk-installer-devnet", DEVNET);
        map.put("agent-isdk-installer-support", SUPPORT);
        return map;
    }
}
