# EcoBreezeRepo-Android

Este Repositorio contiene la parte de la app de android del proyecto de Biometría y Medio Ambiente. 
Esta App es responsable de recibir los datos del beacon de Arduino y enviarlos a la base de datos.

## Guía de Uso

    1. Activar la placa de arduino (Comprobar que el programa de arduino este subido en la placa).
    
    2. Encender tu contenedor del docker, para esto deberás tambien tener clonado el repositorio del servidor en tu PC, una vez hayas hacho esto sigue las instrucciones para activar el contenedor de docker.
    
    3. En la página web registrar tu usuario.

    4. Una vez hecho esto, iniciar la App y loguearte.

    5. Escanear el codigo QR que vendrá en la caja del nodo. (Hay una copia de este en la carpeta doc)
    
Con esto la app debería empezar a recibir beacons de Arduino y enviarlos a la BBDD, desde la cual se mostrará en la página Web

## Estructura del proyecto:

Este repositorio se divide en 3 carpetas: doc, src y test.

        1. doc: Contiene toda la documentación de esta parte del proyecto.

        2. src: Carpeta principal que contiene todo el codigo fuente del repositorio.

            2.1. EcoBreeze: Carpeta de proyecto de AndroidStudio, dentro de esta se encuentran todos los archivos y carpetas que AndroidStudio y la propia App necesitan para funcionar, nos vamos a centrar en la carpeta principal de codigo (src/main/java/com/m4gti/ecobreeze) y la carpeta principal de recursos (src/res), recordar que en esta carpeta también esta el AndroidManifest.

                2.1.1. com/m4gti/ecobreeze:
                    2.1.1.1: logic: Carpeta que contiene la lógica fake de la app, es decir las llamadas a la api de forma que no esten implementadas directamente en los metodos de las clases.
                    
                    2.1.1.2: models: Carpeta que contiene los distintos POJOs que son necesarios en la app.
                    
                    2.1.1.3: services: Carpeta que contiene el servicio principal de la app.
                    
                    2.1.1.4: ui: carpeta que contiene todas las Actividades y archivos necesarios para estas.

                        2.1.1.4.1. activities: Contiene las actividades de la app.

                        2.1.1.4.2. adapters: Contiene los adaptadores necesarios de los recyclerview de la app.

                        2.1.1.4.3. fragments: Contiene los fragments de la app.

                    2.1.1.5: utils: Carpeta que contiene las clases con metodos o variables usadas en varias otras clases de la app.

                2.1.2. res: Contiene todos los recursos necesarios de la app:
            
        3. test: Contiene todos los tests realizados en este repositorio.

### Version 1.0