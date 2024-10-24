package com.afusesc.appbioma;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;

// ------------------------------------------------------------------
/**
 * @brief Servicio para la deteccion de beacons.
 */
// ------------------------------------------------------------------
public class ServicioBeacons extends Service {
    private static final String ETIQUETA_LOG = "BTLEScanService";
    private static final String CHANNEL_ID = "BeaconServiceChannel";
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo;
    private BackendManager backendManager;
    private String MAC_OBJETIVO = "D1:04:CF:20:79:85";  // Dirección MAC del dispositivo objetivo (puedes personalizarla)
    private BluetoothAdapter bluetoothAdapter;

    // --------------------------------------------------------------
    /**
     *  @brief OnCreate del servicio de escaneo de beacons.
     *
     *  Se encarga de inicializar el adaptador de bluetooth, el SannerLE
     */
    // --------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ETIQUETA_LOG, "Servicio de escaneo BTLE creado");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        elEscanner = bluetoothAdapter.getBluetoothLeScanner();

        // Crear el canal de notificaciones si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal del Servicio de Beacons",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    // --------------------------------------------------------------
    /**
     * @brief Método que se ejecuta cuando el servicio se inicia.
     *
     * Este método maneja la lógica para iniciar el servicio.
     *
     * Parametros:
     *      @param intent Intent que contiene la acción y datos adicionales que inicia el servicio.
     *      @param flags flag de control para el servicio.
     *      @param startId ID único para el inicio del servicio.
     *
     * @return Un valor que indica cómo debe comportarse el servicio.
     */
// --------------------------------------------------------------
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ETIQUETA_LOG, "Servicio de escaneo BTLE iniciado");

        // Verificar si la acción es para detener el servicio
        if (intent != null && "DETENER_SERVICIO".equals(intent.getAction())) {
            Log.d(ETIQUETA_LOG, "Deteniendo el servicio a petición del usuario");
            stopSelf();  // Detener el servicio
            return START_NOT_STICKY;  // No reiniciar el servicio
        }

        // Crear la notificación para el servicio en primer plano (Foreground Service)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Acción para detener el servicio desde la notificación
        Intent stopIntent = new Intent(this, ServicioBeacons.class);
        stopIntent.setAction("DETENER_SERVICIO");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Servicio de Beacons Activo")
                .setContentText("Escaneando dispositivos cercanos.")
                .setSmallIcon(R.drawable.ic_launcher_background)  // Asegúrate de tener un icono aquí
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_background, "Detener", stopPendingIntent) // Botón para detener
                .build();

        startForeground(1, notification);

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
     *  @brief Método encargado de buscar entre todos los dispositivos BLE que podemos detectar.
     *
     *  |-----------------------------------------------------
     *  |          mostrarInformacionDispositivoBTLE()
     *  |     <---
     *  |-----------------------------------------------------
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
     *  @brief Método encargado de buscar cierto dispositivo dada su dirección MAC.
     *
     *  |-----------------------------------------------------
     *  |         String (MAC) --->
     *  |                          buscarEsteDispositivoBLE()
     *  |                      <---
     *  |-----------------------------------------------------
     *
     *  Parametros:
     *    @param direccionMac La dirección MAC del dispositivo a buscar.
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
     *  @brief Método encargado de detener el escaneo de dispositivos.
     *
     *  |-----------------------------------------------------
     *  |                detenerBusquedaDispositivosBLE()
     *  |           <---
     *  |-----------------------------------------------------
     *
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

    // --------------------------------------------------------------
    /**
     *  @brief Método encargado de mostrar la información de los dispositivos detectados por el escaneo.
     *         Se extrae la información relevante del mensaje y luego se llama al envio de información al backend.
     *
     *  |-----------------------------------------------------
     *  | ScanResult (resultado) --->
     *  |                           mostrarInformacionDispositivoBTLE()
     *  |                       <---
     *  |-----------------------------------------------------
     *
     *  Parametros:
     *    @param resultado El resultado del escaneo Bluetooth LE que contiene información del dispositivo detectado.
     */
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        String direccionMAC = bluetoothDevice.getAddress();

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
