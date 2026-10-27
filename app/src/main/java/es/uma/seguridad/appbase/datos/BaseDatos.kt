package es.uma.seguridad.appbase.datos

import android.content.Context
import androidx.room.*

/* ==========================================================================
   LA BASE DE DATOS LOCAL
   Teoría: sesión 2.2.5 y 2.5.5.

   La aplicación guarda una copia local de los fichajes descargados para poder
   mostrarlos cuando no hay conexión. Room es una capa sobre SQLite que escribe
   el SQL por ti y comprueba las consultas al compilar.

   AVISO: el fichero de base de datos se escribe SIN CIFRAR. No es un descuido,
   es el comportamiento por defecto de Room. En la sección 5.3 lo sacarás del
   dispositivo y lo abrirás; en la práctica P3.1 lo cifrarás.
   ========================================================================== */

/**
 * Una fila de la tabla.
 *
 * Fíjate en el campo `propietario`, que no viene del backend: lo añadimos
 * nosotros para saber de qué usuario son los datos. Sin él, al entrar con otra
 * cuenta en el mismo dispositivo se verían mezclados los fichajes de las dos.
 */
@Entity(tableName = "fichajes")
data class FichajeEntidad(
    @PrimaryKey val id: Int,
    val tipo: String,
    val detalle: String,
    val fecha: String,
    val propietario: String
)

/**
 * El DAO declara QUÉ consultas existen; Room genera el código que las ejecuta
 * y comprueba el SQL al compilar, no en ejecución.
 *
 * Las tres funciones son `suspend`: no se pueden llamar desde el hilo
 * principal y el compilador lo impone. Es la regla de la sesión 2.4.5
 * convertida en una restricción del lenguaje.
 */
@Dao
interface FichajeDao {

    /** Los fichajes de un propietario, del más reciente al más antiguo. */
    @Query("SELECT * FROM fichajes WHERE propietario = :propietario ORDER BY fecha DESC")
    suspend fun obtenerDe(propietario: String): List<FichajeEntidad>

    /** Inserta una lista; si ya existe una fila con ese id, la reemplaza. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(fichajes: List<FichajeEntidad>)

    /** Borra todo lo de un propietario. Existe; otra cosa es que alguien la llame. */
    @Query("DELETE FROM fichajes WHERE propietario = :propietario")
    suspend fun borrarDe(propietario: String)
}

/*
 * version = 2: en la versión 1 (proyecto base) la tabla se llamaba elementos.
 * Es sólo una copia local de lo que hay en el backend, así que al cambiar de
 * versión se descarta y se vuelve a descargar en lugar de migrarla.
 */
@Database(entities = [FichajeEntidad::class], version = 2, exportSchema = false)
abstract class BaseDatos : RoomDatabase() {

    abstract fun fichajeDao(): FichajeDao

    companion object {

        /**
         * Abrir dos veces la misma base de datos corrompe los datos, así que se
         * guarda una única instancia. @Volatile y synchronized evitan que dos
         * hilos la construyan a la vez.
         */
        @Volatile private var instancia: BaseDatos? = null

        fun obtener(contexto: Context): BaseDatos =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDatos::class.java,
                    NOMBRE_FICHERO
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { instancia = it }
            }

        /** El fichero acaba en /data/data/<tu.paquete>/databases/appbase.db */
        const val NOMBRE_FICHERO = "appbase.db"
    }
}
