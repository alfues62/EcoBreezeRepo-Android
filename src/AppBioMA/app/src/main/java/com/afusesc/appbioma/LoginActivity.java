package com.afusesc.appbioma;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

// --------------------------------------------------------------
/**
 * @brief Actividad que gestiona el inicio de sesión en la app.
 */
// --------------------------------------------------------------
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;

    private static final String LOGIN_URL = "http://192.168.30.180:8080/api/api_usuario.php?action=iniciar_sesion";

    // --------------------------------------------------------------
    /**
     * @brief Método que se llama al crear la actividad de inicio de sesión.
     *
     * Configura la interfaz de usuario, obteniendo las referencias a los elementos de edición de texto
     * y el botón de inicio de sesión. Agrega un listener al botón para ejecutar el método de login.
     *
     * Parámetros:
     *      @param savedInstanceState Estado previamente guardado de la actividad, si está disponible.
     */
    // --------------------------------------------------------------
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
                login();
            }
        });
    }

    // --------------------------------------------------------------
    /**
     * @brief Inicia el proceso de inicio de sesión del usuario.
     *
     * Este método valida que los campos de texto no estén vacíos, y luego envía una solicitud
     * de inicio de sesión al servidor. Si el inicio de sesión es exitoso, guarda los datos del usuario
     * en `SharedPreferences` y redirige a `MainActivity`.
     */
    // --------------------------------------------------------------
    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el JSON para la solicitud
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("action", "iniciar_sesion");
            jsonBody.put("email", email);
            jsonBody.put("contrasena", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Crear la solicitud con Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                // Obtener datos del usuario
                                JSONObject usuario = response.getJSONObject("usuario");
                                int userId = usuario.getInt("ID");
                                String userName = usuario.getString("Nombre");
                                String userRole = usuario.getString("Rol");

                                // Crear una instancia de UsuarioActivo
                                UsuarioActivo usuarioActivo = new UsuarioActivo(userId, userName, userRole);

                                // Guardar datos del usuario en SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("userId", usuarioActivo.getUserId());
                                editor.putString("userName", usuarioActivo.getUserName());
                                editor.putString("userRole", usuarioActivo.getUserRole());
                                editor.apply();

                                // Ir a MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Cerrar la actividad de login
                            } else {
                                String errorMessage = response.optString("error", "Error desconocido.");
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoginActivity", "Error: " + error.getMessage());
                        Toast.makeText(LoginActivity.this, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
