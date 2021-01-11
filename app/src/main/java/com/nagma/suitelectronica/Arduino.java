package com.nagma.suitelectronica;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

public class Arduino {

    private static final int ARDUINO_VID = 0x2341;

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public UsbDevice usbDevice;
    public UsbDeviceConnection usbConnection;
    public UsbSerialDevice serialDevice;

    private Context context;
    private Handler readHandler;
    private UsbManager manager;

    /*
     *  Data received from serial port will be received here.
     */
    private UsbSerialInterface.UsbReadCallback readCallback = data -> {
        try {
            String message = new String(data, "UTF-8");

            if (readHandler != null)
                readHandler.obtainMessage(0, message).sendToTarget();
        } catch (UnsupportedEncodingException e) {
            // Bad message. Ignore it, I guess
        }
    };

    public Arduino(Context context, Handler readHandler) {
        manager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        this.context = context;
        this.readHandler = readHandler;
    }

    /*
     * Initializes the serial device. Returns true if it's successful.
     */
    public boolean initialize(UsbDevice device) {
        if (!manager.hasPermission(device)) {
            throw new SecurityException("This context doesn't have permission for this device.");
        }

        usbDevice = device;
        usbConnection = manager.openDevice(device);

        serialDevice = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);

        return serialDevice != null;
    }

    /*
     * Opens the connection to the serial device.
     */
    public void open(int baudrate) {
        serialDevice.open();

        configureSerialDevice(serialDevice, baudrate);

        serialDevice.read(readCallback);
    }

    /*
     * Closes the connection to the serial device.
     */
    public void close() {
        serialDevice.close();
    }

    /*
     * Writes data to the serial device.
     */
    public void write(byte[] data) {
        if (serialDevice != null)
            serialDevice.write(data);
    }

    private void configureSerialDevice(UsbSerialDevice serialDevice, int baudrate) {
        serialDevice.setBaudRate(baudrate);
        serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
        serialDevice.setParity(UsbSerialInterface.PARITY_NONE);

        serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
    }

    /*
     * Request user permission. The response will be received by whomever provided the context.
     */
    public void requestUserPermission(UsbDevice device) {
        PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);

        manager.requestPermission(device, intent);
    }

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
