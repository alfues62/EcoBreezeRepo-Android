package com.m4gti.ecobreeze.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaUser;

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
        // Llamar a la lógica de negocio a través de LogicaUser
        LogicaUser.cambiarContrasena(this, contrasenaActual, nuevaContrasena, new Response.Listener<JSONObject>() {
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
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Manejo de error
                Toast.makeText(CambiarContrasenyaActivity.this, "Error al realizar la solicitud", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
