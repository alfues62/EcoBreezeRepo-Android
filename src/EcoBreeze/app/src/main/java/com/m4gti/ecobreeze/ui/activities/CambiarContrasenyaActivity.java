package com.m4gti.ecobreeze.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.utils.Globales;

import org.json.JSONObject;

public class CambiarContrasenyaActivity extends AppCompatActivity {
    private EditText edtContrasenaActual, edtNuevaContrasena, edtConfirmarContrasena;
    private Button btnCambiarContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contrasenya);  // Asegúrate de que el XML esté configurado correctamente

        // Inicializar vistas
        edtContrasenaActual = findViewById(R.id.edtContrasenaActual);
        edtNuevaContrasena = findViewById(R.id.edtNuevaContrasena);
        edtConfirmarContrasena = findViewById(R.id.edtConfirmarContrasena);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);

        // Configurar el botón para cambiar la contraseña
        btnCambiarContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los datos del formulario
                String contrasenaActual = edtContrasenaActual.getText().toString().trim();
                String nuevaContrasena = edtNuevaContrasena.getText().toString().trim();
                String confirmarContrasena = edtConfirmarContrasena.getText().toString().trim();

                // Validar los campos
                if (contrasenaActual.isEmpty() || nuevaContrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                    Toast.makeText(CambiarContrasenyaActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!nuevaContrasena.equals(confirmarContrasena)) {
                    Toast.makeText(CambiarContrasenyaActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Enviar la solicitud para cambiar la contraseña
                cambiarContrasena(contrasenaActual, nuevaContrasena);
            }
        });
    }

    private void cambiarContrasena(String contrasenaActual, String nuevaContrasena) {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("userId", -1);  // Recupera el ID del usuario

        // Verificar si se obtuvo el ID del usuario
        if (idUsuario == -1) {
            Toast.makeText(CambiarContrasenyaActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;  // Si no se encuentra el usuario, no hacer la solicitud
        }

        // Crear el objeto JSON con los datos
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("id", idUsuario);
            requestData.put("contrasena_actual", contrasenaActual);
            requestData.put("nueva_contrasena", nuevaContrasena);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // URL del endpoint
        String url = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=cambiar_contrasena";

        // Crear una solicitud POST con Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String message = response.getString("message");
                                Toast.makeText(CambiarContrasenyaActivity.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                String error = response.getString("error");
                                Toast.makeText(CambiarContrasenyaActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(CambiarContrasenyaActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        // Manejo de error
                        Toast.makeText(CambiarContrasenyaActivity.this, "Error al realizar la solicitud", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Añadir la solicitud a la cola de Volley
        Volley.newRequestQueue(this).add(request);
    }
}
