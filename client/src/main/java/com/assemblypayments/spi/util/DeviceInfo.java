package com.assemblypayments.spi.util;

import java.util.HashMap;
import java.util.Map;

public final class DeviceInfo {

	private DeviceInfo() {
	}

	public static Map<String, String> getAppDeviceInfo() {
		Map<String, String> deviceInfo = new HashMap<String, String>();
		deviceInfo.put("device_system: ", System.getProperty("os.name") + " " + System.getProperty("os.version"));
		return deviceInfo;
	}

}
