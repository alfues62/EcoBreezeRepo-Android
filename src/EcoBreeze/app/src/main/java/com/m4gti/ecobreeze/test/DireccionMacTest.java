package com.m4gti.ecobreeze.test;

import org.junit.Test;
import static org.junit.Assert.*;

public class DireccionMacTest {

    // Método a probar
    public boolean esDireccionMacValida(String mac) {
        return mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    @Test
    public void testDireccionMacValida() {
        // Casos válidos
        assertTrue(esDireccionMacValida("01:23:45:67:89:AB"));
        assertTrue(esDireccionMacValida("01-23-45-67-89-AB"));
        assertTrue(esDireccionMacValida("a1:b2:c3:d4:e5:f6"));
        assertTrue(esDireccionMacValida("A1-B2-C3-D4-E5-F6"));

        // Casos no válidos
        assertFalse(esDireccionMacValida("01:23:45:67:89"));       // Menos grupos
        assertFalse(esDireccionMacValida("01:23:45:67:89:AB:CD")); // Más grupos
        assertFalse(esDireccionMacValida("01:23:45:67:89:GZ"));    // Caracter inválido
        assertFalse(esDireccionMacValida("01:23:45:67:89:"));      // Grupo incompleto
        assertFalse(esDireccionMacValida(""));                     // Cadena vacía
    }
}