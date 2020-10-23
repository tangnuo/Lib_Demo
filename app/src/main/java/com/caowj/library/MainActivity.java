package com.caowj.library;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.caowj.library.activity.NetworkTestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void testNetwork(View view) {
        startActivity(new Intent(this, NetworkTestActivity.class));
    }

    public void testLog(View view) {

    }

    public void testWidgets(View view) {

    }

    public void testUtils(View view) {

    }
}
