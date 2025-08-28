package com.example.gestionenegozio.dati.repository

import com.example.gestionenegozio.dati.dao.VenditaDao
import com.example.gestionenegozio.dati.dao.ElementoVenditaDao
import com.example.gestionenegozio.dati.dao.ProdottoDao
import com.example.gestionenegozio.dati.entita.Vendita
import com.example.gestionenegozio.dati.entita.ElementoVendita
import com.example.gestionenegozio.dati.entita.MetodoPagamento
import com.example.gestionenegozio.dati.dao.ElementoConProdotto
import com.example.gestionenegozio.dati.dao.StatisticheProdotto
import kotlinx.coroutines.flow.Flow

class RepositoryVendita(
    private val venditaDao: VenditaDao,
    private val elementoVenditaDao: ElementoVenditaDao,
    private val prodottoDao: ProdottoDao
) {

    suspend fun creaVendita(
        idDipendente: Long,
        coseDaVendere: List<CosaVendere>,
        comePagare: MetodoPagamento,
        note: String? = null
    ): Long {
        val prezzoTotale = coseDaVendere.sumOf { it.prezzoTotale }

        val nuovaVendita = Vendita(
            idDipendente = idDipendente,
            totale = prezzoTotale,
            metodoPagamento = comePagare,
            note = note
        )

        val idVendita = venditaDao.inserisciVendita(nuovaVendita)

        val elementiDaInserire = coseDaVendere.map { cosa ->
            ElementoVendita(
                idVendita = idVendita,
                idProdotto = cosa.idProdotto,
                quantita = cosa.quantita,
                prezzoUnitario = cosa.prezzoUno,
                prezzoTotale = cosa.prezzoTotale
            )
        }

        venditaDao.inserisciElementiVendita(elementiDaInserire)

        coseDaVendere.forEach { cosa ->
            prodottoDao.riduciScorta(cosa.idProdotto, cosa.quantita)
        }

        return idVendita
    }

    fun ottieniTutteVendite(): Flow<List<Vendita>> {
        return venditaDao.ottieniTutteVendite()
    }

    fun ottieniVenditeDelDipendente(idDipendente: Long): Flow<List<Vendita>> {
        return venditaDao.ottieniVenditePerDipendente(idDipendente)
    }

    fun ottieniVenditeDelPeriodo(inizioPeriodo: Long, finePeriodo: Long): Flow<List<Vendita>> {
        return venditaDao.ottieniVenditePerPeriodo(inizioPeriodo, finePeriodo)
    }

    suspend fun ottieniDettagliVendita(idVendita: Long): List<ElementoConProdotto> {
        return elementoVenditaDao.ottieniElementiConNomeProdotto(idVendita)
    }

    suspend fun ottieniStatistiche(inizioPeriodo: Long, finePeriodo: Long): RiepilogoVendite {
        val soldiTotali = venditaDao.ottieniTotaleVenditePerPeriodo(inizioPeriodo, finePeriodo) ?: 0.0
        val quanteVendite = venditaDao.contaVenditePerPeriodo(inizioPeriodo, finePeriodo)
        val prodottiMigliori = elementoVenditaDao.ottieniProdottiPiuVenduti(inizioPeriodo, finePeriodo)

        return RiepilogoVendite(
            soldiTotali = soldiTotali,
            quanteVendite = quanteVendite,
            prodottiMigliori = prodottiMigliori
        )
    }
}

data class CosaVendere(
    val idProdotto: Long,
    val nomeProdotto: String,
    val quantita: Int,
    val prezzoUno: Double,
    val prezzoTotale: Double
)

data class RiepilogoVendite(
    val soldiTotali: Double,
    val quanteVendite: Int,
    val prodottiMigliori: List<StatisticheProdotto>
)