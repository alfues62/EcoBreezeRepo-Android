package com.m4gti.ecobreeze.ui.activities;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaEnvioDatos;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.UUID;
import java.util.concurrent.Executor;

public class HuellaActivity extends AppCompatActivity {
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private LogicaEnvioDatos logicaEnvioDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huella);

        logicaEnvioDatos = new LogicaEnvioDatos(this);

        // Configurar el executor para el BiometricPrompt
        Executor executor = ContextCompat.getMainExecutor(this);

        // Crear el BiometricPrompt
        biometricPrompt = new BiometricPrompt(HuellaActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Mostrar mensaje de error
                showToast("Error de autenticación: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // La autenticación fue exitosa, generar el token y proceder
                generateFingerprintToken();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Mostrar mensaje de fallo en la autenticación
                showToast("Autenticación fallida");
            }
        });

        // Configurar el prompt de autenticación
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación biométrica")
                .setSubtitle("Inicia sesión usando tu huella")
                .setNegativeButtonText("Cancelar")
                .build();

        // Iniciar la autenticación
        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Método que se llama cuando la autenticación es exitosa.
     * Aquí generamos un token único para la huella y lo asociamos al usuario.
     */
    private void generateFingerprintToken() {
        // Generar un token único (UUID)
        String fingerprintToken = UUID.randomUUID().toString();
        // Mostrar el token generado (en una aplicación real, lo guardarías en la base de datos asociada al usuario)
        showToast("Autenticación exitosa! Token generado: " + fingerprintToken);
        Log.d(TAG, "Token generado: " + fingerprintToken);
        // Aquí puedes asociar el token a tu base de datos, por ejemplo:
        if (logicaEnvioDatos != null) {
            logicaEnvioDatos.guardarTokenHuellaEnBD(fingerprintToken);
        } else {
            Log.e(TAG, "logicaEnvioDatos es nulo, no se puede guardar el token.");
        }
    }

    /**
     * Método para mostrar un mensaje en un Toast.
     */
    private void showToast(String message) {
        Toast.makeText(HuellaActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
