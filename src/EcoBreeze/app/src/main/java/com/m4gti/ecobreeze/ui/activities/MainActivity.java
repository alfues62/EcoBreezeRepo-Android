package com.m4gti.ecobreeze.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.ui.fragments.HomeFragment;
import com.m4gti.ecobreeze.ui.fragments.MapaGlobalFragment;
import com.m4gti.ecobreeze.ui.fragments.PerfilFragment;
import com.m4gti.ecobreeze.ui.fragments.QueRespirasFragment;

import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
