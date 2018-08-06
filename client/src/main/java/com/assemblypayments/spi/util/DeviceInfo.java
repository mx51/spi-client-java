package com.assemblypayments.spi.util;

import java.util.HashMap;
import java.util.Map;

public final class DeviceInfo {

    private DeviceInfo() {
    }

    public static Map<String, String> getAppDeviceInfo () {
    	Map<String, String> deviceInfo = new HashMap<String, String>();
        String name = "os.name";
        String version = "os.version";
        
        deviceInfo.put("device_system: ", System.getProperty(name) + " " + System.getProperty(version));
        
        return deviceInfo;
    }

}
