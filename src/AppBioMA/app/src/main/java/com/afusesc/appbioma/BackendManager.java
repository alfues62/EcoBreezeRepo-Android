package com.afusesc.appbioma;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

// --------------------------------------------------------------
/**
 * @brief Clase que gestiona la comunicación con el servidor backend.
 */
// --------------------------------------------------------------
public class BackendManager {
    private static final String ETIQUETA_LOG = ">>>>"; // Etiqueta para los logs
    private static final String URL_BASE = "http://192.168.151.180:8080"; // URL del servidor, ajustar según sea necesario
    private OkHttpClient client;

    /**
     * @brief Constructor de la clase BackendManager.
     *        Se inicializa el cliente OkHttp para las solicitudes al backend.
     */
    public BackendManager() {
        this.client = new OkHttpClient();
    }

    // --------------------------------------------------------------
    /**
     *  @brief Método encargado para enviar la información al backend.
     *
     *  |-----------------------------------------------------
     *  | int (numero) --->
     *  |                   enviarNumeroAlBackend()
     *  |              <---
     *  |-----------------------------------------------------
     *
     *  Parametros:
     *    @param numero El numero a enviar a la base de datos.
     */
    // --------------------------------------------------------------
    public void enviarNumeroAlBackend(int numero) {
        // Construye la solicitud POST con el número como parámetro
        RequestBody formBody = new FormBody.Builder()
                .add("numero", String.valueOf(numero))
                .build();

        // Configura la solicitud con la URL base y el cuerpo de la petición
        Request request = new Request.Builder()
                .url(URL_BASE)
                .post(formBody)
                .build();

        // Ejecuta la solicitud de forma asíncrona
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Log de error si la solicitud falla
                Log.e(ETIQUETA_LOG, "Error al enviar el número al backend", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Verifica si la respuesta es satisfactoria
                if (!response.isSuccessful()) {
                    Log.e(ETIQUETA_LOG, "Error en la respuesta del servidor: " + response);
                } else {
                    // Log de éxito y respuesta del servidor
                    Log.d(ETIQUETA_LOG, "Número enviado con éxito al backend");
                    String responseData = response.body().string();
                    Log.d(ETIQUETA_LOG, "Respuesta del servidor: " + responseData);
                }
            }
        });
    }
}
