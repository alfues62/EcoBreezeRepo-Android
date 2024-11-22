package com.m4gti.ecobreeze.ui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaEnvioDatos;
import com.m4gti.ecobreeze.services.BeaconService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;
    private LogicaEnvioDatos logicaEnvioDatos;
    private static final String ETIQUETA_LOG = "ScannerActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private ExecutorService cameraExecutor;
    private String qrText = "";
    private TextView qrTextView;
    private BluetoothAdapter bluetoothAdapter;
    private TextView tv;
    Button scanQrButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        tv = findViewById(R.id.minor);
        logicaEnvioDatos = new LogicaEnvioDatos(this);

        // Inicializar Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verificar si el dispositivo soporta Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no está disponible en este dispositivo", Toast.LENGTH_SHORT).show();
            finish(); // Cerrar la actividad
        }

        // Solicitar permisos necesarios
        solicitarPermisos();

        cameraExecutor = Executors.newSingleThreadExecutor();
        qrTextView = findViewById(R.id.qrTextView);  // Inicializamos el TextView

        // Inicializamos el botón y configuramos el listener
        scanQrButton = findViewById(R.id.scanQrButton);
        scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();  // Inicia el escaneo al hacer clic en el botón
            }
        });

        // Botón para iniciar búsqueda de un dispositivo específico
        Button botonBuscarDispositivo = findViewById(R.id.botonBuscarNuestroDispositivoBTLE);
        botonBuscarDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarBusquedaEste("00:11:22:33:44:55"); // Cambiar por la MAC real que quieras buscar
            }
        });

        // Botón para detener la búsqueda
        Button botonDetenerBusqueda = findViewById(R.id.botonDetenerBusquedaDispositivosBTLE);
        botonDetenerBusqueda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerBusqueda(); // Detiene la búsqueda de dispositivos
            }
        });
    }

    // --------------------------------------------------------------

    /**
     * @brief Método que inicia el servicio de busqueda de un dispositivo en especifico.
     *
     * |-----------------------------------------------------
     *  |         String (MAC) --->
     *  |                          iniciarBusquedaEste()
     *  |                      <---
     *  |-----------------------------------------------------
     * Parametros:
     *      @param direccionMac La dirección MAC del dispositivo a buscar.
     */
    // --------------------------------------------------------------
    private void iniciarBusquedaEste(String direccionMac) {
        Log.d(ETIQUETA_LOG, "Iniciando búsqueda de dispositivo BTLE con MAC: " + direccionMac);

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Iniciar el servicio para buscar un dispositivo específico por su dirección MAC
        Intent intent = new Intent(this, BeaconService.class);
        intent.setAction("buscar_este");
        intent.putExtra("direccionMac", direccionMac);
        startService(intent);
        tv.setText("Buscando dispositivo con MAC: " + direccionMac);
    }

    // --------------------------------------------------------------
    /**
     * @brief Método para detener la búsqueda de dispositivos BTLE
     *  |-----------------------------------------------------
     *  |                detenerBusqueda()
     *  |           <---
     *  |-----------------------------------------------------
     */
    // --------------------------------------------------------------
    private void detenerBusqueda() {
        Log.d(ETIQUETA_LOG, "Deteniendo búsqueda de dispositivos BTLE");

        // Detener el servicio de escaneo
        Intent intent = new Intent(this, BeaconService.class);
        stopService(intent);
        tv.setText("Búsqueda detenida");
    }

    // --------------------------------------------------------------
    /**
     * @brief Solicitar permisos necesarios (Bluetooth, Localización y Cámara)
     */
// --------------------------------------------------------------
    private void solicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Permisos para Android 12 y versiones superiores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA
                }, REQUEST_PERMISSIONS);
            }
        } else {
            // Permisos para versiones anteriores a Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA
                }, REQUEST_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean permissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false;
                    break;
                }
            }

            if (permissionsGranted) {
                Log.d(ETIQUETA_LOG, "Permisos concedidos");
            } else {
                Toast.makeText(this, "Se requieren todos los permisos para el escaneo y búsqueda de dispositivos", Toast.LENGTH_SHORT).show();
                finish(); // Cerrar la actividad si no se otorgan todos los permisos
            }
        }
    }

    // --------------------------------------------------------------
    /**
     * @brief Inicia la cámara para el escaneo de códigos QR.
     *
     * Este método configura la cámara y vincula la vista previa y el análisis de imágenes.
     */
    // --------------------------------------------------------------
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Vista previa
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(((PreviewView) findViewById(R.id.previewView)).getSurfaceProvider());

                // Analizador de imágenes
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> processImageProxy(imageProxy));

                // Selección de cámara trasera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unir el ciclo de vida de la cámara con la actividad
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this, cameraSelector, preview, imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    // --------------------------------------------------------------
    /**
     * @brief Detiene la cámara y desvincula todos los casos de uso.
     */
    // --------------------------------------------------------------
    private void detenerCamara() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll(); // Desvincula todos los casos de uso (vista previa, análisis, etc.)
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // --------------------------------------------------------------
    /**
     * @brief Metodo para detectar el código QR.
     *
     * Este método se encarga de analizar la imagen y detectar códigos QR. Si se encuentra un código
     * QR válido que contiene una dirección MAC, inicia la búsqueda del dispositivo correspondiente.
     *
     * Parametros:
     *      @param imageProxy El proxy de la imagen que se va a procesar.
     */
    // --------------------------------------------------------------
    private void processImageProxy(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() != null) {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            if (barcode.getValueType() == Barcode.TYPE_TEXT) {
                                qrText = barcode.getDisplayValue();
                                qrTextView.setText(qrText); // Muestra el texto QR en el TextView

                                // Verificar si el texto del QR contiene una dirección MAC
                                if (logicaEnvioDatos.esDireccionMacValida(qrText)) {

                                    //Busqueda del Dispositivo (ESTO A LA LARGA DEBE IR EN MAINACTIVITY)
                                    iniciarBusquedaEste(qrText);

                                    // Guardar la MAC en la base de datos
                                    logicaEnvioDatos.guardarMacEnBD(qrText);

                                    // Detener la cámara porque ya detectamos el QR
                                    detenerCamara();
                                } else {
                                    Log.d(ETIQUETA_LOG, "El texto escaneado no es una dirección MAC válida.");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace())
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }
    // --------------------------------------------------------------
    /**
     * @brief Método llamado cuando la actividad entra en pausa.
     *
     * Este método detiene la cámara para liberar recursos mientras
     * la actividad está en segundo plano.
     */
    // --------------------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
        detenerCamara(); // Detener la cámara al pausar la actividad
    }
    // --------------------------------------------------------------
    /**
     * @brief Método llamado cuando la actividad se reanuda.
     *
     * Este método reinicia la cámara para que el escaneo de códigos QR continúe
     * cuando la actividad vuelve a estar activa.
     */
    // --------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        startCamera(); // Reiniciar la cámara al reanudar la actividad
    }
    // --------------------------------------------------------------
    /**
     * @brief Método llamado cuando la actividad se destruye.
     *
     * Este método cierra el ejecutor de cámara para liberar recursos al cerrar la actividad.
     */
    // --------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        detenerBusqueda();
    }
} // class
