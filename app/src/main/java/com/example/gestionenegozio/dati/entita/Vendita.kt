package com.example.gestionenegozio.dati.entita

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vendite",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["idDipendente"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Vendita(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idDipendente: Long,
    val totale: Double,
    val metodoPagamento: MetodoPagamento,
    val note: String? = null,
    val creatoIl: Long = System.currentTimeMillis()
)

enum class MetodoPagamento {
    CONTANTI,
    CARTA
}