package com.m4gti.ecobreeze.ui.activities;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private static final String LOGIN_URL = "http://192.168.1.58:8080/api/api_usuario.php?action=iniciar_sesion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los EditText dentro del onClick
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Verificar que los campos no estén vacíos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Llamar a la función de inicio de sesión
                LogicaLogin.login(LoginActivity.this, email, password, LOGIN_URL);
            }
        });
    }
}
