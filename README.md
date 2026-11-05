# Sesayou

App Android de fichaje laboral, en plan parodia, para Seguridad en Entornos Móviles (UMA).

Ahora mismo la app es insegura a propósito: guarda el token en claro, saca datos sensibles por logcat y usa HTTP. Se arregla en el Bloque III. No uséis credenciales reales.

## Equipo

- Grupo: I
- Nombre del equipo: Sesayou
- Miembros: (completar)
- Dominio asignado: fichaje laboral con ubicación

## Qué hace

El empleado ficha la entrada y la salida, y la app guarda la hora y desde dónde ha fichado. Si llega tarde, avisa a su jefe, le salta una alerta y le quita dinero.

El dato más sensible es la ubicación del empleado junto con su identidad y su horario. Con eso se puede saber dónde está y cuál es su rutina.

## Requisitos mínimos

| Requisito | Cómo lo cumplimos | Estado | Responsable |
|---|---|---|---|
| Autenticación | Login con usuario y contraseña contra `/api/login`, que devuelve un token | Hecho, sin proteger | (completar) |
| Almacenamiento local de datos sensibles | Token en SharedPreferences y fichajes en una base de datos Room | Hecho, sin cifrar | (completar) |
| Llamada a una API remota | Retrofit contra el backend de la asignatura (`/api/login`, `/api/elementos`, `/api/perfil`) | Hecho, por HTTP | (completar) |
| Permiso peligroso | Ubicación, para saber desde dónde se ficha. Se pide al pulsar «Conceder ubicación» y la app sigue funcionando si se deniega | Hecho | (completar) |

Qué datos sensibles hay y dónde están: [INVENTARIO.md](INVENTARIO.md). La memoria del bloque 2, con la superficie expuesta y el modelo de amenazas: [MEMORIA.md](MEMORIA.md).

## Convenciones

- Paquete: `es.uma.seguridad.appbase`. Es el del proyecto base y los comandos de las prácticas lo usan tal cual.
- minSdk 26 (Android 8.0), el del proyecto base.
- targetSdk 37 (Android 17), la última versión. Así el sistema nos aplica todas sus restricciones de seguridad, por ejemplo la de red local, que nos obliga a pedir `ACCESS_LOCAL_NETWORK` para hablar con el backend. Bajarlo sería quitar protecciones para ahorrarnos trabajo.
- Ramas: `main` siempre tiene que compilar. Cada uno trabaja en su rama y la mete en `main` con un pull request que revisa otro compañero.
- Nada de claves, contraseñas ni bases de datos en el repo (está en el `.gitignore`).

## Estado

- P2.2: proyecto base con el modelo adaptado (`Elemento` pasa a ser `Fichaje`) y el arreglo del cierre de sesión de la sección 5.6.
- P2.3: pantalla de detalle, enlace profundo `appsegura://detalle/{id}`, permiso de ubicación y `FLAG_SECURE`. Es la versión etiquetada como `bloque2`.
- Bloque III: pendiente.

## Reconocimiento de una app real (P2.1, apartado 5.4)

En el emulador no hay ninguna app de fichaje y para bajarla de Google Play hace falta una cuenta, así que, como permite el enunciado, hemos usado una del sistema: el Reloj de Google (`com.google.android.deskclock`, versión 9.1).

| Qué mirar | Con qué | Observación |
|---|---|---|
| Permisos declarados | `aapt2 dump permissions` | 28. Los peligrosos son `READ_CALENDAR`, `READ_MEDIA_AUDIO` (para poner una canción como alarma), `POST_NOTIFICATIONS` y `READ_EXTERNAL_STORAGE` hasta Android 12. El resto son normales o de sistema, como alarmas exactas, encender la pantalla o quitar el bloqueo al sonar. |
| Componentes exportados | `apktool` y el manifiesto | 26 de 78: 13 activities, 6 servicios, 6 receivers y su único content provider. Varios piden un permiso para usarlos (`SET_ALARM`, `BIND_JOB_SERVICE`, `DUMP`). El provider está exportado sin permiso, pero el código rechaza las consultas de fuera (`content query` devuelve «No external queries»). |
| Esquemas de firma | `apksigner verify --print-certs` | Solo v3, con un certificado de Google (`CN=Android, O=Google Inc.`). Además lleva el sello de origen de Google Play (Source Stamp). |
| minSdk y targetSdk | `aapt2 dump badging` | minSdk 32 (Android 12L) y targetSdk 37 (Android 17). |
| Piezas instaladas | `adb shell pm path` | 2: `base.apk` y `split_config.xxhdpi.apk`, que lleva los recursos para la densidad de pantalla del emulador. |

Lo que nos llama la atención:

- El permiso que no sabríamos justificar mirando lo que hace la app es `READ_CALENDAR`. Seguramente sea para alguna función de alarmas ligada a la agenda, pero desde fuera no se ve.
- Tiene muchos componentes exportados, pero los protege con permisos o con comprobaciones en el código. Es lo mismo que vimos en la P2.3: lo que cuenta es lo que comprueba el código, no lo que declara el manifiesto.
- Comparado con Sesayou: nosotros solo exportamos `MainActivity` (con el enlace profundo), pedimos la ubicación en el momento de usarla y firmamos con la clave de depuración (v2), porque aún no hay versión publicada.

## Cómo ejecutarla

1. Arrancar el backend de la asignatura y comprobar que responde:
   ```
   python3 servidor.py
   curl http://localhost:8080/api/salud
   ```
   También vale `java Servidor.java` o `docker compose up --build`. Usuarios de prueba: `ana` / `Practica2024`, `luis` / `Practica2024` y `admin` / `Administrador`.
2. Abrir la carpeta raíz del repo en Android Studio, sincronizar Gradle y ejecutar en el emulador.
3. La dirección del backend está en `BASE_URL`, dentro de `datos/ApiServicio.kt`. Desde el emulador es `http://10.0.2.2:8080/` y desde un móvil, la IP del ordenador.

Si no conecta:

- En un emulador con Android 17 hay que dar el permiso de red local una vez después de instalar:
  ```
  adb shell pm grant es.uma.seguridad.appbase android.permission.ACCESS_LOCAL_NETWORK
  ```
- Si sigue sin ir: `adb reverse tcp:8080 tcp:8080` y `BASE_URL = "http://localhost:8080/"`. Hay que quitarlo antes de la P3.2.
- Si no sale nada en logcat: `adb logcat -G 16M`.

## Estructura

Todo está en `app/src/main/java/es/uma/seguridad/appbase/`:

- `MainActivity.kt` y `VistaModelo.kt`: arranque y estado de las pantallas.
- `modelo/Modelo.kt`: la clase `Fichaje` y los estados de la interfaz.
- `datos/ApiServicio.kt`: llamadas al backend con Retrofit.
- `datos/BaseDatos.kt`: base de datos Room con la tabla `fichajes`.
- `datos/Repositorio.kt`: junta la red, la sesión y la base de datos.
- `datos/SesionLocal.kt`: guarda el token.
- `ui/`: pantallas de login, listado y detalle.
