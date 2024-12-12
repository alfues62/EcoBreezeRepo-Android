package com.m4gti.ecobreeze.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.m4gti.ecobreeze.utils.Globales;
import org.json.JSONObject;

/**
 * @class LogicaUser
 * @brief Clase encargada de gestionar las operaciones relacionadas con el usuario, como la actualización de correo electrónico y contraseña.
 *
 * Esta clase utiliza la biblioteca Volley para realizar solicitudes HTTP al servidor. Proporciona métodos para cambiar el correo electrónico y la contraseña del usuario.
 *
 * Métodos principales:
 *   1. `cambiarCorreo()`: Enviar una solicitud POST para actualizar el correo electrónico del usuario.
 *   2. `cambiarContrasena()`: Enviar una solicitud POST para actualizar la contraseña del usuario.
 *
 * @note Requiere el contexto de la aplicación y utiliza `SharedPreferences` para obtener el ID del usuario.
 */
public class LogicaUser {

    /**
     * @brief Método para cambiar el correo electrónico del usuario.
     *
     * Este método envía una solicitud POST al servidor para actualizar el correo del usuario.
     * Primero, obtiene el ID del usuario desde `SharedPreferences`, luego crea un objeto JSON
     * con la nueva dirección de correo y la contraseña actual. Finalmente, realiza la solicitud
     * y gestiona las respuestas a través de listeners.
     *
     * Diseño:
     *   idUsuario, contrasenaActual, nuevoCorreo (String) ---> [cambiarCorreo()] ---> Solicitud POST al servidor
     *
     * @param context El contexto de la aplicación.
     * @param contrasenaActual La contraseña actual del usuario.
     * @param nuevoCorreo El nuevo correo que se desea cambiar.
     * @param listener Listener para manejar respuestas exitosas.
     * @param errorListener Listener para manejar errores en la solicitud.
     */
    public static void cambiarCorreo(Context context, String contrasenaActual, String nuevoCorreo, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("userId", -1);

        if (idUsuario == -1) {
            Toast.makeText(context, "No se ha encontrado el ID del usuario.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, "Error al crear la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL del endpoint (ajusta el puerto si es necesario)
        String url = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=cambiar_correo";

        // Crear una solicitud POST con Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData, listener, errorListener);

        // Añadir la solicitud a la cola de Volley
        Volley.newRequestQueue(context).add(request);
    }

    /**
     * @brief Método para cambiar la contraseña del usuario.
     *
     * Este método envía una solicitud POST al servidor para actualizar la contraseña del usuario.
     * Obtiene el ID del usuario desde `SharedPreferences`, crea un objeto JSON con la nueva contraseña
     * y la contraseña actual. Realiza la solicitud y gestiona las respuestas a través de listeners.
     *
     * Diseño:
     *      contrasenaActual, nuevaContrasena (String) ---> [cambiarContrasena()] ---> Solicitud POST al servidor
     *
     * @param context El contexto de la aplicación.
     * @param contrasenaActual La contraseña actual del usuario.
     * @param nuevaContrasena La nueva contraseña que se desea establecer.
     * @param listener Listener para manejar respuestas exitosas.
     * @param errorListener Listener para manejar errores en la solicitud.
     */
    public static void cambiarContrasena(Context context, String contrasenaActual, String nuevaContrasena, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        // Obtener el ID del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int idUsuario = sharedPreferences.getInt("userId", -1);

        if (idUsuario == -1) {
            Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;  // Si no se encuentra el usuario, no hacer la solicitud
        }

        // Crear el objeto JSON con los datos
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("id", idUsuario);
            requestData.put("contrasena_actual", contrasenaActual);
            requestData.put("nueva_contrasena", nuevaContrasena);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al crear la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL del endpoint
        String url = "http://" + Globales.IP + ":8080/api/api_usuario.php?action=cambiar_contrasena";

        // Crear una solicitud POST con Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData, listener, errorListener);

        // Añadir la solicitud a la cola de Volley
        Volley.newRequestQueue(context).add(request);
    }
}