package com.example.gestionenegozio.dati.dao

import androidx.room.*
import com.example.gestionenegozio.dati.entita.Prodotto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdottoDao {
    @Query("SELECT * FROM prodotti WHERE attivo = 1")
    fun ottieniTuttiProdottiAttivi(): Flow<List<Prodotto>>

    @Query("SELECT * FROM prodotti WHERE codiceBarre = :codiceBarre AND attivo = 1")
    suspend fun ottieniProdottoPerCodiceBarre(codiceBarre: String): Prodotto?

    @Query("SELECT * FROM prodotti WHERE id = :idProdotto AND attivo = 1")
    suspend fun ottieniProdottoPerId(idProdotto: Long): Prodotto?

    @Query("SELECT * FROM prodotti WHERE nome LIKE :ricerca AND attivo = 1")
    suspend fun cercaProdotti(ricerca: String): List<Prodotto>

    @Insert
    suspend fun inserisciProdotto(prodotto: Prodotto): Long

    @Update
    suspend fun aggiornaProdotto(prodotto: Prodotto)

    @Query("UPDATE prodotti SET attivo = 0 WHERE id = :idProdotto")
    suspend fun disattivaProdotto(idProdotto: Long)

    @Query("UPDATE prodotti SET scorta = scorta - :quantita WHERE id = :idProdotto")
    suspend fun riduciScorta(idProdotto: Long, quantita: Int)

    @Query("SELECT * FROM prodotti WHERE scorta <= 10 AND attivo = 1")
    fun ottieniProdottiScortaBassa(): Flow<List<Prodotto>>
}