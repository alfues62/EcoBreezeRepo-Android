package com.m4gti.ecobreeze.ui.fragments;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;
import com.m4gti.ecobreeze.logic.LogicaRecepcionDatos;
import com.m4gti.ecobreeze.logic.NotificationHelper;
import com.m4gti.ecobreeze.models.Medicion;
import com.m4gti.ecobreeze.ui.activities.HuellaActivity;
import com.m4gti.ecobreeze.ui.activities.MainActivity;
import com.m4gti.ecobreeze.ui.activities.ScannerActivity;
import com.m4gti.ecobreeze.ui.activities.UserActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment implements LogicaRecepcionDatos.OnMedicionRecibidaListener{

    private Button logoutButton;
    private Button scannerButton;
    private Button huellaButton;
    private Button userButton;
    private Handler handler;
    private Runnable notificacionRunnable;
    private TextView textViewUltimaMedicion;
    private LogicaRecepcionDatos logicaRecepcionDatos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        logoutButton = view.findViewById(R.id.logoutButton);
        scannerButton = view.findViewById(R.id.scannerButton);
        huellaButton = view.findViewById(R.id.huellaButton);
        userButton = view.findViewById(R.id.userButton);

        textViewUltimaMedicion = view.findViewById(R.id.textViewUltimaMedicion);

        // Inicializamos LogicaRecepcionDatos con el listener
        if (getActivity() != null) {
            logicaRecepcionDatos = new LogicaRecepcionDatos(getActivity(), this);
        } // Le pasamos el listener a la clase

        // Obtener las mediciones
        logicaRecepcionDatos.obtenerMedicionesDeServidor();

        configurarBotones();

        // Inicializar el temporizador para notificaciones
        iniciarNotificaciones();

        return view;
    }

    private void iniciarNotificaciones() {
        handler = new Handler(Looper.getMainLooper());
        notificacionRunnable = new Runnable() {
            @Override
            public void run() {
                enviarNotificacionConCategoriaActual();
                handler.postDelayed(this, 30000); // 30 segundos
            }
        };
        handler.post(notificacionRunnable);
    }

    private void enviarNotificacionConCategoriaActual() {
        // Extraer el texto de la medición actual desde el TextView
        String medicionText = textViewUltimaMedicion.getText().toString();

        if (medicionText.contains("Categoría:")) {
            String categoria = medicionText.substring(medicionText.indexOf("Categoría:") + 10).trim();

            // Enviar notificación con la categoría
            NotificationHelper.sendSensorAlertNotification(
                    requireContext(),
                    "Categoría actual: " + categoria
            );
        }
    }



    // Implementamos el método de la interfaz
    @Override
    public void onMedicionRecibida(Medicion medicion) {
        // Mostrar la última medición incluyendo la categoría
        String medicionText = "ID: " + medicion.getIdMedicion() + "\n" +
                "Valor: " + medicion.getValor() + "\n" +
                "Fecha: " + medicion.getFecha() + "\n" +
                "Hora: " + medicion.getHora() + "\n" +
                "Categoría: " + medicion.getCategoria();  // Añadir categoría al texto

        textViewUltimaMedicion.setText(medicionText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacks(notificacionRunnable);
        }
    }

    private void configurarBotones() {
        // Botón para ir a UserActivity
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Usamos getActivity() para acceder a la actividad que contiene el fragmento
                Intent intent = new Intent(getActivity(), UserActivity.class);
                startActivity(intent); // Inicia la actividad UserActivity
            }
        });

        // Botón para ir a ScannerActivity
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivity(intent); // Inicia la actividad ScannerActivity
            }
        });

        // Botón para ir a HuellaActivity
        huellaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HuellaActivity.class);
                startActivity(intent); // Inicia la actividad HuellaActivity
            }
        });

        // Botón de logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llama al método de logout en LogicaLogin desde la actividad que contiene el fragmento
                LogicaLogin.logout(getActivity()); // Usamos getActivity() para acceder a la actividad y llamar al método logout
            }
        });

    }
}