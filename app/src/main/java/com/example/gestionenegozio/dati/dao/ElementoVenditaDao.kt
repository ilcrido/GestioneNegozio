package com.example.gestionenegozio.dati.dao

import androidx.room.*
import com.example.gestionenegozio.dati.entita.ElementoVendita
import kotlinx.coroutines.flow.Flow

@Dao
interface ElementoVenditaDao {
    @Query("SELECT * FROM elementi_vendita WHERE idVendita = :idVendita")
    suspend fun ottieniElementiPerVendita(idVendita: Long): List<ElementoVendita>

    @Query("""
        SELECT p.nome, ev.quantita, ev.prezzoUnitario, ev.prezzoTotale 
        FROM elementi_vendita ev 
        JOIN prodotti p ON ev.idProdotto = p.id 
        WHERE ev.idVendita = :idVendita
    """)
    suspend fun ottieniElementiConNomeProdotto(idVendita: Long): List<ElementoConProdotto>

    @Query("""
        SELECT p.nome, SUM(ev.quantita) as quantitaTotale, SUM(ev.prezzoTotale) as ricavoTotale
        FROM elementi_vendita ev 
        JOIN prodotti p ON ev.idProdotto = p.id 
        JOIN vendite v ON ev.idVendita = v.id
        WHERE v.creatoIl BETWEEN :dataInizio AND :dataFine
        GROUP BY ev.idProdotto
        ORDER BY ricavoTotale DESC
    """)
    suspend fun ottieniProdottiPiuVenduti(dataInizio: Long, dataFine: Long): List<StatisticheProdotto>
}

data class ElementoConProdotto(
    val nome: String,
    val quantita: Int,
    val prezzoUnitario: Double,
    val prezzoTotale: Double
)

data class StatisticheProdotto(
    val nome: String,
    val quantitaTotale: Int,
    val ricavoTotale: Double
)