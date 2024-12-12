package com.m4gti.ecobreeze.models;

/**
 * @class UsuarioActivo
 * @brief Clase que se usa para almacenar temporalmente los datos del usuario activo.
 *
 * Esta clase obtiene y guarda los datos del usuario activo.
 */
public class UsuarioActivo {

    private int userId;
    private String userName;
    private String userRole;
    private String macAddress;

    // --------------------------------------------------------------
    /**
     * @brief Constructor de `UsuarioActivo` que inicializa el usuario con su ID, nombre y rol.
     *
     * @param userId   Identificador único del usuario.
     * @param userName Nombre del usuario.
     * @param userRole Rol del usuario.
     */
    // --------------------------------------------------------------
    public UsuarioActivo(int userId, String userName, String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
    }

    // --------------------------------------------------------------
    /**
     * @brief Obtiene el ID del usuario.
     *
     * @return ID del usuario.
     */
    // --------------------------------------------------------------
    public int getUserId() {
        return userId;
    }

    // --------------------------------------------------------------
    /**
     * @brief Establece el ID del usuario.
     *
     * @param userId Nuevo ID para el usuario.
     */
    // --------------------------------------------------------------
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // --------------------------------------------------------------
    /**
     * @brief Obtiene el nombre del usuario.
     *
     * @return Nombre del usuario.
     */
    // --------------------------------------------------------------
    public String getUserName() {
        return userName;
    }

    // --------------------------------------------------------------
    /**
     * @brief Establece el nombre del usuario.
     *
     * @param userName Nuevo nombre para el usuario.
     */
    // --------------------------------------------------------------
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // --------------------------------------------------------------
    /**
     * @brief Obtiene el rol del usuario.
     *
     * @return Rol del usuario.
     */
    // --------------------------------------------------------------
    public String getUserRole() {
        return userRole;
    }

    // --------------------------------------------------------------
    /**
     * @brief Establece el rol del usuario.
     *
     * @param userRole Nuevo rol para el usuario.
     */
    // --------------------------------------------------------------
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    // --------------------------------------------------------------
    /**
     * @brief Obtiene la dirección MAC asociada al usuario.
     *
     * @return Dirección MAC del usuario.
     */
    // --------------------------------------------------------------
    public String getMacAddress() {
        return macAddress;
    }

    // --------------------------------------------------------------
    /**
     * @brief Establece la dirección MAC del usuario.
     *
     * @param macAddress Nueva dirección MAC para el usuario.
     */
    // --------------------------------------------------------------
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}