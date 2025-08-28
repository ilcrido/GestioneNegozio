package com.example.gestionenegozio.dati.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gestionenegozio.dati.entita.*
import com.example.gestionenegozio.dati.dao.*

@Database(
    entities = [Utente::class, Prodotto::class, Vendita::class, ElementoVendita::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Convertitori::class)
abstract class DatabaseNegozio : RoomDatabase() {

    abstract fun utenteDao(): UtenteDao
    abstract fun prodottoDao(): ProdottoDao
    abstract fun venditaDao(): VenditaDao
    abstract fun elementoVenditaDao(): ElementoVenditaDao

    companion object {
        @Volatile
        private var ISTANZA: DatabaseNegozio? = null

        fun ottieniDatabase(context: Context): DatabaseNegozio {
            return ISTANZA ?: synchronized(this) {
                val istanza = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseNegozio::class.java,
                    "database_negozio"
                )
                    .addCallback(CallbackDatabase())
                    .build()
                ISTANZA = istanza
                istanza
            }
        }

        private class CallbackDatabase : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("""
                    INSERT INTO utenti (nomeUtente, password, nomeCompleto, ruolo, attivo, creatoIl) 
                    VALUES ('admin', 'admin123', 'Amministratore', 'ADMIN', 1, ${System.currentTimeMillis()})
                """)
            }
        }
    }
}