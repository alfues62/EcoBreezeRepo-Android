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

import java.util.ArrayList;
import java.util.List;

/**
 * @class LogicaRecepcionDatos
 * @brief Clase encargada de gestionar la lógica de recepción y procesamiento de mediciones desde el servidor.
 *
 *Esta clase se encarga de obtener datos de mediciones asociados a un usuario desde un servidor remoto, procesar las respuestas, y notificar al componente correspondiente mediante un listener.
 *
 * Métodos principales:
 *
 *  1. Solicitar mediciones desde el servidor para el usuario activo.
 *  2. Procesar las mediciones obtenidas, extrayendo la más reciente.
 *  3. Comunicar la medición procesada a través de una interfaz de callback.
 *
 * @note Utiliza `Volley` para realizar solicitudes HTTP y `SharedPreferences` para recuperar el ID del usuario.
 * @note Requiere un contexto válido de la aplicación y un listener para recibir las notificaciones procesadas.
 */
public class LogicaRecepcionDatos {
    private static final String MEDICIONES_URL = "http://" + Globales.IP + ":8080/api/api_datos.php?action=obtener_mediciones_usuario";
    private Context context;
    private OnMedicionRecibidaListener listener;

    private List<Medicion> mediciones = new ArrayList<>();

    // Constructor de la clase
    public LogicaRecepcionDatos(Context context, OnMedicionRecibidaListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * @brief Realiza una solicitud al servidor para obtener las mediciones del usuario activo.
     *
     * Este método utiliza el ID del usuario almacenado en `SharedPreferences` para construir una solicitud
     * al servidor. Si la respuesta es exitosa, se procesan las mediciones obtenidas.
     *
     * Diseño:
     *                     [obtenerMedicionesDeServidor()] ---> Solicitud POST al servidor
     *
     */
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

    /**
     * @brief Procesa las mediciones obtenidas desde el servidor.
     *
     * Este método analiza un arreglo de mediciones, selecciona la última y la convierte en un objeto
     * `Medicion`. Finalmente, pasa la medición procesada al listener asociado.
     *
     * Diseño:
     *   mediciones (JSONArray) ---> [procesarMediciones()]
     *
     * @param medicionesArray Un arreglo JSON que contiene todas las mediciones obtenidas desde el servidor.
     */
    private void procesarMediciones(JSONArray medicionesArray) {
        try {
            // Limpiar lista anterior antes de agregar nuevas mediciones
            mediciones.clear();

            // Procesar todas las mediciones obtenidas
            for (int i = 0; i < medicionesArray.length(); i++) {
                JSONObject medicionJson = medicionesArray.getJSONObject(i);

                // Crear un objeto Medicion y agregarlo a la lista
                Medicion medicion = new Medicion(
                        medicionJson.getInt("IDMedicion"),
                        medicionJson.getDouble("Valor"),
                        medicionJson.getDouble("Lon"),
                        medicionJson.getDouble("Lat"),
                        medicionJson.getString("Fecha"),
                        medicionJson.getString("Hora"),
                        medicionJson.getString("Categoria"),
                        medicionJson.getInt("TIPOGAS_TipoID"),
                        medicionJson.getString("TipoGas")
                );
                mediciones.add(medicion);
            }

            // Llamar al listener si es necesario (para la última medición)
            if (listener != null && !mediciones.isEmpty()) {
                listener.onMedicionRecibida(mediciones.get(mediciones.size() - 1)); // Última medición
            }

        } catch (JSONException e) {
            Log.e("JSON Exception", "Error al procesar las mediciones: " + e.getMessage());
        }
    }

    // Método para obtener todas las mediciones
    public List<Medicion> getMediciones() {
        return mediciones;
    }

    public interface OnMedicionRecibidaListener {
        void onMedicionRecibida(Medicion medicion);
    }
}