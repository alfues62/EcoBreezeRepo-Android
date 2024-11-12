package com.m4gti.ecobreeze.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;

public class MainActivity extends AppCompatActivity {
    private Button logoutButton;
    private Button scannerButton;
    private Button huellaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoutButton = findViewById(R.id.logoutButton);
        scannerButton = findViewById(R.id.scannerButton);
        huellaButton = findViewById(R.id.huellaButton);

        configurarBotones();
    }

    private void configurarBotones() {
        // Botón de logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogicaLogin.logout(MainActivity.this); // Llama al método de logout en LogicaLogin
            }
        });

        // Botón para ir a ScannerActivity
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(intent); // Inicia la actividad ScannerActivity
            }
        });

        // Botón para ir a HuellaActivity
        huellaButton.setOnClickListener(new View.OnClickListener() {
            @Override
               public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HuellaActivity.class);
                startActivity(intent); // Inicia la actividad HuellaActivity
            }
        });
    }
}