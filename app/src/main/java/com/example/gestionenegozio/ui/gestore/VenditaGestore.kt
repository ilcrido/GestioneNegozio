package com.example.gestionenegozio.ui.gestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionenegozio.dati.dao.ElementoConProdotto
import com.example.gestionenegozio.dati.repository.RepositoryVendita
import com.example.gestionenegozio.dati.repository.RepositoryProdotto
import com.example.gestionenegozio.dati.repository.RepositoryUtente
import com.example.gestionenegozio.dati.repository.CosaVendere
import com.example.gestionenegozio.dati.entita.Prodotto
import com.example.gestionenegozio.dati.entita.MetodoPagamento
import com.example.gestionenegozio.dati.entita.Vendita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class VenditaGestore(
    private val depositoVendita: RepositoryVendita,
    private val depositoProdotto: RepositoryProdotto,
    private val depositoUtente: RepositoryUtente
) : ViewModel() {

    private val _carrello = MutableStateFlow<List<ElementoCarrello>>(emptyList())
    val carrello: StateFlow<List<ElementoCarrello>> = _carrello.asStateFlow()

    private val _totale = MutableStateFlow(0.0)
    val totale: StateFlow<Double> = _totale.asStateFlow()

    private val _caricamento = MutableStateFlow(false)
    val caricamento: StateFlow<Boolean> = _caricamento.asStateFlow()

    private val _messaggio = MutableStateFlow<String?>(null)
    val messaggio: StateFlow<String?> = _messaggio.asStateFlow()

    private val _venditeRecenti = MutableStateFlow<List<VenditaConDettagli>>(emptyList())
    val venditeRecenti: StateFlow<List<VenditaConDettagli>> = _venditeRecenti.asStateFlow()

    private val _statisticheOggi = MutableStateFlow<StatisticheGiornaliere?>(null)
    val statisticheOggi: StateFlow<StatisticheGiornaliere?> = _statisticheOggi.asStateFlow()

    init {
        caricaStatisticheOggi()
    }

    fun aggiungiAlCarrello(prodotto: Prodotto, quantita: Int = 1) {
        val carrelloCorrente = _carrello.value.toMutableList()
        val elementoEsistente = carrelloCorrente.find { it.idProdotto == prodotto.id }

        if (elementoEsistente != null) {
            val nuovaQuantita = elementoEsistente.quantita + quantita
            if (nuovaQuantita <= prodotto.scorta) {
                val indice = carrelloCorrente.indexOf(elementoEsistente)
                carrelloCorrente[indice] = elementoEsistente.copy(
                    quantita = nuovaQuantita,
                    prezzoTotale = prodotto.prezzo * nuovaQuantita
                )
            } else {
                _messaggio.value = "Scorta insufficiente"
                return
            }
        } else {
            if (quantita <= prodotto.scorta) {
                carrelloCorrente.add(
                    ElementoCarrello(
                        idProdotto = prodotto.id,
                        nomeProdotto = prodotto.nome,
                        quantita = quantita,
                        prezzoUno = prodotto.prezzo,
                        prezzoTotale = prodotto.prezzo * quantita
                    )
                )
            } else {
                _messaggio.value = "Scorta insufficiente"
                return
            }
        }

        _carrello.value = carrelloCorrente
        calcolaTotale()
    }

    fun rimuoviDalCarrello(idProdotto: Long) {
        _carrello.value = _carrello.value.filter { it.idProdotto != idProdotto }
        calcolaTotale()
    }

    fun modificaQuantita(idProdotto: Long, nuovaQuantita: Int) {
        if (nuovaQuantita <= 0) {
            rimuoviDalCarrello(idProdotto)
            return
        }

        val carrelloCorrente = _carrello.value.toMutableList()
        val indice = carrelloCorrente.indexOfFirst { it.idProdotto == idProdotto }

        if (indice >= 0) {
            val elemento = carrelloCorrente[indice]
            carrelloCorrente[indice] = elemento.copy(
                quantita = nuovaQuantita,
                prezzoTotale = elemento.prezzoUno * nuovaQuantita
            )
            _carrello.value = carrelloCorrente
            calcolaTotale()
        }
    }

    private fun calcolaTotale() {
        _totale.value = _carrello.value.sumOf { it.prezzoTotale }
    }

    fun completaVendita(idDipendente: Long, metodoPagamento: MetodoPagamento, onCompletata: () -> Unit) {
        if (_carrello.value.isEmpty()) {
            _messaggio.value = "Il carrello è vuoto"
            return
        }

        _caricamento.value = true

        viewModelScope.launch {
            try {
                val coseDaVendere = _carrello.value.map { elemento ->
                    CosaVendere(
                        idProdotto = elemento.idProdotto,
                        nomeProdotto = elemento.nomeProdotto,
                        quantita = elemento.quantita,
                        prezzoUno = elemento.prezzoUno,
                        prezzoTotale = elemento.prezzoTotale
                    )
                }

                depositoVendita.creaVendita(idDipendente, coseDaVendere, metodoPagamento)

                svuotaCarrello()
                caricaVenditeRecenti()
                caricaStatisticheOggi()
                _messaggio.value = "Vendita completata!"
                onCompletata()

            } catch (e: Exception) {
                _messaggio.value = "Errore durante la vendita: ${e.message}"
            } finally {
                _caricamento.value = false
            }
        }
    }

    fun svuotaCarrello() {
        _carrello.value = emptyList()
        _totale.value = 0.0
    }

    fun cercaProdottoPer(termine: String, onRisultato: (List<Prodotto>) -> Unit) {
        viewModelScope.launch {
            try {
                val risultati = if (termine.isBlank()) {
                    emptyList()
                } else {
                    depositoProdotto.cercaProdotti(termine)
                }
                onRisultato(risultati)
            } catch (e: Exception) {
                onRisultato(emptyList())
            }
        }
    }

    fun caricaVenditeRecenti() {
        viewModelScope.launch {
            try {
                println("DEBUG: Inizio caricamento vendite")
                val vendite = depositoVendita.ottieniTutteVendite().first()
                println("DEBUG: Trovate ${vendite.size} vendite nel database")

                val venditeConDettagli = vendite.take(20).map { vendita ->
                    try {
                        val elementi = depositoVendita.ottieniDettagliVendita(vendita.id)
                        println("DEBUG: Vendita ${vendita.id} ha ${elementi.size} elementi")

                        // Ottieni il nome del dipendente
                        val dipendente = depositoUtente.ottieniUtentePerId(vendita.idDipendente)
                        val nomeDipendente = dipendente?.nomeCompleto ?: "Dipendente sconosciuto"

                        VenditaConDettagli(
                            vendita = vendita,
                            elementi = elementi,
                            nomeDipendente = nomeDipendente
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Errore dettagli vendita ${vendita.id}: ${e.message}")
                        VenditaConDettagli(
                            vendita = vendita,
                            elementi = emptyList(),
                            nomeDipendente = "Errore"
                        )
                    }
                }

                _venditeRecenti.value = venditeConDettagli
                println("DEBUG: Aggiornato _venditeRecenti con ${venditeConDettagli.size} vendite")
            } catch (e: Exception) {
                println("DEBUG: Errore caricamento vendite: ${e.message}")
                _messaggio.value = "Errore caricamento vendite: ${e.message}"
            }
        }
    }

    fun caricaStatisticheOggi() {
        viewModelScope.launch {
            try {
                // Calcolo per oggi
                val calendar = Calendar.getInstance()

                // Inizio giorno di oggi
                val inizioGiornoCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val inizioGiorno = inizioGiornoCalendar.timeInMillis

                // Fine giorno di oggi
                val fineGiornoCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val fineGiorno = fineGiornoCalendar.timeInMillis

                // Calcolo per il mese corrente
                val inizioMeseCalendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val inizioMese = inizioMeseCalendar.timeInMillis

                // Fine del mese corrente
                val fineMeseCalendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val fineMese = fineMeseCalendar.timeInMillis

                // Debug per verificare i calcoli
                println("DEBUG STATISTICHE:")
                println("Inizio giorno: ${Date(inizioGiorno)}")
                println("Fine giorno: ${Date(fineGiorno)}")
                println("Inizio mese: ${Date(inizioMese)}")
                println("Fine mese: ${Date(fineMese)}")

                val totaleOggi = depositoVendita.ottieniStatistiche(inizioGiorno, fineGiorno)
                val totaleMese = depositoVendita.ottieniStatistiche(inizioMese, fineMese)

                println("Vendite oggi: ${totaleOggi.quanteVendite}, incasso: €${totaleOggi.soldiTotali}")
                println("Vendite mese: ${totaleMese.quanteVendite}, incasso: €${totaleMese.soldiTotali}")

                _statisticheOggi.value = StatisticheGiornaliere(
                    venditeOggi = totaleOggi.quanteVendite,
                    incassoOggi = totaleOggi.soldiTotali,
                    incassoMese = totaleMese.soldiTotali
                )
            } catch (e: Exception) {
                println("Errore caricamento statistiche: ${e.message}")
                e.printStackTrace()
                _messaggio.value = "Errore caricamento statistiche: ${e.message}"
            }
        }
    }

    fun pulisciMessaggio() {
        _messaggio.value = null
    }
}

data class ElementoCarrello(
    val idProdotto: Long,
    val nomeProdotto: String,
    val quantita: Int,
    val prezzoUno: Double,
    val prezzoTotale: Double
)

data class VenditaConDettagli(
    val vendita: Vendita,
    val elementi: List<ElementoConProdotto>,
    val nomeDipendente: String
)

data class StatisticheGiornaliere(
    val venditeOggi: Int,
    val incassoOggi: Double,
    val incassoMese: Double
)