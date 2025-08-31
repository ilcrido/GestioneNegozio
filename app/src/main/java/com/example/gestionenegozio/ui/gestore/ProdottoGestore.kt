package com.example.gestionenegozio.ui.gestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionenegozio.dati.repository.RepositoryProdotto
import com.example.gestionenegozio.dati.entita.Prodotto
import com.example.gestionenegozio.ui.schermate.NuovoProdotto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProdottoGestore(private val depositoProdotto: RepositoryProdotto) : ViewModel() {

    private val _prodotti = MutableStateFlow<List<Prodotto>>(emptyList())
    val prodotti: StateFlow<List<Prodotto>> = _prodotti.asStateFlow()

    private val _caricamento = MutableStateFlow(false)
    val caricamento: StateFlow<Boolean> = _caricamento.asStateFlow()

    private val _messaggio = MutableStateFlow<String?>(null)
    val messaggio: StateFlow<String?> = _messaggio.asStateFlow()

    init {
        caricaProdotti()
    }

    fun caricaProdotti() {
        viewModelScope.launch {
            depositoProdotto.ottieniTuttiProdotti().collect { listaProdotti ->
                _prodotti.value = listaProdotti
            }
        }
    }

    fun aggiungiProdotto(nuovoProdotto: NuovoProdotto, onCompletato: (Boolean) -> Unit) {
        _caricamento.value = true
        _messaggio.value = null

        viewModelScope.launch {
            try {
                val prodottoEsistentePerCodice = nuovoProdotto.codiceBarre?.let { codice ->
                    depositoProdotto.trovaProdottoConCodice(codice)
                }

                val prodottoEsistentePerNome = depositoProdotto.cercaProdotti(nuovoProdotto.nome)
                    .find { it.nome.equals(nuovoProdotto.nome, ignoreCase = true) }

                when {
                    prodottoEsistentePerCodice != null -> {
                        _messaggio.value = "Esiste già un prodotto con questo codice a barre"
                        onCompletato(false)
                    }
                    prodottoEsistentePerNome != null -> {
                        _messaggio.value = "Esiste già un prodotto con questo nome"
                        onCompletato(false)
                    }
                    else -> {
                        val id = depositoProdotto.aggiungiProdotto(
                            nome = nuovoProdotto.nome,
                            descrizione = nuovoProdotto.descrizione,
                            codiceBarre = nuovoProdotto.codiceBarre,
                            prezzo = nuovoProdotto.prezzo,
                            scorta = nuovoProdotto.scorta,
                            categoria = nuovoProdotto.categoria
                        )

                        if (id > 0) {
                            _messaggio.value = "Prodotto aggiunto con successo!"
                            onCompletato(true)
                        } else {
                            _messaggio.value = "Errore durante il salvataggio"
                            onCompletato(false)
                        }
                    }
                }
            } catch (e: Exception) {
                _messaggio.value = "Errore: ${e.message}"
                onCompletato(false)
            } finally {
                _caricamento.value = false
            }
        }
    }

    fun aggiornaProdottoPerCodice(
        codiceBarre: String,
        nuovoNome: String,
        nuovaDescrizione: String?,
        nuovoPrezzo: Double,
        nuovaScorta: Int,
        nuovaCategoria: String?,
        onCompletato: (Boolean) -> Unit
    ) {
        _caricamento.value = true
        _messaggio.value = null

        viewModelScope.launch {
            try {
                val prodottoEsistente = depositoProdotto.trovaProdottoConCodice(codiceBarre)

                if (prodottoEsistente != null) {
                    val prodottoAggiornato = prodottoEsistente.copy(
                        nome = nuovoNome,
                        descrizione = nuovaDescrizione,
                        prezzo = nuovoPrezzo,
                        scorta = nuovaScorta,
                        categoria = nuovaCategoria
                    )

                    depositoProdotto.modificaProdotto(prodottoAggiornato)
                    _messaggio.value = "Prodotto aggiornato con successo!"
                    onCompletato(true)
                } else {
                    _messaggio.value = "Prodotto non trovato"
                    onCompletato(false)
                }
            } catch (e: Exception) {
                _messaggio.value = "Errore durante l'aggiornamento: ${e.message}"
                onCompletato(false)
            } finally {
                _caricamento.value = false
            }
        }
    }

    fun cercaProdotto(termine: String, onRisultato: (List<Prodotto>) -> Unit) {
        viewModelScope.launch {
            try {
                val risultati = depositoProdotto.cercaProdotti(termine)
                onRisultato(risultati)
            } catch (e: Exception) {
                onRisultato(emptyList())
            }
        }
    }

    fun ottieniProdottoPerCodice(codiceBarre: String, onRisultato: (Prodotto?) -> Unit) {
        viewModelScope.launch {
            try {
                val prodotto = depositoProdotto.trovaProdottoConCodice(codiceBarre)
                onRisultato(prodotto)
            } catch (e: Exception) {
                onRisultato(null)
            }
        }
    }

    fun eliminaProdotto(idProdotto: Long) {
        viewModelScope.launch {
            try {
                depositoProdotto.eliminaProdotto(idProdotto)
                _messaggio.value = "Prodotto eliminato"
            } catch (e: Exception) {
                _messaggio.value = "Errore: ${e.message}"
            }
        }
    }

    fun pulisciMessaggio() {
        _messaggio.value = null
    }
}