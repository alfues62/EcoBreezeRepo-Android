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

public class LogicaLogin {

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
                                Intent intent = new Intent(context, MainActivity.class);
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
