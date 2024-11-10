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

                // Llamar a la función de inicio de sesión con correo y contraseña
                LogicaLogin.login(LoginActivity.this, email, password, LOGIN_URL);
            }
        });

        // Configurar el botón de huella digital
        loginWithFingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarAutenticacionConHuella();
            }
        });
    }

    // Función para iniciar la autenticación con huella digital
    private void iniciarAutenticacionConHuella() {
        // Verificar si el dispositivo soporta la autenticación biométrica (huella digital)
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            // Configurar el executor y el BiometricPrompt para autenticar al usuario con huella
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);

                            // Si la autenticación con huella fue exitosa, obtener el correo y el token de huella
                            String email = emailEditText.getText().toString().trim();
                            String tokenHuella = result.getCryptoObject() != null ? result.getCryptoObject().getSignature().toString() : null;

                            // Verificar que el correo y el token de huella no sean nulos
                            if (email.isEmpty() || tokenHuella == null) {
                                Toast.makeText(LoginActivity.this, "Correo y huella son necesarios", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Llamar al login con correo y huella (usando el correo y el token de huella)
                            LogicaLogin.loginConHuella(LoginActivity.this, email, tokenHuella, LOGIN_HUELLA_URL);
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

            // Crear la información del prompt para la autenticación
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Iniciar sesión con huella digital")
                    .setSubtitle("Usa tu huella digital para iniciar sesión")
                    .setNegativeButtonText("Cancelar")
                    .build();

            // Iniciar la autenticación biométrica
            biometricPrompt.authenticate(promptInfo);
        } else {
            // Si el dispositivo no soporta huella, mostrar mensaje
            Toast.makeText(this, "La autenticación biométrica no está disponible", Toast.LENGTH_SHORT).show();
        }
    }
}
