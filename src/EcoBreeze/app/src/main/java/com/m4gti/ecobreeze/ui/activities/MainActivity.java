package com.m4gti.ecobreeze.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;

public class MainActivity extends AppCompatActivity {

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logoutButton = findViewById(R.id.logoutButton);

        configurarBotones();
    }

    private void configurarBotones() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogicaLogin.logout(MainActivity.this); // Llama al método de logout en LogicaLogin
            }
        });
    }
}