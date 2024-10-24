package com.afusesc.appbioma;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
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
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ------------------------------------------------------------------
/**
 * @brief Actividad principal de la app, escanea un QR para empezar el escaneo y envio de datos
 */
// ------------------------------------------------------------------
public class MainActivity extends AppCompatActivity {
    private static final String ETIQUETA_LOG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private ExecutorService cameraExecutor;
    private String qrText = "";
    private TextView qrTextView;
    private BluetoothAdapter bluetoothAdapter;
    private TextView tv;
    private Button /*btnBuscarTodos, */ btnBuscarEste, btnDetener;

    // --------------------------------------------------------------
    /**
     * @brief Método que se llama cuando la actividad se crea.
     *
     * Inicializa los componentes de la interfaz, verifica si el dispositivo soporta Bluetooth, solicita los permisos necesarios
     * y configura los botones para buscar dispositivos Bluetooth LE y escanear un código QR.
     *
     * Parametros:
     *      @param savedInstanceState Estado previamente guardado de la actividad, si está disponible.
     */
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar componentes de UI
        tv = findViewById(R.id.minor);

            /*btnBuscarTodos = findViewById(R.id.botonBuscarDispositivosBTLE);*/
        btnBuscarEste = findViewById(R.id.botonBuscarNuestroDispositivoBTLE);
        btnDetener = findViewById(R.id.botonDetenerBusquedaDispositivosBTLE);

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
        Button scanQrButton = findViewById(R.id.scanQrButton);
        scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();  // Inicia el escaneo al hacer clic en el botón
            }
        });
        configurarBotones();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");
    }

    // --------------------------------------------------------------
    /**
     * @brief Se enlazan los botones a sus funciones.
     *
     * |-----------------------------------------------------
     *  |     configurarBotones()
     *  | <---
     *  |-----------------------------------------------------
     */
    // --------------------------------------------------------------
    private void configurarBotones() {
        // Botón para buscar un dispositivo específico por su dirección MAC
        btnBuscarEste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarBusquedaEste(""); // Reemplaza con la MAC específica
            }
        });

        // Botón para detener la búsqueda
        btnDetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerBusqueda();
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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Iniciar el servicio para buscar un dispositivo específico por su dirección MAC
        Intent intent = new Intent(this, ServicioBeacons.class);
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
        Intent intent = new Intent(this, ServicioBeacons.class);
        stopService(intent);
        tv.setText("Búsqueda detenida");
    }

    // --------------------------------------------------------------
    /**
     * @brief Solicitar permisos necesarios (Bluetooth y Localización para escanear dispositivos BTLE)
     */
    // --------------------------------------------------------------
    private void solicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Permisos necesarios a partir de Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_LOCATION_PERMISSION);
            }
        } else {
            // Permiso de localización para versiones anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(ETIQUETA_LOG, "Permisos de ubicación concedidos");
            } else {
                Toast.makeText(this, "Permisos de ubicación necesarios para el escaneo de BTLE", Toast.LENGTH_SHORT).show();
                finish(); // Cerrar la actividad si no hay permisos
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
     * @brief Verifica si una dirección MAC es válida.
     *
     * |--------------------------------------
     * |
     * | String (MAC) --->
     * |                esDireccionMacValida()
     * | Bool (T/F) <---
     * |
     * |--------------------------------------
     * Parametros:
     *      @param mac La dirección MAC que se va a verificar.
     *
     *  @return true si el formato es válido, false en caso contrario.
     */
    // --------------------------------------------------------------
    private boolean esDireccionMacValida(String mac) {
        // Expresión regular para verificar el formato de una dirección MAC
        return mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
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
                                if (esDireccionMacValida(qrText)) {
                                    // Iniciar búsqueda del dispositivo Bluetooth LE con la MAC del QR
                                    iniciarBusquedaEste(qrText);
                                    //buscarEsteDispositivoBTLE(qrText);

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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

} // class