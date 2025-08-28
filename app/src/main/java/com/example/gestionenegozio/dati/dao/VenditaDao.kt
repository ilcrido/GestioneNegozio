package com.example.gestionenegozio.dati.dao

import androidx.room.*
import com.example.gestionenegozio.dati.entita.Vendita
import com.example.gestionenegozio.dati.entita.ElementoVendita
import kotlinx.coroutines.flow.Flow

@Dao
interface VenditaDao {
    @Insert
    suspend fun inserisciVendita(vendita: Vendita): Long

    @Insert
    suspend fun inserisciElementiVendita(elementi: List<ElementoVendita>)

    @Query("SELECT * FROM vendite ORDER BY creatoIl DESC")
    fun ottieniTutteVendite(): Flow<List<Vendita>>

    @Query("SELECT * FROM vendite WHERE idDipendente = :idDipendente ORDER BY creatoIl DESC")
    fun ottieniVenditePerDipendente(idDipendente: Long): Flow<List<Vendita>>

    @Query("SELECT * FROM vendite WHERE creatoIl BETWEEN :dataInizio AND :dataFine ORDER BY creatoIl DESC")
    fun ottieniVenditePerPeriodo(dataInizio: Long, dataFine: Long): Flow<List<Vendita>>

    @Query("SELECT SUM(totale) FROM vendite WHERE creatoIl BETWEEN :dataInizio AND :dataFine")
    suspend fun ottieniTotaleVenditePerPeriodo(dataInizio: Long, dataFine: Long): Double?

    @Query("SELECT COUNT(*) FROM vendite WHERE creatoIl BETWEEN :dataInizio AND :dataFine")
    suspend fun contaVenditePerPeriodo(dataInizio: Long, dataFine: Long): Int
}