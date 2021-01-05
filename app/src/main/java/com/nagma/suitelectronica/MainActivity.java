package com.nagma.suitelectronica;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openButton_Click(View view) {
        EditText et = findViewById(R.id.receivedEditText);

        // Get devices
        UsbManager manager =
                (UsbManager)getApplicationContext().getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> devices = manager.getDeviceList();

        // If there are any, enumerate them in the receivedEditText
        if (devices.isEmpty()) {
            et.setText("No ports detected.");
        } else {
            String deviceString = "";

            for (UsbDevice device : devices.values()) {
                int deviceVID = device.getVendorId();

                deviceString += deviceVID + "\n";

                if (deviceVID == 0x2341) {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);

                    manager.requestPermission(device, pi);

                    deviceString += "Arduino!";

                    break;
                }
            }

            et.setText(deviceString);
        }
    }
}
