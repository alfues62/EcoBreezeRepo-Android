package com.m4gti.ecobreeze.ui.activities;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;
import com.m4gti.ecobreeze.utils.Globales;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button loginWithFingerprintButton;
    private static final String LOGIN_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=iniciar_sesion";
    private static final String LOGIN_HUELLA_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=iniciar_sesion_huella";
    private static final String TOKEN_HUELLA_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=obtener_token_huella";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        loginWithFingerprintButton = findViewById(R.id.loginWithFingerprintButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                LogicaLogin.login(LoginActivity.this, email, password, LOGIN_URL);
            }
        });

        loginWithFingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                Log.d("LoginActivity", "Email introducido: " + email); // Confirmación de email
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "El correo es necesario", Toast.LENGTH_SHORT).show();
                    return;
                }

                iniciarAutenticacionConHuella(email);
            }
        });
    }

    private void iniciarAutenticacionConHuella(String email) {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            obtenerTokenHuellaDesdeBaseDeDatos(email);
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

    private void obtenerTokenHuellaDesdeBaseDeDatos(String email) {
        new Thread(() -> {
            try {
                // Codificar el email
                String encodedEmail = URLEncoder.encode(email, "UTF-8");
                String urlString = TOKEN_HUELLA_URL + "&email=" + encodedEmail;
                Log.d("LoginActivity", "URL de token de huella: " + urlString);
                URL url = new URL(urlString);

                // Abrir la conexión HTTP
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                // Leer la respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                // Manejar el resultado en el hilo principal
                new Handler(Looper.getMainLooper()).post(() -> procesarRespuestaToken(result.toString(), email));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(LoginActivity.this, "Error en la conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void procesarRespuestaToken(String result, String email) {
        if (result != null && !result.isEmpty()) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    String tokenHuella = jsonResponse.getString("token_huella");
                    Log.i("TokenHuella", "Token de huella: " + tokenHuella);

                    // Llamar al login con correo y huella
                    LogicaLogin.loginConHuella(LoginActivity.this, email, tokenHuella, LOGIN_HUELLA_URL);
                } else {
                    String error = jsonResponse.getString("error");
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Error en la conexión", Toast.LENGTH_SHORT).show();
        }
    }
}
