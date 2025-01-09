package com.m4gti.ecobreeze.test;

import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GuardarMacEnBDTest {

    @Mock
    Context mockContext;

    @Mock
    SharedPreferences mockSharedPreferences;

    @Mock
    SharedPreferences.Editor mockEditor;

    @Mock
    RequestQueue mockRequestQueue;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula las preferencias compartidas
        when(mockContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.getInt("userId", -1)).thenReturn(123); // Simula un usuario válido
    }

    @Test
    public void testGuardarMacEnBD_Success() {
        // Dirección MAC simulada
        String mac = "01:23:45:67:89:AB";

        // Simula RequestQueue
        when(mockRequestQueue.add(any(JsonObjectRequest.class))).thenAnswer(invocation -> {
            JsonObjectRequest request = invocation.getArgument(0);

            // Simula la respuesta exitosa
            request.getListener().onResponse(new JSONObject().put("success", true));
            return null;
        });

        // Llama al método
        GuardarMacEnBDTestClass testClass = new GuardarMacEnBDTestClass(mockContext, mockRequestQueue);
        testClass.guardarMacEnBD(mac);

        // Verifica que RequestQueue recibió una solicitud
        verify(mockRequestQueue, times(1)).add(any(JsonObjectRequest.class));
    }

    @Test
    public void testGuardarMacEnBD_Error() {
        // Dirección MAC simulada
        String mac = "01:23:45:67:89:AB";

        // Simula RequestQueue
        when(mockRequestQueue.add(any(JsonObjectRequest.class))).thenAnswer(invocation -> {
            JsonObjectRequest request = invocation.getArgument(0);

            // Simula un error de red
            request.getErrorListener().onErrorResponse(new VolleyError("Network error"));
            return null;
        });

        // Llama al método
        GuardarMacEnBDTestClass testClass = new GuardarMacEnBDTestClass(mockContext, mockRequestQueue);
        testClass.guardarMacEnBD(mac);

        // Verifica que RequestQueue recibió una solicitud
        verify(mockRequestQueue, times(1)).add(any(JsonObjectRequest.class));
    }
}

// Clase auxiliar para testear
class GuardarMacEnBDTestClass {
    private final Context context;
    private final RequestQueue requestQueue;

    public GuardarMacEnBDTestClass(Context context, RequestQueue requestQueue) {
        this.context = context;
        this.requestQueue = requestQueue;
    }

    public void guardarMacEnBD(String mac) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            JSONObject jsonBody = new JSONObject();

            try {
                jsonBody.put("mac", mac);
                jsonBody.put("usuario_id", userId);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        "http://example.com", jsonBody,
                        response -> Toast.makeText(context, "Éxito", Toast.LENGTH_SHORT).show(),
                        error -> Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show());

                requestQueue.add(jsonObjectRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
