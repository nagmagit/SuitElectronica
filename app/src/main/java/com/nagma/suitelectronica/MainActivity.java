package com.nagma.suitelectronica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int DETECTION_PERIOD = 200;

    private Arduino arduino;
    private Timer detectionTimer;

    private Context context;
    private UsbDevice detectedDevice;

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Arduino.ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                onPermissionResponse(granted);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        setFilter();

        detectionTimer = new Timer();
        startDetection();
    }

    private void onPermissionResponse(boolean granted) {
        if (granted) {
            Spinner baudrateSpinner = findViewById(R.id.baudrateSpinner);
            int baudrate = Integer.parseInt((String)baudrateSpinner.getSelectedItem());

            boolean initSuccess = arduino.initialize(detectedDevice);

            if (initSuccess) {
                arduino.open(baudrate);
            }

            toggleOpenState(initSuccess);
        } else {
            Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onReceivedMessage(String message) {
        EditText receivedEditText = findViewById(R.id.receivedEditText);
        receivedEditText.append(message);
    }

    public void openButton_Click(View view) {
        if (detectedDevice != null) {
            arduino = new Arduino(context, new ReadHandler(this));
            arduino.requestUserPermission(detectedDevice);
        } else {
            Toast.makeText(context, "No devices detected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeButton_Click(View view) {
        if (detectedDevice != null) {
            arduino.close();
            toggleOpenState(false);
        } else {
            Toast.makeText(context, "No devices detected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendButton_Click(View view) {
        EditText sendEditText = findViewById(R.id.sendEditText);
        String messageToSend = sendEditText.getText().toString();

        if (detectedDevice != null) {
            arduino.write(messageToSend.getBytes());
        } else {
            Toast.makeText(context, "No devices detected.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Enables or disables the views that should only be enabled when the connection is open.
     */
    private void toggleOpenState(boolean isOpen) {
        findViewById(R.id.baudrateSpinner).setEnabled(!isOpen);
        findViewById(R.id.openButton).setEnabled(!isOpen);

        findViewById(R.id.closeButton).setEnabled(isOpen);
        findViewById(R.id.sendEditText).setEnabled(isOpen);
        findViewById(R.id.sendButton).setEnabled(isOpen);
    }

    /*
     * Sets up the broadcast receiver to know when the user accepted the permission.
     */
    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Arduino.ACTION_USB_PERMISSION);
        registerReceiver(receiver, filter);
    }

    /*
     * Start a timer to check if there are any supported devices connected.
     */
    private void startDetection() {
        detectionTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            TextView detectedTextView = findViewById(R.id.detectedTextView);
                            ArrayList<UsbDevice> supportedDevices = Arduino.getSupportedDevices(context);

                            if (supportedDevices.isEmpty()) {
                                detectedTextView.setText(R.string.board_notdetected);
                                detectedDevice = null;
                                toggleOpenState(false);
                            } else {
                                detectedTextView.setText(R.string.board_detected);
                                detectedDevice = supportedDevices.get(0);
                            }
                        });
                    }
                }, 0, DETECTION_PERIOD);
    }

    private static class ReadHandler extends Handler {
        private final WeakReference<MainActivity> activity;

        public ReadHandler(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            String data = (String) message.obj;

            activity.get().onReceivedMessage(data);
        }
    }
}
