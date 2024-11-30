package com.m4gti.ecobreeze.ui.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.NotificationHelper;
import com.m4gti.ecobreeze.ui.fragments.HomeFragment;
import com.m4gti.ecobreeze.ui.fragments.MapaGlobalFragment;
import com.m4gti.ecobreeze.ui.fragments.PerfilFragment;
import com.m4gti.ecobreeze.ui.fragments.QueRespirasFragment;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String CHANNEL_ID = "sensor_notifications";
    private static final int CODIGO_PETICION_PERMISOS = 100; // Puedes usar cualquier valor entero.
    private TextView tv;
    // Declaración de requestPermissionLauncher fuera del método
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permiso concedido
                    NotificationHelper.sendSensorAlertNotification(this, "Permiso concedido, notificaciones activadas.");
                } else {
                    // Permiso denegado
                    Toast.makeText(this, "Permiso denegado para mostrar notificaciones.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear un canal de notificación (requerido en Android 8.0 y superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sensor Notifications";
            String description = "Notificaciones del estado del sensor";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Registrar el canal en el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //comprobarEstadoSensor();
                handler.postDelayed(this, 60000); // Verifica cada 5 segundos
            }
        };
        handler.post(runnable);


        // Crear el canal de notificaciones
        NotificationHelper.createNotificationChannel(this);

        // Verifica y solicita el permiso si es necesario (solo Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Configura BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.navigation_mapaGlobal) {
                    selectedFragment = new MapaGlobalFragment();
                } else if (item.getItemId() == R.id.navigation_queRespiras) {
                    selectedFragment = new QueRespirasFragment();
                } else if (item.getItemId() == R.id.navigation_perfil) {
                    selectedFragment = new PerfilFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }

        });

        // Configura el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Selección predeterminada en la navegación
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Configura el mapa aquí
        LatLng UPV = new LatLng(39.481106, -0.340987); // Coordenadas de la UPV
        googleMap.addMarker(new MarkerOptions().position(UPV).title("Marker UPV"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(UPV));
    }

    // Método para mostrar la notificación
    private void mostrarNotificacion(String mensaje) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear la notificación
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Estado del Sensor")
                .setContentText(mensaje)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build();

        // Mostrar la notificación
        notificationManager.notify(1, notification);
    }

    // Simulación de la comprobación de estado del sensor
    private void comprobarEstadoSensor() {
        // Lógica para verificar si el sensor está apagado o desconectado
        boolean sensorDesconectado = true; // Cambiar por la lógica real

        if (sensorDesconectado) {
            //mostrarNotificacion("El sensor está apagado o desconectado");
        }
    }
}
