package org.sheedon.carshapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import org.sheedon.crashlibrary.Crash;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Crash.init(getApplication(), false, true, 0, MainActivity.class, "123");
    }

    public void onCrashClick(View view) {
        int k = 1 / 0;
    }
}