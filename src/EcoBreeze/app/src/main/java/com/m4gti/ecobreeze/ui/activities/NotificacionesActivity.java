package com.m4gti.ecobreeze.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.ui.adapters.NotificacionesAdapter;
import com.m4gti.ecobreeze.logic.LogicaRecepcionNotif;
import com.m4gti.ecobreeze.models.Notificacion;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificacionesAdapter adapter;
    private List<Notificacion> notificacionesList = new ArrayList<>();
    private LogicaRecepcionNotif logicaRecepcionNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        recyclerView = findViewById(R.id.recyclerViewNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear instancia de LogicaRecepcionNotif
        logicaRecepcionNotif = new LogicaRecepcionNotif(this, new LogicaRecepcionNotif.OnNotificacionesRecibidasListener() {
            @Override
            public void onNotificacionesRecibidas(List<Notificacion> notificaciones) {
                // Aquí es donde recibimos la lista de notificaciones
                notificacionesList.clear();
                notificacionesList.addAll(notificaciones);
                // Notificar al adapter para que actualice la vista
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Manejar el error
                Log.e("NotificacionesActivity", "Error: " + errorMessage);
                Toast.makeText(NotificacionesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Llamar al método para obtener las notificaciones
        logicaRecepcionNotif.obtenerNotificacionesDeServidor();

        // Inicializar el adapter
        adapter = new NotificacionesAdapter(notificacionesList);
        recyclerView.setAdapter(adapter);
    }
}
