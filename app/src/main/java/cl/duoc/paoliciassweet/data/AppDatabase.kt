package cl.duoc.paoliciassweet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Usuario::class, Producto::class, Reserva::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun reservaDao(): ReservaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        // Catálogo ficticio (datos sintéticos) con precio base por persona en CLP
        private val catalogoInicial = listOf(
            Producto(nombre = "Torta tres leches", categoria = "Tortas", precioPorPersona = 3200,
                descripcion = "Bizcocho esponjoso bañado en tres leches con crema chantilly."),
            Producto(nombre = "Torta mil hojas manjar", categoria = "Tortas", precioPorPersona = 3500,
                descripcion = "Capas crujientes de hojarasca con manjar casero y nueces."),
            Producto(nombre = "Torta chocolate personalizada", categoria = "Tortas", precioPorPersona = 4200,
                descripcion = "Torta de chocolate con diseño y mensaje a pedido."),
            Producto(nombre = "Coffee break corporativo", categoria = "Banquetería", precioPorPersona = 4800,
                descripcion = "Café, té, jugos, mini sándwiches y pastelería fina."),
            Producto(nombre = "Cóctel de bienvenida", categoria = "Banquetería", precioPorPersona = 6900,
                descripcion = "Canapés fríos y calientes, mini empanadas y bebestibles."),
            Producto(nombre = "Almuerzo ejecutivo", categoria = "Banquetería", precioPorPersona = 9800,
                descripcion = "Entrada, plato de fondo y postre servido en mesa."),
            Producto(nombre = "Menú vegetariano", categoria = "Banquetería", precioPorPersona = 8900,
                descripcion = "Entrada, fondo y postre sin carnes. Apto para vegetarianos."),
            Producto(nombre = "Menú sin gluten", categoria = "Banquetería", precioPorPersona = 9400,
                descripcion = "Preparado sin gluten, apto para celíacos. Indica alergias al reservar.")
        )

        private val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                catalogoInicial.forEach { p ->
                    db.execSQL(
                        "INSERT INTO productos (nombre, descripcion, categoria, precioPorPersona) " +
                            "VALUES (?, ?, ?, ?)",
                        arrayOf<Any?>(p.nombre, p.descripcion, p.categoria, p.precioPorPersona)
                    )
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "INSERT OR IGNORE INTO usuarios (nombre, email, telefono, empresa, passwordHash, rol) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        "Administradora", Roles.ADMIN_EMAIL, "900000000", "Paolicias.Sweet",
                        hashPassword(Roles.ADMIN_PASSWORD), Roles.ADMIN
                    )
                )
            }
        }

        fun get(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paoliciassweet.db"
                )
                    .addCallback(callback)
                    .build().also { instancia = it }
            }
    }
}
