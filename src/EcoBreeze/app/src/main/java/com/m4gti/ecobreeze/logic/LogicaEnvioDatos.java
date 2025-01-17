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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @class LogicaEnvioDatos
 * @brief Clase encargada de gestionar la lógica relacionada con el envío de datos al servidor remoto.
 *
 * Esta clase proporciona métodos para interactuar con un servidor remoto utilizando la biblioteca Volley.
 * Permite registrar direcciones MAC, tokens de huella y notificaciones en la base de datos remota.
 * La configuración del usuario actual se obtiene a través de `SharedPreferences`.
 *
 *   Métodos principales:
 *       1. Verificación de formato de direcciones MAC.
 *       2. Envío de direcciones MAC detectadas al servidor.
 *       3. Envío de tokens de huella al servidor.
 *       4. Envío de notificaciones al servidor.
 *
 * @note Se requiere acceso a un contexto Android para inicializar esta clase.
 *
 */
public class LogicaEnvioDatos {
    private static final String SCAN_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=insertar_sensor"; // URL PARA INSERTAR LA MAC
    private static final String HUELLA_URL = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=insertar_huella"; // URL PARA SUBIR LA HUELLA
    private static final String NOTIFICACION_URL = "http://" + Globales.IP + ":8080/api/api_datos.php?action=insertar_notificacion"; // URL PARA INSERTAR LA NOTIFICACIÓN
    private static final String MEDICION_URL = "http://" + Globales.IP + ":8080/api/api_datos.php?action=insertar_medicion_usuario"; // URL PARA INSERTAR LA MEDICIÓN
    private Context context;
    public LogicaEnvioDatos(Context context) {
        this.context = context;
    }

    /**
     * @brief Verifica si una dirección MAC tiene un formato válido.
     *
     * Esta función utiliza una expresión regular para validar el formato de la dirección MAC.
     * Una dirección MAC válida debe cumplir con el formato estándar: seis pares de caracteres
     * hexadecimales separados por ':' o '-'.
     *
     * Diseño:
     *   mac (String) ---> [esDireccionMacValida()] ---> true/false (boolean)
     *
     * @param mac La dirección MAC que se va a verificar. Debe ser una cadena de texto.
     * @return true si el formato es válido; false en caso contrario.
     */
    public boolean esDireccionMacValida(String mac) {
        // Expresión regular para verificar el formato de una dirección MAC
        return mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    /**
     * @brief Guarda la dirección MAC detectada en la base de datos remota.
     *
     * Este método envía una solicitud POST al servidor para registrar un sensor asociado a un usuario.
     * Los datos enviados incluyen la dirección MAC del sensor y el ID del usuario actual.
     * Si ocurre un error durante el proceso, se muestra un mensaje al usuario.
     *
     * Diseño:
     *   mac (String)
     *        ---> [guardarMacEnBD()] ---> Solicitud POST al servidor
     *                                         ---> Respuesta de éxito o error.
     *
     * @param mac La dirección MAC que será registrada en la base de datos. Debe ser una cadena en formato válido.
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

    /**
     * @brief Registra un token de huella en la base de datos remota.
     *
     * Este método envía una solicitud POST al servidor para guardar un token de huella asociado
     * al usuario actual. El token de huella permite autenticar o identificar al usuario.
     *
     * Diseño:
     *   tokenHuella (String)
     *        ---> [guardarTokenHuellaEnBD()] ---> Solicitud POST al servidor
     *                                             ---> Respuesta de éxito o error.
     *
     * @param tokenHuella El token de huella que se registrará en la base de datos. Debe ser una cadena no vacía.
     */
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

    /**
     * @brief Registra una notificación en la base de datos remota.
     *
     * Este método envía una solicitud POST al servidor para guardar los detalles de una notificación.
     * Los datos enviados incluyen el título, el cuerpo, la fecha y el ID del usuario que creó la notificación.
     * Muestra mensajes de éxito o error al usuario según el resultado de la operación.
     *
     * Diseño:
     *   titulo (String)
     *   cuerpo (String)
     *   fecha (String)
     *        ---> [guardarNotificacionEnBD()] ---> Solicitud POST al servidor
     *                                             ---> Respuesta de éxito o error.
     *
     * @param titulo El título de la notificación. Debe ser una cadena no vacía.
     * @param cuerpo El cuerpo del mensaje de la notificación. Debe ser una cadena no vacía.
     * @param fecha La fecha de la notificación en formato `YYYY-MM-DD`. Debe ser válida.
     */
    public void guardarNotificacionEnBD(String titulo, String cuerpo, String fecha) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            // Crear el objeto JSON con los datos
            JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("titulo", titulo);
                jsonBody.put("cuerpo", cuerpo);
                jsonBody.put("fecha", fecha);
                jsonBody.put("usuario_id", userId);

                Log.d("JSON Enviado", jsonBody.toString()); // Log del JSON enviado

                // Crear la solicitud Volley
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        NOTIFICACION_URL, jsonBody, // Cambia esta URL
                        response -> {
                            Log.d("API Response", response.toString()); // Ver el contenido de la respuesta
                            try {
                                // Verifica si la respuesta contiene el campo "success" como booleano
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    Log.d("Notificación", "Notificación guardada exitosamente.");
                                    Toast.makeText(context, "Notificación guardada exitosamente.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Extraer el mensaje de error y mostrarlo
                                    String errorMessage = response.optString("error", "Error desconocido.");
                                    Log.e("Error", "Error al guardar la notificación: " + errorMessage);
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e("JSON Exception", "Error parsing response: " + e.getMessage());
                                Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Log.e("LogicaEnvioDatos", "Error: " + error.getMessage());
                            if (error.networkResponse != null) {
                                // Si hay respuesta del servidor, verifica el código de estado HTTP
                                Log.e("LogicaEnvioDatos", "Error code: " + error.networkResponse.statusCode);
                                Log.e("LogicaEnvioDatos", "Error response: " + new String(error.networkResponse.data));
                            }
                            // Toast en caso de error de red o de servidor
                            Toast.makeText(context, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                        });


                // Añadir la solicitud a la cola de solicitudes
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

    public void insertarMedicionEnBD(float valor, String lon, String lat, String fecha, String hora, int tipoGas) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            // Crear el objeto JSON con los datos
            JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("usuario_id", userId);  // ID del usuario
                jsonBody.put("valor", valor);        // Valor de la medición
                jsonBody.put("lon", lon);            // Longitud
                jsonBody.put("lat", lat);            // Latitud
                jsonBody.put("fecha", fecha);        // Fecha de la medición
                jsonBody.put("hora", hora);          // Hora de la medición
                jsonBody.put("tipo_gas", tipoGas);   // Tipo de gas

                Log.d("JSON Enviado", jsonBody.toString()); // Log del JSON enviado

                // Crear la solicitud Volley
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        MEDICION_URL, jsonBody, // Cambia esta URL por la correspondiente
                        response -> {
                            Log.d("API Response", response.toString()); // Ver el contenido de la respuesta
                            try {
                                // Verifica si la respuesta contiene el campo "success" como booleano
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    Log.d("Medición", "Medición guardada exitosamente.");
                                    Toast.makeText(context, "Medición guardada exitosamente.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Extraer el mensaje de error y mostrarlo
                                    String errorMessage = response.optString("error", "Error desconocido.");
                                    Log.e("Error", "Error al guardar la medición: " + errorMessage);
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e("JSON Exception", "Error parsing response: " + e.getMessage());
                                Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Log.e("LogicaEnvioDatos", "Error: " + error.getMessage());
                            if (error.networkResponse != null) {
                                // Si hay respuesta del servidor, verifica el código de estado HTTP
                                Log.e("LogicaEnvioDatos", "Error code: " + error.networkResponse.statusCode);
                                Log.e("LogicaEnvioDatos", "Error response: " + new String(error.networkResponse.data));
                            }
                            // Toast en caso de error de red o de servidor
                            Toast.makeText(context, "Ocurrió un error en el servidor", Toast.LENGTH_SHORT).show();
                        });

                // Añadir la solicitud a la cola de solicitudes
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