package com.example.gestionenegozio.ui.gestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionenegozio.dati.repository.RepositoryUtente
import com.example.gestionenegozio.dati.entita.Utente
import com.example.gestionenegozio.dati.entita.RuoloUtente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DipendenteGestore(private val depositoUtente: RepositoryUtente) : ViewModel() {

    private val _dipendenti = MutableStateFlow<List<Utente>>(emptyList())
    val dipendenti: StateFlow<List<Utente>> = _dipendenti.asStateFlow()

    private val _caricamento = MutableStateFlow(false)
    val caricamento: StateFlow<Boolean> = _caricamento.asStateFlow()

    private val _messaggio = MutableStateFlow<String?>(null)
    val messaggio: StateFlow<String?> = _messaggio.asStateFlow()

    init {
        caricaDipendenti()
    }

    fun caricaDipendenti() {
        viewModelScope.launch {
            depositoUtente.ottieniTuttiUtenti().collect { lista ->
                _dipendenti.value = lista
            }
        }
    }

    fun aggiungiDipendente(
        nomeUtente: String,
        password: String,
        nomeCompleto: String,
        ruolo: RuoloUtente,
        onCompletato: (Boolean) -> Unit
    ) {
        _caricamento.value = true
        _messaggio.value = null

        viewModelScope.launch {
            try {
                val utenteEsistente = _dipendenti.value.find { it.nomeUtente == nomeUtente }

                if (utenteEsistente != null) {
                    _messaggio.value = "Nome utente già esistente"
                    onCompletato(false)
                } else {
                    val id = depositoUtente.creaUtente(nomeUtente, password, nomeCompleto, ruolo)

                    if (id > 0) {
                        _messaggio.value = "Dipendente aggiunto con successo!"
                        onCompletato(true)
                    } else {
                        _messaggio.value = "Errore durante il salvataggio"
                        onCompletato(false)
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

    fun eliminaDipendente(idUtente: Long, onCompletato: () -> Unit) {
        _caricamento.value = true

        viewModelScope.launch {
            try {
                depositoUtente.eliminaUtente(idUtente)
                _messaggio.value = "Dipendente eliminato"
                onCompletato()
            } catch (e: Exception) {
                _messaggio.value = "Errore durante l'eliminazione: ${e.message}"
            } finally {
                _caricamento.value = false
            }
        }
    }

    fun pulisciMessaggio() {
        _messaggio.value = null
    }
}