package com.example.gestionenegozio.ui.gestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionenegozio.dati.repository.RepositoryUtente
import com.example.gestionenegozio.dati.entita.Utente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginGestore(private val depositoUtente: RepositoryUtente) : ViewModel() {

    private val _statoLogin = MutableStateFlow(StatoLogin())
    val statoLogin: StateFlow<StatoLogin> = _statoLogin.asStateFlow()

    private val _utenteCorrente = MutableStateFlow<Utente?>(null)
    val utenteCorrente: StateFlow<Utente?> = _utenteCorrente.asStateFlow()

    fun aggiornaNomeUtente(nome: String) {
        _statoLogin.value = _statoLogin.value.copy(nomeUtente = nome, errore = null)
    }

    fun aggiornaPassword(password: String) {
        _statoLogin.value = _statoLogin.value.copy(password = password, errore = null)
    }

    fun accedi() {
        val stato = _statoLogin.value

        if (stato.nomeUtente.isBlank() || stato.password.isBlank()) {
            _statoLogin.value = stato.copy(errore = "Inserisci nome utente e password")
            return
        }

        _statoLogin.value = stato.copy(caricamento = true, errore = null)

        viewModelScope.launch {
            try {
                val utente = depositoUtente.accedi(stato.nomeUtente, stato.password)

                if (utente != null) {
                    _utenteCorrente.value = utente
                    _statoLogin.value = stato.copy(
                        caricamento = false,
                        loginRiuscito = true
                    )
                } else {
                    _statoLogin.value = stato.copy(
                        caricamento = false,
                        errore = "Nome utente o password sbagliati"
                    )
                }
            } catch (e: Exception) {
                _statoLogin.value = stato.copy(
                    caricamento = false,
                    errore = "Errore durante l'accesso: ${e.message}"
                )
            }
        }
    }

    fun pulisciErrore() {
        _statoLogin.value = _statoLogin.value.copy(errore = null)
    }

    fun logout() {
        _utenteCorrente.value = null
        _statoLogin.value = StatoLogin()
    }
}

data class StatoLogin(
    val nomeUtente: String = "",
    val password: String = "",
    val caricamento: Boolean = false,
    val loginRiuscito: Boolean = false,
    val errore: String? = null
)