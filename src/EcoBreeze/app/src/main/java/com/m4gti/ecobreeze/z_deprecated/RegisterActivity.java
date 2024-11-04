package com.m4gti.ecobreeze.z_deprecated;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.ui.activities.MainActivity;

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

// --------------------------------------------------------------
/**
 * @brief Actividad que gestiona el regisrro de usuarios en la app.
 *
 *
 * ESTA CLASE YA NO SERÁ USADA A PARTIR DEL SPRINT 1, ESTÁ AQUÍ POR SI ALGÚN DÍA NOS HACE FALTA.
 */
// --------------------------------------------------------------
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextNombre, editTextApellidos, editTextEmail, editTextContrasena;

    // --------------------------------------------------------------
    /**
     * @brief Método que se llama al crear la actividad de registro.
     *
     * Configura los elementos de la interfaz de usuario y el botón de registro, y establece
     * un listener para iniciar el proceso de registro del usuario cuando el botón se presiona.
     *
     * Parámetros:
     *      @param savedInstanceState Estado previamente guardado de la actividad, si está disponible.
     */
    // --------------------------------------------------------------
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

    // --------------------------------------------------------------
    /**
     * @brief Inicia el proceso de registro de usuario.
     *
     * Obtiene los datos ingresados en los campos de texto, aplica un hash a la contraseña
     * y ejecuta una petición de registro en un hilo separado. Si el registro es exitoso, muestra un mensaje
     * y redirige al usuario a la pantalla de inicio de sesión.
     */
    // --------------------------------------------------------------
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
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza RegisterActivity para que el usuario no pueda volver con el botón "Atrás"
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --------------------------------------------------------------
    /**
     * @brief Crea el registro del usuario en la base de datos.
     *
     * Este método realiza una solicitud POST al servidor con los datos del usuario para registrar
     * su cuenta en la base de datos.
     *
     * Parámetros:
     *      @param nombre Nombre del usuario.
     *      @param apellidos Apellidos del usuario.
     *      @param email Correo electrónico del usuario.
     *      @param contrasenaHash Contraseña del usuario, previamente encriptada.
     *
     * @return Un mensaje indicando si el registro fue exitoso o si hubo un error.
     *
     * @throws IOException En caso de problemas de red o de conexión con el servidor.
     */
    // --------------------------------------------------------------
    private String crearUsuario(String nombre, String apellidos, String email, String contrasenaHash) throws IOException {
        String urlString = "http://192.168.30.180:8080/api/api_usuario.php"; // Cambia esto por tu URL real
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

    // --------------------------------------------------------------
    /**
     * @brief Aplica un hash a la contraseña del usuario.
     *
     * Usa el algoritmo BCrypt para encriptar la contraseña del usuario antes de enviarla al servidor.
     * Un valor de "10" se usa como número de rondas de encriptación, ofreciendo un buen balance entre seguridad y rendimiento.
     *
     * Parámetros:
     *      @param password La contraseña del usuario en texto plano.
     *
     * @return La contraseña hasheada usando BCrypt.
     */
    // --------------------------------------------------------------
    private String hashPassword(String password) {
        // Elige un número de rondas de encriptación. Por lo general, 10 es una buena opción.
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
}