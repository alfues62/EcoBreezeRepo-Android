package com.m4gti.ecobreeze.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaUser;

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
        // Llamar a la lógica de negocio a través de LogicaUser
        LogicaUser.cambiarCorreo(this, contrasenaActual, nuevoCorreo, new Response.Listener<JSONObject>() {
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
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvMensaje.setText("Error al realizar la solicitud");
            }
        });
    }
}