package com.nagma.suitelectronica;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Arduino arduino;
    Timer detectionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arduino = new Arduino();
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView detectedTextView = findViewById(R.id.detectedTextView);

                                if (arduino.isConnected(getApplicationContext())) {
                                    detectedTextView.setText(R.string.board_detected);
                                } else {
                                    detectedTextView.setText(R.string.board_notdetected);
                                }
                            }
                        });
                    }
                }, 0, 500);
    }
}
