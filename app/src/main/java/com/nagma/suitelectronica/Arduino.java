package com.nagma.suitelectronica;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.felhr.usbserial.UsbSerialDevice;

import java.util.ArrayList;
import java.util.Map;

public class Arduino {

    public static final int ARDUINO_VID = 0x2341;

    /*
     * Returns all UsbDevices compatible with UsbSerial and that are Arduinos.
     */
    public static ArrayList<UsbDevice> getSupportedDevices(Context context) {
        // Get USB manager services and ask for all connected devices
        UsbManager manager = (UsbManager)context.getApplicationContext().getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> usbDevices = manager.getDeviceList();

        ArrayList<UsbDevice> supportedDevices = new ArrayList<>();

        // Iterate over them and select the ones supported by UsbSerial and with Arduino's VID
        for (UsbDevice device : usbDevices.values()) {
            int deviceVID = device.getVendorId();

            if (UsbSerialDevice.isSupported(device) && deviceVID == ARDUINO_VID) {
                supportedDevices.add(device);
            }
        }

        return supportedDevices;
    }
}
