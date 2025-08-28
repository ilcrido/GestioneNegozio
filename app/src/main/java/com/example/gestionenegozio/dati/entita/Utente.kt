package com.example.gestionenegozio.dati.entita

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utenti")
data class Utente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nomeUtente: String,
    val password: String,
    val nomeCompleto: String,
    val ruolo: RuoloUtente,
    val attivo: Boolean = true,
    val creatoIl: Long = System.currentTimeMillis()
)

enum class RuoloUtente {
    ADMIN,
    DIPENDENTE
}