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

public class LogicaUser {

    // Método para cambiar el correo (ya existente)
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

    // Nuevo método para cambiar la contraseña
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