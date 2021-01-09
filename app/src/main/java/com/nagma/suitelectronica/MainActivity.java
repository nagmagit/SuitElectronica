package com.nagma.suitelectronica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int DETECTION_PERIOD = 200;

    private Arduino arduino;
    private Timer detectionTimer;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        detectionTimer = new Timer();
        startDetection();
    }

    public void openButton_Click(View view) {

    }

    private void startDetection() {
        detectionTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            TextView detectedTextView = findViewById(R.id.detectedTextView);

                            if (!Arduino.getSupportedDevices(context).isEmpty()) {
                                detectedTextView.setText(R.string.board_detected);
                            } else {
                                detectedTextView.setText(R.string.board_notdetected);
                            }
                        });
                    }
                }, 0, DETECTION_PERIOD);
    }
}
