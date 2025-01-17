package com.m4gti.ecobreeze.models;

import java.util.Arrays;

/**
 * @class TramaIBeacon
 * @brief Procesa la informacion de la trama del iBeacon.
 *
 * Esta clase maneja y procesa los datos de una trama iBeacon, incluyendo su prefijo, UUID,
 * major, minor, y otros atributos necesarios para identificar y trabajar con dispositivos iBeacon.
 */
public class TramaIBeacon {
    private byte[] prefijo = null; // 9 bytes
    private byte[] uuid = null; // 16 bytes
    private byte[] major = null; // 2 bytes
    private byte[] minor = null; // 2 bytes
    private byte txPower = 0; // 1 byte
    private byte[] losBytes;
    private byte[] advFlags = null; // 3 bytes
    private byte[] advHeader = null; // 2 bytes
    private byte[] companyID = new byte[2]; // 2 bytes
    private byte iBeaconType = 0 ; // 1 byte
    private byte iBeaconLength = 0 ; // 1 byte

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el prefijo de la trama iBeacon.
     *
     * @return El prefijo como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getPrefijo() {
        return prefijo;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el UUID de la trama iBeacon.
     *
     * @return El UUID como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getUUID() {
        return uuid;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el mayor de la trama iBeacon.
     *
     * @return El mayor como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getMajor() {
        return major;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el minor de la trama iBeacon.
     *
     * @return El minor como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getMinor() {
        return minor;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el valor de txPower de la trama iBeacon.
     *
     * @return El txPower como un byte.
     */
    // -------------------------------------------------------------------------------
    public byte getTxPower() {
        return txPower;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene los bytes de la trama iBeacon.
     *
     * @return Los bytes como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getLosBytes() {
        return losBytes;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene las flags de la trama iBeacon.
     *
     * @return Las flags como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getAdvFlags() {
        return advFlags;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el encabezado de advertencia de la trama iBeacon.
     *
     * @return El encabezado de advertencia como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getAdvHeader() {
        return advHeader;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el ID de la compañía de la trama iBeacon.
     *
     * @return El ID de la compañía como un arreglo de bytes.
     */
    // -------------------------------------------------------------------------------
    public byte[] getCompanyID() {
        return companyID;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene el tipo de iBeacon.
     *
     * @return El tipo de iBeacon como un byte.
     */
    // -------------------------------------------------------------------------------
    public byte getiBeaconType() {
        return iBeaconType;
    }

    // -------------------------------------------------------------------------------
    /**
     * Obtiene la longitud del iBeacon.
     *
     * @return La longitud del iBeacon como un byte.
     */
    // -------------------------------------------------------------------------------
    public byte getiBeaconLength() {
        return iBeaconLength;
    }

    // -------------------------------------------------------------------------------
    /**
     * Constructor que inicializa la trama iBeacon a partir de un arreglo de bytes.
     *
     * @param bytes El arreglo de bytes que contiene la trama completa del iBeacon.
     */
    // -------------------------------------------------------------------------------
    public TramaIBeacon(byte[] bytes ) {
        this.losBytes = bytes;

        prefijo = Arrays.copyOfRange(losBytes, 0, 8+1 ); // 9 bytes
        uuid = Arrays.copyOfRange(losBytes, 9, 24+1 ); // 16 bytes
        major = Arrays.copyOfRange(losBytes, 25, 26+1 ); // 2 bytes
        minor = Arrays.copyOfRange(losBytes, 27, 28+1 ); // 2 bytes
        txPower = losBytes[ 29 ]; // 1 byte

        advFlags = Arrays.copyOfRange( prefijo, 0, 2+1 ); // 3 bytes
        advHeader = Arrays.copyOfRange( prefijo, 3, 4+1 ); // 2 bytes
        companyID = Arrays.copyOfRange( prefijo, 5, 6+1 ); // 2 bytes
        iBeaconType = prefijo[ 7 ]; // 1 byte
        iBeaconLength = prefijo[ 8 ]; // 1 byte

    } // ()
} // class