package com.example.gestionenegozio.dati.repository

import com.example.gestionenegozio.dati.dao.ProdottoDao
import com.example.gestionenegozio.dati.entita.Prodotto
import kotlinx.coroutines.flow.Flow

class RepositoryProdotto(private val prodottoDao: ProdottoDao) {

    fun ottieniTuttiProdotti(): Flow<List<Prodotto>> {
        return prodottoDao.ottieniTuttiProdottiAttivi()
    }

    suspend fun trovaProdottoConCodice(codiceBarre: String): Prodotto? {
        return prodottoDao.ottieniProdottoPerCodiceBarre(codiceBarre)
    }

    suspend fun ottieniProdotto(id: Long): Prodotto? {
        return prodottoDao.ottieniProdottoPerId(id)
    }

    suspend fun cercaProdotti(parolaChiave: String): List<Prodotto> {
        val ricerca = "%$parolaChiave%"
        return prodottoDao.cercaProdotti(ricerca)
    }

    suspend fun aggiungiProdotto(
        nome: String,
        descrizione: String?,
        codiceBarre: String?,
        prezzo: Double,
        scorta: Int,
        categoria: String?
    ): Long {
        val nuovoProdotto = Prodotto(
            nome = nome,
            descrizione = descrizione,
            codiceBarre = codiceBarre,
            prezzo = prezzo,
            scorta = scorta,
            categoria = categoria
        )
        return prodottoDao.inserisciProdotto(nuovoProdotto)
    }

    suspend fun modificaProdotto(prodotto: Prodotto) {
        val prodottoModificato = prodotto.copy(modificatoIl = System.currentTimeMillis())
        prodottoDao.aggiornaProdotto(prodottoModificato)
    }

    suspend fun eliminaProdotto(idProdotto: Long) {
        prodottoDao.disattivaProdotto(idProdotto)
    }

    suspend fun diminuisciScorta(idProdotto: Long, quantita: Int) {
        prodottoDao.riduciScorta(idProdotto, quantita)
    }

    fun ottieniProdottiScortaFinita(): Flow<List<Prodotto>> {
        return prodottoDao.ottieniProdottiScortaBassa()
    }
}