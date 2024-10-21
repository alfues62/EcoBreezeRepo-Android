package com.afusesc.appbioma;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
 * Actividad principal que maneja la interacción con dispositivos Bluetooth LE (BTLE)
 * y se comunica con el backend para enviar información.
 */
// ------------------------------------------------------------------
public class MainActivity extends AppCompatActivity {
    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;
    private static final String MAC_OBJETIVO = "D1:04:CF:20:79:85"; // Dirección MAC del dispositivo objetivo
    private BackendManager backendManager;
    TextView tv;
    private ExecutorService cameraExecutor;
    private String qrText = ""; // Variable donde almacenaremos el texto del QR
    private TextView qrTextView; // TextView para mostrar el texto escaneado

    // --------------------------------------------------------------
    /**
     * Método que se ejecuta al crear la actividad.
     *
     * @param savedInstanceState Estado guardado de la instancia.
     */
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        tv = findViewById(R.id.minor);

        inicializarBlueTooth();

        backendManager = new BackendManager();

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

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");
    }

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

    private boolean esDireccionMacValida(String mac) {
        // Expresión regular para verificar el formato de una dirección MAC
        return mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

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
                                    buscarEsteDispositivoBTLE(qrText);

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

    // --------------------------------------------------------------
    /**
     * Método para buscar y escanear todos los dispositivos BTLE.
     */
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    /**
     * Muestra la información de un dispositivo BTLE específico.
     *
     * @param resultado El resultado del escaneo que contiene el dispositivo BTLE.
     */
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        String direccionMAC = bluetoothDevice.getAddress(); // Obtener la dirección MAC del dispositivo

        // Filtrar el dispositivo por la dirección MAC
        if (!direccionMAC.equals(MAC_OBJETIVO)) {
            // Si no coincide la dirección MAC, no hacemos nada
            return;
        }

        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ******************");
        Log.d(ETIQUETA_LOG, " ** DISPOSITIVO OBJETIVO DETECTADO BTLE ****** ");
        Log.d(ETIQUETA_LOG, " ******************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ******************");

        // Enviar el valor del minor al backend solo si el major es igual a 1
        if(Utilidades.bytesToInt(tib.getMajor()) == 1){
            // Aquí es donde llamas al BackendManager o la lógica fake para enviar el minor al servidor
            backendManager.enviarNumeroAlBackend(Utilidades.bytesToInt(tib.getMinor()));

        }
        // Convertir el valor minor a String y actualizar el TextView
        String minorValue = String.valueOf(Utilidades.bytesToInt(tib.getMinor()));
        tv.setText(minorValue);  // Asegúrate de que tv no sea null
    }

    // --------------------------------------------------------------
    /**
     * Método para buscar un dispositivo BTLE específico por su dirección MAC.
     *
     * @param direccionMac La dirección MAC del dispositivo a buscar.
     */
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String direccionMac) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                // Comparar la dirección MAC del dispositivo encontrado
                if (resultado.getDevice().getAddress().equals(direccionMac)) {
                    mostrarInformacionDispositivoBTLE(resultado);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");
            }
        };

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + direccionMac);
        this.elEscanner.startScan(this.callbackDelEscaneo);
    }

    // --------------------------------------------------------------
    /**
     * Método detener la busqueda de dispositivos BTLE
     */
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        this.elEscanner.stopScan( this.callbackDelEscaneo );
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    /**
     * Botones que llaman a los métodos anteriores
     */
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    } // ()
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE( "EPSG-GTI-PROY-3A" );
        this.buscarEsteDispositivoBTLE( "D1:04:CF:20:79:85" );

    } // ()
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    /**
     * Inicializa el adaptador Bluetooth y verifica los permisos necesarios.
     */
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()

} // class