package com.nagma.suitelectronica;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.Map;

public class Arduino {

    public final int ARDUINO_VID = 0x2341;

    public boolean isConnected(Context context) {
        // Get USB manager services and ask for all connected devices
        UsbManager manager = (UsbManager)context.getApplicationContext().getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> devices = manager.getDeviceList();

        // If there are any, iterate over them and compare their VIDs with Arduino's
        if (!devices.isEmpty()) {
            for (UsbDevice device : devices.values()) {
                int deviceVID = device.getVendorId();

                if (deviceVID == ARDUINO_VID) {
                    return true;
                }
            }
        }

        return false;
    }
}
