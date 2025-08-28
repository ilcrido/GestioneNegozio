package com.example.gestionenegozio.dati.entita

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prodotti")
data class Prodotto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val descrizione: String? = null,
    val codiceBarre: String? = null,
    val prezzo: Double,
    val scorta: Int,
    val categoria: String? = null,
    val immaginePath: String? = null,
    val attivo: Boolean = true,
    val creatoIl: Long = System.currentTimeMillis(),
    val modificatoIl: Long = System.currentTimeMillis()
)