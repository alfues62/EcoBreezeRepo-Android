package com.m4gti.ecobreeze.ui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.logic.LogicaEnvioDatos;
import com.m4gti.ecobreeze.services.BTLEScanService;

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
    }

    // --------------------------------------------------------------

    /*public void iniciarServicio(View v) {
        Intent intent = new Intent(this, BTLEScanService.class);
        intent.putExtra("MAC_ADDRESS", DIRECCION_MAC); // Reemplaza con la dirección MAC deseada
        startService(intent);
    }*/

    public void detenerServicio() {
        Log.d(ETIQUETA_LOG, "Deteniendo servicio BTLE");
        Intent intent = new Intent(this, BTLEScanService.class);
        stopService(intent);
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

                                    // Log para verificar si estamos entrando en la sección del código
                                    Log.d(ETIQUETA_LOG, "Dirección MAC válida encontrada: " + qrText);

                                    // Iniciar el servicio BTLEScanService con la dirección MAC
                                    Intent serviceIntent = new Intent(this, BTLEScanService.class);
                                    serviceIntent.putExtra("MAC_ADDRESS", qrText); // Pasa la dirección MAC al servicio
                                    startService(serviceIntent);  // Inicia el servicio en segundo plano
                                    Log.d(ETIQUETA_LOG, "Servicio iniciado");

                                    // Guardar la MAC en la base de datos
                                    logicaEnvioDatos.guardarMacEnBD(qrText);

                                    // Detener la cámara porque ya detectamos el QR
                                    detenerCamara();

                                    // Iniciar MainActivity de forma independiente
                                    Intent mainActivityIntent = new Intent(this, MainActivity.class);

                                    // Usar log para confirmar que estamos intentando iniciar MainActivity
                                    Log.d(ETIQUETA_LOG, "Iniciando MainActivity");

                                    startActivity(mainActivityIntent);
                                    finish();  // Opcionalmente, si deseas cerrar ScannerActivity al iniciar MainActivity
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
        //detenerServicio();
    }
} // class
