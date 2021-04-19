package com.example.socio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button Continuebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Continuebtn = (Button)findViewById(R.id.continue_btn);

        Continuebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread =new Thread() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                    }
                };
                thread.start();
            }
        });

    }
}