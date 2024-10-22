package com.afusesc.appbioma;

import android.app.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class ServicioBeacons extends Service {
    private static final String ETIQUETA_LOG = "BTLEScanService";
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo;
    private BackendManager backendManager;
    private String MAC_OBJETIVO = "D1:04:CF:20:79:85";  // Dirección MAC del dispositivo objetivo (puedes personalizarla)
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ETIQUETA_LOG, "Servicio de escaneo BTLE creado");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        elEscanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ETIQUETA_LOG, "Servicio de escaneo BTLE iniciado");

        // Determina qué operación quieres hacer basándote en la intención que inicia el servicio
        String accion = intent.getAction();
        if ("buscar_todos".equals(accion)) {
            buscarTodosLosDispositivosBTLE();
        } else if ("buscar_este".equals(accion)) {
            String direccionMac = intent.getStringExtra("direccionMac");
            buscarEsteDispositivoBTLE(direccionMac);
        }

        return START_STICKY;  // El servicio se reinicia si el sistema lo mata
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detenerBusquedaDispositivosBTLE();
        Log.d(ETIQUETA_LOG, "Servicio de escaneo BTLE destruido");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // --------------------------------------------------------------
    /**
     * Método para buscar y escanear todos los dispositivos BTLE.
     */
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): empieza ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): onScanResult()");
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): onBatchScanResults()");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): onScanFailed()");
            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTLE(): empezamos a escanear ");
        this.elEscanner.startScan(this.callbackDelEscaneo);
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

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): onScanResult()");

                // Comparar la dirección MAC del dispositivo encontrado
                if (resultado.getDevice().getAddress().equals(direccionMac)) {
                    mostrarInformacionDispositivoBTLE(resultado);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): onBatchScanResults()");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): onScanFailed()");
            }
        };

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + direccionMac);
        this.elEscanner.startScan(this.callbackDelEscaneo);
    }

    // --------------------------------------------------------------
    /**
     * Detener la búsqueda de dispositivos BTLE
     */
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {
        if (this.callbackDelEscaneo == null) {
            return;
        }
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;
        Log.d(ETIQUETA_LOG, "detenerBusquedaDispositivosBTLE(): Escaneo detenido");
    }

    /**
     * Muestra la información de un dispositivo BTLE específico.
     *
     * @param resultado El resultado del escaneo que contiene el dispositivo BTLE.
     */
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        String direccionMAC = bluetoothDevice.getAddress();

        // Filtrar el dispositivo por la dirección MAC
        if (!direccionMAC.equals(MAC_OBJETIVO)) {
            return;
        }

        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ******************");
        Log.d(ETIQUETA_LOG, " ** DISPOSITIVO OBJETIVO DETECTADO BTLE ****** ");
        Log.d(ETIQUETA_LOG, " ******************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, " major = " + Utilidades.bytesToInt(tib.getMajor()));
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToInt(tib.getMinor()));

        if (Utilidades.bytesToInt(tib.getMajor()) == 1) {
            backendManager.enviarNumeroAlBackend(Utilidades.bytesToInt(tib.getMinor()));
        }
    }
}