package com.afusesc.appbioma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.mindrot.jbcrypt.BCrypt;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextNombre, editTextApellidos, editTextEmail, editTextContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextNombre = findViewById(R.id.editTextNombre);
        editTextApellidos = findViewById(R.id.editTextApellidos);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextContrasena = findViewById(R.id.editTextContrasena);

        Button buttonRegistrar = findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = editTextNombre.getText().toString();
        String apellidos = editTextApellidos.getText().toString();
        String email = editTextEmail.getText().toString();
        String contrasena = editTextContrasena.getText().toString();

        // Hashear la contraseña usando BCrypt
        String contrasenaHash = hashPassword(contrasena);

        new Thread(() -> {
            try {
                String resultado = crearUsuario(nombre, apellidos, email, contrasenaHash);

                // Mostrar mensaje en la UI y redirigir si el registro es exitoso
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, resultado, Toast.LENGTH_LONG).show();

                    if (resultado.equals("Registro exitoso")) { // Asumiendo que el servidor devuelve esta respuesta en caso de éxito
                        // Redirigir a LoginActivity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza RegisterActivity para que el usuario no pueda volver con el botón "Atrás"
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String crearUsuario(String nombre, String apellidos, String email, String contrasenaHash) throws IOException {
        String urlString = "http://192.168.1.59:8080/api/api_usuario.php"; // Cambia esto por tu URL real
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("Nombre", nombre);
                jsonParam.put("Apellidos", apellidos);
                jsonParam.put("Email", email);
                jsonParam.put("ContrasenaHash", contrasenaHash); // Mandamos la contraseña hasheada
                jsonParam.put("ROL_RolID", 2); // Asignar un rol por defecto
            } catch (JSONException e) {
                e.printStackTrace();
                return "Error al crear el objeto JSON";
            }

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonParam.toString());
            writer.flush();
            writer.close();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString(); // Suponiendo que la respuesta exitosa es "Registro exitoso"
            } else {
                return "Error en el registro";
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    // Método para hashear la contraseña
    private String hashPassword(String password) {
        // Elige un número de rondas de encriptación. Por lo general, 10 es una buena opción.
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
}


