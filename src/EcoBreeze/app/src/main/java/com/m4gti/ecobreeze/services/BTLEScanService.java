package com.m4gti.ecobreeze.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.models.TramaIBeacon;
import com.m4gti.ecobreeze.utils.Utilidades;

import java.util.List;

public class BTLEScanService extends Service {

    private static final String ETIQUETA_LOG = "BTLE_SERVICE";
    private static final String CHANNEL_ID = "BTLELocationServiceChannel";
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private Handler handler = new Handler();
    private Runnable verificarBeacons = () -> {
        enviarNotificacion();
        stopSelf(); // Opcional: detener el servicio.
    };

    private void iniciarTemporizador() {
        handler.postDelayed(verificarBeacons, 30000); // 60 segundos.
    }

    private void detenerTemporizador() {
        handler.removeCallbacks(verificarBeacons);
    }

    private void enviarNotificacion() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Sin detección de beacons")
                .setContentText("No se detectaron beacons en 60 segundos.")
                .setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            crearCanalDeNotificacion();
        }

        notificationManager.notify(1, builder.build());
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ETIQUETA_LOG, "Servicio creado: inicializando Bluetooth.");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        inicializarBlueTooth();
        //inicializarLocalizacion();


    }

    private void crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Solo se necesita crear un canal en dispositivos con Android 8.0 o superior
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Beacon Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canal.setDescription("Notificaciones cuando no se detecten beacons.");

            // Obtener el servicio de notificaciones y registrar el canal
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(canal);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("MAC_ADDRESS")) {
            String direccionMac = intent.getStringExtra("MAC_ADDRESS");
            if (direccionMac != null && !direccionMac.isEmpty()) {
                Log.d(ETIQUETA_LOG, "Servicio iniciado: buscando dispositivo con dirección MAC: " + direccionMac);
                buscarEsteDispositivoBTLE(direccionMac);
                iniciarTemporizador(); // Inicia el temporizador aquí
            } else {
                Log.e(ETIQUETA_LOG, "No se proporcionó una dirección MAC válida en el Intent.");
                stopSelf(); // Detenemos el servicio si no hay una dirección MAC válida.
            }
        } else {
            Log.e(ETIQUETA_LOG, "El Intent no contiene la dirección MAC. Deteniendo el servicio.");
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ETIQUETA_LOG, "Servicio detenido: deteniendo escaneo BTLE.");
        detenerBusquedaDispositivosBTLE();
        //detenerActualizacionesDeLocalizacion();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No usaremos comunicación tipo "bind" para este servicio.
        return null;
    }

    private void inicializarBlueTooth() {
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        if (bta == null || !bta.isEnabled()) {
            Log.e(ETIQUETA_LOG, "Bluetooth no está habilitado o no es compatible.");
            stopSelf(); // Finalizamos el servicio si no se puede usar Bluetooth.
            return;
        }

        elEscanner = bta.getBluetoothLeScanner();
        if (elEscanner == null) {
            Log.e(ETIQUETA_LOG, "No se pudo obtener el escáner BTLE.");
            stopSelf();
        }
    }

    private void detenerBusquedaDispositivosBTLE() {
        if (callbackDelEscaneo != null) {
            elEscanner.stopScan(callbackDelEscaneo);
            callbackDelEscaneo = null;
            Log.d(ETIQUETA_LOG, "Escaneo BTLE detenido.");
        }
    }

    private void buscarEsteDispositivoBTLE(final String direccionMac) {
        Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE(): empieza");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE(): onScanResult()");

                // Comparar la dirección MAC del dispositivo encontrado
                if (resultado.getDevice().getAddress().equals(direccionMac)) {
                    // Mostrar la información del dispositivo como lo haces ahora
                    mostrarInformacionDispositivoBTLE(resultado);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE(): onBatchScanResults()");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE(): onScanFailed()");
            }
        };

        Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE(): comenzamos a escanear buscando: " + direccionMac);
        this.elEscanner.startScan(this.callbackDelEscaneo);
    }

    private void enviarDatosBroadcast(String mensaje) {
        Intent intent = new Intent("BTLEScanServiceUpdates");
        intent.putExtra("datos", mensaje);
        sendBroadcast(intent);
    }

    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        detenerTemporizador();
        iniciarTemporizador();

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        String nombreDispositivo = bluetoothDevice.getName() != null ? bluetoothDevice.getName() : "Nombre no disponible";
        String direccionMac = bluetoothDevice.getAddress();

        Log.d(ETIQUETA_LOG, "\n****************************************************");
        Log.d(ETIQUETA_LOG, "****** INFORMACIÓN DEL DISPOSITIVO BTLE DETECTADO ******");
        Log.d(ETIQUETA_LOG, "****************************************************");
        Log.d(ETIQUETA_LOG, "Nombre: " + nombreDispositivo);
        Log.d(ETIQUETA_LOG, "Dirección MAC: " + direccionMac);
        Log.d(ETIQUETA_LOG, "RSSI (Potencia de la señal): " + rssi);
        enviarDatosBroadcast("Dispositivo detectado: " + nombreDispositivo);

        if (bytes != null && bytes.length > 0) {
            Log.d(ETIQUETA_LOG, "Datos crudos del anuncio: " + Utilidades.bytesToHexString(bytes));
        } else {
            Log.d(ETIQUETA_LOG, "Datos crudos del anuncio: No disponibles");
        }

        try {
            TramaIBeacon tib = new TramaIBeacon(bytes);

            Log.d(ETIQUETA_LOG, "---------------- DETALLES DEL IBEACON ----------------");
            Log.d(ETIQUETA_LOG, "Prefijo: " + Utilidades.bytesToHexString(tib.getPrefijo()));
            Log.d(ETIQUETA_LOG, "AdvFlags: " + Utilidades.bytesToHexString(tib.getAdvFlags()));
            Log.d(ETIQUETA_LOG, "AdvHeader: " + Utilidades.bytesToHexString(tib.getAdvHeader()));
            Log.d(ETIQUETA_LOG, "Company ID: " + Utilidades.bytesToHexString(tib.getCompanyID()));
            Log.d(ETIQUETA_LOG, "iBeacon Type: " + Integer.toHexString(tib.getiBeaconType()));
            Log.d(ETIQUETA_LOG, "iBeacon Length: 0x" + Integer.toHexString(tib.getiBeaconLength()) + " (" + tib.getiBeaconLength() + ")");
            Log.d(ETIQUETA_LOG, "UUID: " + Utilidades.bytesToString(tib.getUUID()));
            Log.d(ETIQUETA_LOG, "Major: " + Utilidades.bytesToInt(tib.getMajor()) + " (Hex: " + Utilidades.bytesToHexString(tib.getMajor()) + ")");
            Log.d(ETIQUETA_LOG, "Minor: " + Utilidades.bytesToInt(tib.getMinor()) + " (Hex: " + Utilidades.bytesToHexString(tib.getMinor()) + ")");
            Log.d(ETIQUETA_LOG, "TxPower: " + tib.getTxPower() + " (Hex: " + Integer.toHexString(tib.getTxPower()) + ")");
            Log.d(ETIQUETA_LOG, "-----------------------------------------------------");
        } catch (Exception e) {
            Log.e(ETIQUETA_LOG, "Error al procesar los datos del iBeacon: " + e.getMessage());
        }
        //obtenerUbicacionActual(); // Obtener la ubicación actual

        Log.d(ETIQUETA_LOG, "****************************************************\n");
    }

    private void inicializarLocalizacion() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            private LocationResult locationResult;

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                this.locationResult = locationResult;
                if (locationResult == null) {
                    Log.d(ETIQUETA_LOG, "No location result");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(ETIQUETA_LOG, "Ubicación: " + location.getLatitude() + ", " + location.getLongitude());
                }
            }
        };

        iniciarActualizacionesDeLocalizacion();
    }

    private void iniciarActualizacionesDeLocalizacion() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(ETIQUETA_LOG, "Permisos de localización no concedidos.");
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void detenerActualizacionesDeLocalizacion() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
