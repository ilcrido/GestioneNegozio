package com.example.gestionenegozio.dati.dao

import androidx.room.*
import com.example.gestionenegozio.dati.entita.Utente
import com.example.gestionenegozio.dati.entita.RuoloUtente
import kotlinx.coroutines.flow.Flow

@Dao
interface UtenteDao {
    @Query("SELECT * FROM utenti WHERE nomeUtente = :nomeUtente AND password = :password AND attivo = 1")
    suspend fun accedi(nomeUtente: String, password: String): Utente?

    @Query("SELECT * FROM utenti WHERE attivo = 1")
    fun ottieniTuttiUtentiAttivi(): Flow<List<Utente>>

    @Query("SELECT * FROM utenti WHERE ruolo = :ruolo AND attivo = 1")
    fun ottieniUtentiPerRuolo(ruolo: RuoloUtente): Flow<List<Utente>>

    @Insert
    suspend fun inserisciUtente(utente: Utente): Long

    @Update
    suspend fun aggiornaUtente(utente: Utente)

    @Query("UPDATE utenti SET attivo = 0 WHERE id = :idUtente")
    suspend fun disattivaUtente(idUtente: Long)

    @Query("SELECT COUNT(*) FROM utenti WHERE ruolo = 'ADMIN' AND attivo = 1")
    suspend fun contaAdmin(): Int

    @Query("SELECT * FROM utenti WHERE id = :idUtente AND attivo = 1")
    suspend fun ottieniUtentePerId(idUtente: Long): Utente?
}