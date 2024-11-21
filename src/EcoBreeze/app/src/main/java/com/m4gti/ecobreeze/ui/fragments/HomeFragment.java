package com.m4gti.ecobreeze.ui.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.m4gti.ecobreeze.ui.activities.VerMas;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializa el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configura el TextView y su OnClickListener
        TextView textLink = view.findViewById(R.id.text_link);

        // Subrayar el texto programáticamente
        textLink.setPaintFlags(textLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        textLink.setOnClickListener(v -> {
            // Inicia la actividad VerMas
            Intent intent = new Intent(getActivity(), VerMas.class);
            startActivity(intent);
        });

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
