package com.afusesc.appbioma;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

// -----------------------------------------------------------------------------------
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class Utilidades {

    /**
     * Convierte una cadena de texto en un array de bytes.
     *
     * @param texto La cadena de texto que se desea convertir.
     * @return Un array de bytes que representa el texto en formato binario.
     */
    public static byte[] stringToBytes ( String texto ) {
        return texto.getBytes();
        // byte[] b = string.getBytes(StandardCharsets.UTF_8); // Ja
    } // ()

    /**
     * Convierte una cadena de texto de 16 caracteres en un UUID.
     * La cadena debe tener exactamente 16 caracteres.
     *
     * @param uuid La cadena que se desea convertir en UUID.
     * @return El UUID correspondiente a la cadena proporcionada.
     * @throws Error Si la cadena no tiene exactamente 16 caracteres.
     */
    public static UUID stringToUUID( String uuid ) {
        if ( uuid.length() != 16 ) {
            throw new Error( "stringUUID: string no tiene 16 caracteres ");
        }
        byte[] comoBytes = uuid.getBytes();

        String masSignificativo = uuid.substring(0, 8);
        String menosSignificativo = uuid.substring(8, 16);
        UUID res = new UUID( Utilidades.bytesToLong( masSignificativo.getBytes() ), Utilidades.bytesToLong( menosSignificativo.getBytes() ) );

        // Log.d( MainActivity.ETIQUETA_LOG, " \n\n***** stringToUUID *** " + uuid  + "=?=" + Utilidades.uuidToString( res ) );

        // UUID res = UUID.nameUUIDFromBytes( comoBytes ); no va como quiero

        return res;
    } // ()

    /**
     * Convierte un UUID en su representación como cadena de texto (String).
     *
     * @param uuid El UUID que se desea convertir.
     * @return La cadena de texto que representa el UUID.
     */
    public static String uuidToString ( UUID uuid ) {
        return bytesToString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    /**
     * Convierte un UUID en su representación hexadecimal.
     *
     * @param uuid El UUID que se desea convertir.
     * @return Una cadena de texto que representa el UUID en formato hexadecimal.
     */
    public static String uuidToHexString ( UUID uuid ) {
        return bytesToHexString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    /**
     * Convierte un array de bytes en una cadena de texto (String).
     * Cada byte se interpreta como un carácter ASCII.
     *
     * @param bytes El array de bytes a convertir.
     * @return La cadena de texto resultante, o una cadena vacía si el array es nulo.
     */
    public static String bytesToString( byte[] bytes ) {
        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append( (char) b );
        }
        return sb.toString();
    }

    /**
     * Convierte dos valores `long` (bits más y menos significativos de un UUID) en un array de bytes.
     *
     * @param masSignificativos La parte de bits más significativa.
     * @param menosSignificativos La parte de bits menos significativa.
     * @return Un array de bytes que representa los dos valores `long`.
     */
    public static byte[] dosLongToBytes( long masSignificativos, long menosSignificativos ) {
        ByteBuffer buffer = ByteBuffer.allocate( 2 * Long.BYTES );
        buffer.putLong( masSignificativos );
        buffer.putLong( menosSignificativos );
        return buffer.array();
    }

    /**
     * Convierte un array de bytes en un valor entero (`int`).
     *
     * @param bytes El array de bytes a convertir.
     * @return El valor entero resultante.
     */
    public static int bytesToInt( byte[] bytes ) {
        return new BigInteger(bytes).intValue();
    }

    /**
     * Convierte un array de bytes en un valor largo (`long`).
     *
     * @param bytes El array de bytes a convertir.
     * @return El valor `long` resultante.
     */
    public static long bytesToLong( byte[] bytes ) {
        return new BigInteger(bytes).longValue();
    }

    /**
     * Convierte un array de bytes en un valor entero (`int`).
     * Este método se asegura de que el array de bytes tenga menos de 4 bytes
     * y maneja correctamente valores con signo.
     *
     * @param bytes El array de bytes a convertir.
     * @return El valor entero resultante.
     * @throws Error Si el array tiene más de 4 bytes.
     */
    public static int bytesToIntOK( byte[] bytes ) {
        if (bytes == null ) {
            return 0;
        }

        if ( bytes.length > 4 ) {
            throw new Error( "demasiados bytes para pasar a int ");
        }
        int res = 0;



        for( byte b : bytes ) {
           /*
           Log.d( MainActivity.ETIQUETA_LOG, "bytesToInt(): byte: hex=" + Integer.toHexString( b )
                   + " dec=" + b + " bin=" + Integer.toBinaryString( b ) +
                   " hex=" + Byte.toString( b )
           );
           */
            res =  (res << 8) // * 16
                    + (b & 0xFF); // para quedarse con 1 byte (2 cuartetos) de lo que haya en b
        } // for

        if ( (bytes[ 0 ] & 0x8) != 0 ) {
            // si tiene signo negativo (un 1 a la izquierda del primer byte
            res = -(~(byte)res)-1; // complemento a 2 (~) de res pero como byte, -1
        }
       /*
        Log.d( MainActivity.ETIQUETA_LOG, "bytesToInt(): res = " + res + " ~res=" + (res ^ 0xffff)
                + "~res=" + ~((byte) res)
        );
        */

        return res;
    } // ()

    /**
     * Convierte un array de bytes en su representación hexadecimal.
     *
     * @param bytes El array de bytes a convertir.
     * @return Una cadena que representa los bytes en formato hexadecimal separados por dos puntos (:).
     */
    public static String bytesToHexString( byte[] bytes ) {

        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
            sb.append(':');
        }
        return sb.toString();
    } // ()
} // class