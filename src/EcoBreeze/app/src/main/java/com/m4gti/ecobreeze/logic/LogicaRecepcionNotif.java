package com.m4gti.ecobreeze.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.models.Notificacion;
import com.m4gti.ecobreeze.utils.Globales;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @class LogicaRecepcionNotif
 * @brief Clase encargada de gestionar la lógica de recepción y procesamiento de notificaciones desde el servidor.
 *
 * Esta clase se encarga de obtener las notificaciones asociadas a un usuario desde un servidor remoto,
 * procesar las respuestas recibidas, y notificar al componente correspondiente mediante un listener.
 *
 *   Métodos principales:
 *       1. Solicitar notificaciones desde el servidor para el usuario activo.
 *       2. Procesar las notificaciones obtenidas y almacenarlas en una lista.
 *       3. Comunicar la lista de notificaciones procesadas a través de una interfaz de callback.
 *
 * @note Utiliza `Volley` para realizar solicitudes HTTP y `SharedPreferences` para recuperar el ID del usuario.
 * @note Requiere un contexto válido de la aplicación y un listener para recibir las notificaciones procesadas.
 */
public class LogicaRecepcionNotif {
    private static final String NOTIFICACIONES_URL = "http://" + Globales.IP + ":8080/api/api_datos.php?action=obtener_notificaciones_usuario";
    private Context context;
    private OnNotificacionesRecibidasListener listener;

    // Constructor donde se pasa el listener
    public LogicaRecepcionNotif(Context context, OnNotificacionesRecibidasListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * @brief Obtiene las notificaciones desde el servidor para el usuario activo.
     *
     * Este método construye un JSON con el ID del usuario y realiza una solicitud POST al servidor
     * para obtener las notificaciones asociadas al usuario. Luego, procesa la respuesta para extraer
     * las notificaciones y las pasa al listener correspondiente.
     *
     * Diseño:
     *                  [obtenerNotificacionesDeServidor()] ---> Solicitud al servidor
     *
     */
    public void obtenerNotificacionesDeServidor() {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            // Construir el JSON para solicitar las notificaciones
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("usuario_id", userId); // Agregar el ID del usuario al JSON

                Log.d("JSON Enviado", jsonBody.toString()); // Log del JSON enviado

                // Crear una cola de solicitudes (RequestQueue)
                RequestQueue requestQueue = Volley.newRequestQueue(context);

                // Realizar la solicitud al servidor usando Volley
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        NOTIFICACIONES_URL, jsonBody,
                        response -> {
                            Log.d("API Response", response.toString());
                            try {
                                // Analizar la respuesta
                                boolean success = response.getBoolean("success"); // Verificar si la solicitud fue exitosa

                                if (success) {
                                    // Si la respuesta es exitosa, procesar las notificaciones
                                    JSONArray notificaciones = response.getJSONArray("notificaciones");
                                    procesarNotificaciones(notificaciones); // Llamar a un método para procesar las notificaciones
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
     * @brief Procesa las notificaciones obtenidas desde el servidor.
     *
     * Este método recorre el arreglo de notificaciones, parsea cada notificación y la convierte en
     * un objeto `Notificacion`. Luego, agrega todas las notificaciones procesadas a una lista y
     * las pasa al listener asociado.
     *
     * Diseño:
     *   notificaciones (JSONArray) ---> [procesarNotificaciones()]
     *
     * @param notificaciones Un arreglo JSON que contiene todas las notificaciones obtenidas desde el servidor.
     */
    private void procesarNotificaciones(JSONArray notificaciones) {
        // Verificamos si hay notificaciones y las procesamos
        if (notificaciones.length() > 0) {
            try {
                // Crear una lista para almacenar las notificaciones
                List<Notificacion> notificacionesList = new ArrayList<>();

                for (int i = 0; i < notificaciones.length(); i++) {
                    JSONObject notificacion = notificaciones.getJSONObject(i);

                    int idNotificacion = notificacion.getInt("NotificacionID");
                    String titulo = notificacion.getString("Titulo");
                    String cuerpo = notificacion.getString("Cuerpo");
                    String fecha = notificacion.getString("Fecha");

                    // Crear un objeto Notificacion para cada notificación
                    Notificacion notificacionObj = new Notificacion(idNotificacion, titulo, cuerpo, fecha);

                    // Agregar la notificación a la lista
                    notificacionesList.add(notificacionObj);

                    Log.d("Notificación", "Notificación - ID: " + idNotificacion + ", Título: " + titulo + ", Fecha: " + fecha);
                }

                // Pasar la lista completa de notificaciones al listener (actividad)
                if (listener != null) {
                    listener.onNotificacionesRecibidas(notificacionesList);
                }

            } catch (JSONException e) {
                Log.e("JSON Exception", "Error al procesar notificación: " + e.getMessage());
            }
        }
    }


    // Interfaz para pasar las notificaciones al MainActivity
    public interface OnNotificacionesRecibidasListener {
        void onNotificacionesRecibidas(List<Notificacion> notificaciones); // Cambiado para recibir una lista de notificaciones
        void onFailure(String errorMessage);
    }

}