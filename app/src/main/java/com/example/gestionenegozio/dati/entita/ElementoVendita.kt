package com.example.gestionenegozio.dati.entita

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "elementi_vendita",
    foreignKeys = [
        ForeignKey(
            entity = Vendita::class,
            parentColumns = ["id"],
            childColumns = ["idVendita"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Prodotto::class,
            parentColumns = ["id"],
            childColumns = ["idProdotto"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ElementoVendita(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idVendita: Long,
    val idProdotto: Long,
    val quantita: Int,
    val prezzoUnitario: Double,
    val prezzoTotale: Double
)