package com.m4gti.ecobreeze.ui.activities;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;
import com.m4gti.ecobreeze.utils.Globales;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button loginWithFingerprintButton;
    private static final String LOGIN_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=iniciar_sesion";
    private static final String LOGIN_HUELLA_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=iniciar_sesion_huella";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        loginWithFingerprintButton = findViewById(R.id.loginWithFingerprintButton); // Inicializar botón de huella

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

        // Configurar el botón de huella digital
        loginWithFingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar autenticación biométrica
                iniciarAutenticacionConHuella();
            }
        });
    }

    // Configuración de autenticación biométrica
    private void iniciarAutenticacionConHuella() {
        BiometricManager biometricManager;
        biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);

                            // Obtener el token_huella
                            String tokenHuella = obtenerTokenHuella();

                            // Iniciar sesión con huella
                            if (tokenHuella != null) {
                                LogicaLogin.loginConHuella(LoginActivity.this, tokenHuella, LOGIN_HUELLA_URL);
                            } else {
                                Toast.makeText(LoginActivity.this, "Error al obtener token de huella", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(LoginActivity.this, "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(LoginActivity.this, "Autenticación fallida, intenta de nuevo", Toast.LENGTH_SHORT).show();
                        }
                    });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Iniciar sesión con huella digital")
                    .setSubtitle("Usa tu huella digital para iniciar sesión")
                    .setNegativeButtonText("Cancelar")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "La autenticación biométrica no está disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private String obtenerTokenHuella() {
        // Método para obtener el token de huella, ajusta la lógica según tus necesidades
        return "TOKEN_HUELLA_DE_EJEMPLO";
    }
}
