package pt.up.fe.comp;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Map<String, String> configs = new HashMap<>();

    public static void setConfigs(Map<String, String> config){
        if(config.isEmpty()){
            config = new HashMap<>();
        }
        config.put("optimize", config.getOrDefault("optimize", "false"));
        config.put("registerAllocation", config.getOrDefault("registerAllocation", "-1"));
        config.put("debug", "false");

        Config.configs = config;

    }

    public static Map<String, String> getConfigs(){
        return Config.configs;
    }

}
