package com.m4gti.ecobreeze.logic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.models.Medicion;
import com.m4gti.ecobreeze.utils.Globales;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LogicaRecepcionDatos {
    private static final String MEDICIONES_URL = "http://" + Globales.IP + ":8080/api/api_datos.php?action=obtener_mediciones_usuario";
    private Context context;
    private OnMedicionRecibidaListener listener;

    // Constructor donde se pasa el listener
    public LogicaRecepcionDatos(Context context, OnMedicionRecibidaListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void obtenerMedicionesDeServidor() {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            // Construir el JSON para solicitar las mediciones
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("usuario_id", userId); // Agregar el ID del usuario al JSON

                Log.d("JSON Enviado", jsonBody.toString()); // Log del JSON enviado

                // Crear una cola de solicitudes (RequestQueue)
                RequestQueue requestQueue = Volley.newRequestQueue(context);

                // Realizar la solicitud al servidor usando Volley
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        MEDICIONES_URL, jsonBody,
                        response -> {
                            Log.d("API Response", response.toString());
                            try {
                                // Analizar la respuesta
                                boolean success = response.getBoolean("success"); // Verificar si la solicitud fue exitosa

                                if (success) {
                                    // Si la respuesta es exitosa, procesar las mediciones
                                    JSONArray mediciones = response.getJSONArray("mediciones");
                                    procesarMediciones(mediciones); // Llamar a un método para procesar las mediciones
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

                // Añadir la solicitud a la cola
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

    private void procesarMediciones(JSONArray mediciones) {
        // Verificamos si hay mediciones y obtenemos la última
        if (mediciones.length() > 0) {
            try {
                // Tomamos la última medición
                JSONObject ultimaMedicion = mediciones.getJSONObject(mediciones.length() - 1);

                int idMedicion = ultimaMedicion.getInt("IDMedicion");
                double valor = ultimaMedicion.getDouble("Valor");
                double lon = ultimaMedicion.getDouble("Lon");
                double lat = ultimaMedicion.getDouble("Lat");
                String fecha = ultimaMedicion.getString("Fecha");
                String hora = ultimaMedicion.getString("Hora");
                String categoria = ultimaMedicion.getString("Categoria");
                int tipoGasId = ultimaMedicion.getInt("TIPOGAS_TipoID");
                String tipoGas = ultimaMedicion.getString("TipoGas");

                // Crear un objeto Medicion para la última medición
                Medicion medicion = new Medicion(idMedicion, valor, lon, lat, fecha, hora, categoria, tipoGasId, tipoGas);

                // Pasar la última medición al listener (MainActivity)
                if (listener != null) {
                    listener.onMedicionRecibida(medicion);
                }

                Log.d("Medicion", "Última medición - ID: " + idMedicion + ", Valor: " + valor + ", Fecha: " + fecha + ", Hora: " + hora);

            } catch (JSONException e) {
                Log.e("JSON Exception", "Error al procesar medición: " + e.getMessage());
            }
        }
    }

    // Interfaz para pasar la última medición al MainActivity
    public interface OnMedicionRecibidaListener {
        void onMedicionRecibida(Medicion medicion);
    }
}


