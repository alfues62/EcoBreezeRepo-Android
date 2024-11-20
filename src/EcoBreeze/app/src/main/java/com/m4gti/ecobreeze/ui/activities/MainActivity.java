package com.m4gti.ecobreeze.ui.activities;

import com.m4gti.ecobreeze.logic.LogicaRecepcionDatos;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaLogin;
import com.m4gti.ecobreeze.models.Medicion;

public class MainActivity extends AppCompatActivity implements LogicaRecepcionDatos.OnMedicionRecibidaListener {

    private Button logoutButton;
    private Button scannerButton;
    private Button huellaButton;
    private Button userButton;

    private TextView textViewUltimaMedicion;
    private LogicaRecepcionDatos logicaRecepcionDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoutButton = findViewById(R.id.logoutButton);
        scannerButton = findViewById(R.id.scannerButton);
        huellaButton = findViewById(R.id.huellaButton);
        userButton = findViewById(R.id.userButton);

        textViewUltimaMedicion = findViewById(R.id.textViewUltimaMedicion);

        // Inicializamos LogicaRecepcionDatos con el listener
        logicaRecepcionDatos = new LogicaRecepcionDatos(this, this);  // Le pasamos el listener a la clase

        // Obtener las mediciones
        logicaRecepcionDatos.obtenerMedicionesDeServidor();

        configurarBotones();
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

    private void configurarBotones() {
        // Botón de logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogicaLogin.logout(MainActivity.this); // Llama al método de logout en LogicaLogin
            }
        });

        // Botón para ir a ScannerActivity
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(intent); // Inicia la actividad ScannerActivity
            }
        });

        // Botón para ir a HuellaActivity
        huellaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HuellaActivity.class);
                startActivity(intent); // Inicia la actividad HuellaActivity
            }
        });
        // Botón para ir a UserActivity
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent); // Inicia la actividad UserActivity
            }
        });
    }
}
