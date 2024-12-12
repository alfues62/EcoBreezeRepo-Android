package com.m4gti.ecobreeze.logic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.models.UsuarioActivo;
import com.m4gti.ecobreeze.ui.activities.LoginActivity;
import com.m4gti.ecobreeze.ui.activities.MainActivity;
import com.m4gti.ecobreeze.ui.activities.ScannerActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @class LogicaLogin
 * @brief Clase encargada de gestionar la lógica del login.
 *
 * Esta clase se encarga de llamar a la api para poder autenticar al usuario
 *
 *   Métodos principales:
 *       1. Login
 *       2. LoginConHuella
 *       3. Logout
 *
 * @note Utiliza `Volley` para realizar solicitudes HTTP y `SharedPreferences` para recuperar el ID del usuario.
 * @note Requiere un contexto válido de la aplicación y un listener para recibir los datos procesados.
 */
public class LogicaLogin {

    /**
     * @brief Realiza el proceso de inicio de sesión utilizando email y contraseña.
     *
     * Este método envía una solicitud POST al servidor con las credenciales del usuario.
     * Si el inicio de sesión es exitoso, los datos del usuario se guardan en `SharedPreferences`
     * y se redirige al usuario a la actividad correspondiente.
     *
     * Diseño:
     *   email (String)
     *   password (String)
     *   LOGIN_URL (String)
     *        ---> [login()] ---> Solicitud POST al servidor
     *
     * @param context El contexto de la aplicación que ejecuta el método.
     * @param email El correo electrónico del usuario. Debe ser una cadena de texto no vacía.
     * @param password La contraseña del usuario. Debe ser una cadena de texto no vacía.
     * @param LOGIN_URL La URL del servidor donde se realizará el inicio de sesión.
     */
    public static void login(Context context, String email, String password, String LOGIN_URL) {
        // Crear el JSON para la solicitud
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("contrasena", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Crear la solicitud con Volley
        RequestQueue requestQueue = Volley.newRequestQueue(context);
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
                                SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("userId", usuarioActivo.getUserId());
                                editor.putString("userName", usuarioActivo.getUserName());
                                editor.putString("userRole", usuarioActivo.getUserRole());
                                editor.apply();

                                // Ir a La actividad de destino
                                Intent intent = new Intent(context, ScannerActivity .class);
                                context.startActivity(intent);
                                if (context instanceof LoginActivity) {
                                    ((LoginActivity) context).finish(); // Cerrar la actividad de login
                                }
                            } else {
                                String errorMessage = response.optString("error", "Error desconocido.");
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * @brief Realiza el proceso de inicio de sesión utilizando email y token de huella.
     *
     * Este método envía una solicitud POST al servidor con el correo electrónico y el token de huella del usuario.
     * Si el inicio de sesión es exitoso, los datos del usuario se guardan en `SharedPreferences`
     * y se redirige al usuario a la actividad principal.
     *
     * Diseño:
     *   email (String)
     *   tokenHuella (String)
     *   LOGIN_HUELLA_URL (String)
     *        ---> [loginConHuella()] ---> Solicitud POST al servidor
     *
     * @param context El contexto de la aplicación que ejecuta el método.
     * @param email El correo electrónico del usuario. Debe ser una cadena de texto no vacía.
     * @param tokenHuella El token de huella asociado al usuario. Debe ser una cadena de texto no vacía.
     * @param LOGIN_HUELLA_URL La URL del servidor donde se realizará el inicio de sesión con huella.
     */
    public static void loginConHuella(Context context, String email, String tokenHuella, String LOGIN_HUELLA_URL) {
        // Crear el JSON para la solicitud
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("token_huella", tokenHuella); // Enviar el token de huella en lugar de la contraseña
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Crear la solicitud con Volley
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, LOGIN_HUELLA_URL, jsonBody,
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
                                SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("userId", usuarioActivo.getUserId());
                                editor.putString("userName", usuarioActivo.getUserName());
                                editor.putString("userRole", usuarioActivo.getUserRole());
                                editor.apply();

                                // Ir a la actividad principal
                                Intent intent = new Intent(context, ScannerActivity.class);
                                context.startActivity(intent);
                                if (context instanceof LoginActivity) {
                                    ((LoginActivity) context).finish(); // Cerrar la actividad de login
                                }
                            } else {
                                String errorMessage = response.optString("error", "Error desconocido.");
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * @brief Realiza el proceso de cierre de sesión del usuario.
     *
     * Este método elimina todos los datos del usuario almacenados en `SharedPreferences`
     * y redirige al usuario a la actividad de inicio de sesión (`LoginActivity`).
     *
     * Diseño:
     *   context (Context)
     *        ---> [logout()] ---> Eliminar datos en `SharedPreferences`
     *                             ---> Redirigir a LoginActivity.
     *
     * @param context El contexto de la aplicación que ejecuta el método.
     */
    public static void logout(Context context) {
        // Borrar los datos de usuario almacenados
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Elimina todos los datos
        editor.apply();

        // Redirigir a LoginActivity
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Para limpiar la pila de actividades
        context.startActivity(intent);
    }
}
