# Sesayou: memoria del bloque 2

Seguridad en Entornos Móviles, grupo I.

- Miembros: (completar)
- Repositorio: https://github.com/SilverDelgado/Sesayou (etiqueta `bloque2`)

## Trabajo hecho en el bloque 2

- **P2.1.** Montamos el entorno y nos tocó el dominio de fichaje laboral con ubicación. Analizamos una app real del sistema, el Reloj de Google, para ver sus permisos, sus componentes exportados y su firma. Ese análisis y las convenciones del proyecto (paquete, minSdk, targetSdk y ramas) están en el README.
- **P2.2.** Partimos del proyecto base de la asignatura y lo adaptamos a fichajes: `Elemento` pasa a ser `Fichaje` y la app se llama Sesayou. Medimos lo que guarda y lo que envía. El token está en claro en las preferencias, la contraseña y el token salen en logcat y los fichajes se guardan sin cifrar. También arreglamos el cierre de sesión, que no borraba los fichajes del usuario.
- **P2.3.** Añadimos la pantalla de detalle, un enlace profundo (`appsegura://detalle/{id}`), el permiso de ubicación y `FLAG_SECURE`. Comprobamos que la interfaz no protege nada. El enlace abre el detalle sin haber iniciado sesión, y los datos no llegan solo porque el repositorio pide un token. Además, el servidor devuelve fichajes de otro usuario a cualquiera que tenga un token válido.

## 1. La aplicación

Sesayou es una app de fichaje laboral con ubicación, en tono de parodia. El empleado ficha la entrada y la salida, y la app guarda la hora y desde dónde lo ha hecho. Si llega tarde, avisa a su jefe, le salta una alerta y le descuenta dinero.

La usarían los empleados de una empresa para fichar y ver su historial, y sus responsables para revisar los fichajes.

El dato más sensible es la ubicación del empleado junto con su identidad y su horario. Con eso se puede saber dónde está y cuál es su rutina, dentro y fuera del trabajo.

## 2. Datos que maneja

El detalle completo está en [INVENTARIO.md](INVENTARIO.md). Resumen:

| Dato | Dónde se guarda hoy | Quién puede leerlo hoy |
|---|---|---|
| Token de sesión | `shared_prefs/sesion.xml` en claro, logcat y tráfico HTTP | Cualquiera con adb y depuración, quien lea logcat o esté en la misma red, y las copias de seguridad |
| Contraseña | No se guarda, pero sale en logcat y viaja en claro | Quien lea logcat o esté en la misma red |
| Fichajes | Base de datos `appbase.db` sin cifrar, logcat y tráfico HTTP | Cualquiera con adb y depuración, quien lea logcat o esté en la misma red, y las copias de seguridad |
| Horario y rutina | Se deduce de las fechas de los fichajes y de los logs del servidor | Quien tenga la base de datos, el servidor y cualquiera en la red |
| Ubicación | Hoy solo se pide el permiso. Cuando se guarde y se envíe, lo hará en claro como el resto | El sistema y la app. Después, los mismos que pueden leer los fichajes |
| Nombre, rol, correo y DNI | Logcat y tráfico HTTP (login y perfil) | Quien lea logcat o esté en la misma red |

## 3. Superficie expuesta

| Vía de entrada | Cómo se invoca hoy | Qué comprueba hoy | Se trata en |
|---|---|---|---|
| Activity principal exportada | `adb shell am start -n` o un Intent desde otra app | Nada | Sesión 3.4 |
| Enlace profundo `appsegura://detalle/{id}` | Un enlace web, un mensaje u otra app | Nada en la interfaz. Solo el repositorio pide token | Sesión 3.4 |
| Llamadas a la API | `curl` con un token válido | Solo que el token sea válido, no de quién es el fichaje | Sesión 3.5, P3.2 y Bloque IV |
| Tráfico de red | Cualquiera en la misma red o con un proxy | Nada, va por HTTP | Sesión 3.5 y P3.2 |
| Almacenamiento local | `adb shell run-as` y copias de seguridad | Nada, está en claro | Sesión 3.3 y P3.1 |
| Registros | `adb logcat` | Nada | Sesión 3.6 |
| Vista de recientes y capturas | Botón del sistema | `FLAG_SECURE` ya activado | Sesión 3.6 |
| Ubicación | Diálogo al pulsar «Conceder ubicación» | El usuario decide y la app sigue funcionando si la deniega | Sesiones 3.3 y 3.5 |

## 4. Modelo de amenazas preliminar

1. **Un compañero que quiere fichar por otro.** Le coge el móvil o le roba la sesión, que está en claro en el teléfono y sale en los registros. Así puede fichar en su nombre o tapar sus retrasos.
2. **Alguien en la misma wifi**, por ejemplo la de la oficina o la de una cafetería. Como todo va sin cifrar, ve la contraseña, el token y los fichajes al momento.
3. **Un empleado curioso que mira los fichajes de los demás.** Basta con cambiar el número de fichaje en la petición para que el servidor le devuelva los de otro: a qué hora entra cada uno y, más adelante, desde dónde.
4. **Quien encuentra o compra de segunda mano un móvil mal borrado.** Los fichajes y la sesión siguen en el teléfono sin cifrar, y con la ubicación se podría saber dónde trabaja y dónde vive el dueño.

## 5. Reparto de trabajo

| Requisito | Responsable | Estado |
|---|---|---|
| Autenticación de usuario | (completar) | Hecho, sin proteger |
| Almacenamiento local de datos sensibles | (completar) | Hecho, sin cifrar |
| Llamada a una API remota | (completar) | Hecho, por HTTP |
| Permiso peligroso (ubicación) | (completar) | Hecho, se pide en el momento de usarlo |
