package com.m4gti.ecobreeze.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.m4gti.ecobreeze.R;

public class MapaGlobalFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view =  inflater.inflate(R.layout.fragment_mapaglobal, container, false);

        // Inicializa el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Agrega un marcador en una ubicación y mueve la cámara
        LatLng location = new LatLng(39.481106, -0.340987); // Cambia esto según tu necesidad
        mMap.addMarker(new MarkerOptions().position(location).title("Marker in UPV"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

}