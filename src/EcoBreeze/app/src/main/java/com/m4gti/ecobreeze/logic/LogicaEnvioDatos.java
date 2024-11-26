package com.m4gti.ecobreeze.logic;

import com.m4gti.ecobreeze.utils.Globales;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.ui.activities.ScannerActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LogicaEnvioDatos {
    private static final String SCAN_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=insertar_sensor"; // URL PARA INSERTAR LA MAC
    private static final String HUELLA_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=insertar_huella"; // URL PARA SUBIR LA HUELLA

    private Context context;
    public LogicaEnvioDatos(Context context) {
        this.context = context;
    }

    /**
     * Verifica si una dirección MAC es válida. [USADO EN SCANNERACTIVITY]
     *
     * @param mac La dirección MAC que se va a verificar.
     * @return true si el formato es válido, false en caso contrario.
     */
    public boolean esDireccionMacValida(String mac) {
        // Expresión regular para verificar el formato de una dirección MAC
        return mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    /**
     * Guarda la dirección MAC detectada en la base de datos remota. [USADO EN SCANNERACTIVITY]
     *
     * Este método envía una solicitud POST al servidor para registrar un nuevo sensor.
     * Envía como datos la dirección MAC y el identificador de usuario.
     *
     * @param mac La dirección MAC que será registrada en la base de datos.
     */
    public void guardarMacEnBD(String mac) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("mac", mac);
                jsonBody.put("usuario_id", userId);

                Log.d("JSON Enviado", jsonBody.toString()); // Log del JSON enviado

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        SCAN_URL, jsonBody,
                        response -> {
                            Log.d("API Response", response.toString());
                            try {
                                boolean success = response.getBoolean("success"); // Asegúrate de que "success" es parte de la respuesta
                                if (success) {
                                    Toast.makeText(context, "Sensor añadido exitosamente.", Toast.LENGTH_SHORT).show();
                                } else {
                                    String errorMessage = response.optString("error", "Error desconocido.");
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e("JSON Exception", "Error parsing response: " + e.getMessage());
                            }
                        },
                        error -> {
                            Log.e("LogicaEnvioDatos", "Error: " + error.getMessage());
                            if (error.networkResponse != null) {
                                Log.e("LogicaEnvioDatos", "Error code: " + error.networkResponse.statusCode);
                                Log.e("LogicaEnvioDatos", "Error response: " + new String(error.networkResponse.data));
                            }
                            Toast.makeText(context, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                        });

                requestQueue.add(jsonObjectRequest);
            } catch (JSONException e) {
                Log.e("JSON Exception", "Error creando JSON: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(context, "Error creando JSON", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Usuario no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    public void guardarTokenHuellaEnBD(String tokenHuella) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("token_huella", tokenHuella);  // Enviar el token de huella
                jsonBody.put("id", userId);  // Usamos el id del usuario en lugar de "usuario_id"

                Log.d("JSON Enviado", jsonBody.toString()); // Log del JSON enviado

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        HUELLA_URL, jsonBody,
                        response -> {
                            Log.d("API Response", response.toString());
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    Toast.makeText(context, "Token de huella añadido exitosamente.", Toast.LENGTH_SHORT).show();
                                } else {
                                    String errorMessage = response.optString("error", "Error desconocido.");
                                    Log.e("API Error", errorMessage); // Ver el mensaje de error
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e("JSON Exception", "Error parsing response: " + e.getMessage());
                            }
                        },
                        error -> {
                            Log.e("LogicaEnvioDatos", "Error: " + error.getMessage());
                            if (error.networkResponse != null) {
                                Log.e("LogicaEnvioDatos", "Error code: " + error.networkResponse.statusCode);
                                Log.e("LogicaEnvioDatos", "Error response: " + new String(error.networkResponse.data));
                            }
                            Toast.makeText(context, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                        });

                requestQueue.add(jsonObjectRequest);
            } catch (JSONException e) {
                Log.e("JSON Exception", "Error creando JSON: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(context, "Error creando JSON", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Usuario no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }
}