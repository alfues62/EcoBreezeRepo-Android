package com.m4gti.ecobreeze.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.utils.Globales;

import org.json.JSONObject;

public class CambiarCorreoActivity extends AppCompatActivity {

    private EditText etContrasenaActual, etNuevoCorreo;
    private Button btnCambiarCorreo;
    private TextView tvMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correo);

        etContrasenaActual = findViewById(R.id.etContrasenaActual);
        etNuevoCorreo = findViewById(R.id.etNuevoCorreo);
        btnCambiarCorreo = findViewById(R.id.btnCambiarCorreo);
        tvMensaje = findViewById(R.id.tvMensaje);

        // Configurar el botón para cambiar el correo
        btnCambiarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los datos ingresados
                String contrasenaActual = etContrasenaActual.getText().toString().trim();
                String nuevoCorreo = etNuevoCorreo.getText().toString().trim();

                if (contrasenaActual.isEmpty() || nuevoCorreo.isEmpty()) {
                    tvMensaje.setText("Por favor, complete todos los campos.");
                    return;
                }

                // Llamar al método para cambiar el correo
                cambiarCorreo(contrasenaActual, nuevoCorreo);
            }
        });
    }

    private void cambiarCorreo(String contrasenaActual, String nuevoCorreo) {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("userId", -1);

        if (idUsuario == -1) {
            tvMensaje.setText("No se ha encontrado el ID del usuario.");
            return;
        }

        // Crear el objeto JSON para enviar la solicitud
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("id", idUsuario);
            requestData.put("contrasena_actual", contrasenaActual);
            requestData.put("nuevo_correo", nuevoCorreo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // URL del endpoint (ajusta el puerto si es necesario)
        String url = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=cambiar_correo";

        // Crear una solicitud POST con Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String message = response.getString("message");
                                Toast.makeText(CambiarCorreoActivity.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                String error = response.getString("error");
                                tvMensaje.setText(error);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            tvMensaje.setText("Error al procesar la respuesta");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        tvMensaje.setText("Error al realizar la solicitud");
                    }
                }
        );

        // Añadir la solicitud a la cola de Volley
        Volley.newRequestQueue(this).add(request);
    }
}
