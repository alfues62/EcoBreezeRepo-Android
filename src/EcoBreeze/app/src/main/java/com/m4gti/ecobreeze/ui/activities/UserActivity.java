package com.m4gti.ecobreeze.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;

public class UserActivity extends AppCompatActivity {
    private Button correoButton;
    private Button contrasenyaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        correoButton = findViewById(R.id.correoButton);
        contrasenyaButton = findViewById(R.id.contrasenyaButton);

        configurarBotones();
    }

    private void configurarBotones() {
        // Botón para ir a CambiarContrasenyaActivity
        contrasenyaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, CambiarContrasenyaActivity.class);
                startActivity(intent); // Inicia la actividad ScannerActivity
            }
        });

        // Botón para ir a CambiarCorreoActivity
        correoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, CambiarCorreoActivity.class);
                startActivity(intent); // Inicia la actividad ScannerActivity
            }
        });
    }

}
