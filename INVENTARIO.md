# Inventario de datos sensibles

Estado al terminar la P2.2. Las tres primeras columnas son lo que hemos comprobado mirando logcat, leyendo los ficheros con `run-as` y sacando la base de datos del móvil. La última se rellena en el Bloque III.

`<app>` es `/data/data/es.uma.seguridad.appbase/`.

| Dato | Dónde se guarda hoy | Quién puede leerlo hoy | Cómo se protegerá |
|---|---|---|---|
| Token de sesión | `<app>/shared_prefs/sesion.xml`, en claro. También sale en logcat y viaja por HTTP. | Cualquiera con adb y la depuración activada. Cualquiera que lea logcat o esté en la misma red. También acaba en las copias de seguridad. | A completar en el Bloque III |
| Contraseña | No se guarda, pero sale en logcat en la petición de login y viaja en claro por HTTP. | Cualquiera que lea logcat o esté en la misma red. | A completar en el Bloque III |
| Fichajes (tipo, detalle y fecha) | Tabla `fichajes` de `<app>/databases/appbase.db`, sin cifrar. Lo más reciente está en `appbase.db-wal`. También sale en logcat y viaja por HTTP. | Cualquiera con adb y la depuración activada. Cualquiera que lea logcat o esté en la misma red. También acaba en las copias de seguridad. | A completar en el Bloque III |
| Horario y rutina del empleado | Se saca de las fechas de los fichajes y de los logs del servidor, que apuntan quién llama y cuándo. | Quien tenga la base de datos, el servidor y cualquiera en la red, aunque no vea el contenido. | A completar en el Bloque III |
| Nombre de usuario | `sesion.xml` en claro y columna `propietario` de la tabla. También sale en logcat. | Cualquiera con adb y la depuración activada. Cualquiera que lea logcat. | A completar en el Bloque III |
| Nombre completo y rol | Sale en logcat con la respuesta del login y viaja por HTTP. | Cualquiera que lea logcat o esté en la misma red. | A completar en el Bloque III |
| Perfil (correo y DNI) | No se guarda, pero si se pide `/api/perfil` sale entero en logcat y viaja por HTTP. | Cualquiera que lea logcat o esté en la misma red. | A completar en el Bloque III |
| Dirección del backend | Dentro del APK. | Cualquiera que tenga el APK. No es secreta, pero dice qué servidor usamos y que va por HTTP. | A completar en el Bloque III |

## Datos que vendrán con la app

Cuando hagamos lo de la P2.3 habrá que añadir:

- La ubicación al fichar.
- Los avisos al jefe cuando alguien llega tarde.
- Los descuentos y penalizaciones.
- Cuándo se enciende o se usa el móvil, si al final lo hacemos.

## Cómo comprobarlo

```
adb logcat | grep -i -E "Repositorio|okhttp"
adb shell run-as es.uma.seguridad.appbase cat shared_prefs/sesion.xml
adb exec-out run-as es.uma.seguridad.appbase cat databases/appbase.db > local.db
adb exec-out run-as es.uma.seguridad.appbase cat databases/appbase.db-wal > local.db-wal
adb exec-out run-as es.uma.seguridad.appbase cat databases/appbase.db-shm > local.db-shm
sqlite3 local.db "SELECT * FROM fichajes;"
```
