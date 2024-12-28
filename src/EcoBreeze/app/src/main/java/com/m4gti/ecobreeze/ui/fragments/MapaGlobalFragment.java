package com.m4gti.ecobreeze.ui.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.android.gms.maps.model.TileOverlay;

import com.google.maps.android.heatmaps.WeightedLatLng;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaRecepcionDatos;
import com.m4gti.ecobreeze.models.Medicion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapaGlobalFragment extends Fragment implements OnMapReadyCallback, LogicaRecepcionDatos.OnMedicionRecibidaListener {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private LogicaRecepcionDatos logicaRecepcionDatos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_mapaglobal, container, false);

        // Inicializa el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicializar lógica para recibir datos
        logicaRecepcionDatos = new LogicaRecepcionDatos(getContext(), this);
        logicaRecepcionDatos.obtenerMedicionesDeServidor();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Mover la cámara a una ubicación inicial
        LatLng location = new LatLng(39.481106, -0.340987); // Cambia esto según tu necesidad
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
    }

    @Override
    public void onMedicionRecibida(Medicion medicion) {
        // Mostrar la última medición (opcional, si lo necesitas)
        Log.d("MapaGlobalFragment", "Última medición: " + medicion.getValor() + " en " + medicion.getFecha());

        // Ahora obtenemos todas las mediciones y las filtramos por la fecha actual
        List<Medicion> medicionesFiltradas = filtrarMedicionesPorFecha(logicaRecepcionDatos.getMediciones());

        // Mostrar las mediciones filtradas en el mapa de calor
        actualizarMapaConMediciones(medicionesFiltradas);
    }

    private List<Medicion> filtrarMedicionesPorFecha(List<Medicion> mediciones) {
        List<Medicion> medicionesFiltradas = new ArrayList<>();
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (Medicion medicion : mediciones) {
            if (medicion.getFecha().equals(fechaActual)) {
                medicionesFiltradas.add(medicion);
            }
        }
        return medicionesFiltradas;
    }

    private void actualizarMapaConMediciones(List<Medicion> medicionesFiltradas) {
        List<WeightedLatLng> puntos = new ArrayList<>();  // Lista de puntos con intensidades

        // Recorremos las mediciones filtradas para asignar puntos e intensidades
        for (Medicion medicion : medicionesFiltradas) {
            LatLng latLng = new LatLng(medicion.getLat(), medicion.getLon());  // Creamos el punto con las coordenadas
            float intensidad = obtenerIntensidadPorCategoria(medicion.getCategoria());  // Calculamos la intensidad según la categoría

            // Añadimos el punto con la intensidad al mapa
            WeightedLatLng puntoConIntensidad = new WeightedLatLng(latLng, intensidad);
            puntos.add(puntoConIntensidad);  // Añadimos el punto con la intensidad
        }

        // Si hay puntos, actualizamos el mapa de calor
        if (!puntos.isEmpty()) {
            mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(puntos)  // Usamos los puntos con intensidades asignadas
                    .build();

            if (mOverlay != null) {
                mOverlay.remove();  // Eliminamos el overlay anterior si existe
            }

            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));  // Añadimos el nuevo mapa de calor
        } else {
            Log.d("MapaGlobalFragment", "No hay mediciones para la fecha actual.");
        }
    }

    // Método para calcular la intensidad según la categoría
    private float obtenerIntensidadPorCategoria(String categoria) {
        if (categoria == null) return 0.0f;  // Si no hay categoría, asignamos la intensidad mínima

        switch (categoria) {
            case "Bajo":
                return 1.0f;  // Baja intensidad
            case "Normal":
                return 2.0f;  // Intensidad media
            case "Alto":
                return 3.0f;  // Alta intensidad
            default:
                return 0.0f;  // Intensidad mínima en caso de categoría desconocida
        }
    }
}
