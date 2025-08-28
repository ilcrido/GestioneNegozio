package com.example.gestionenegozio.dati.repository

import com.example.gestionenegozio.dati.dao.UtenteDao
import com.example.gestionenegozio.dati.entita.Utente
import com.example.gestionenegozio.dati.entita.RuoloUtente
import kotlinx.coroutines.flow.Flow

class RepositoryUtente(private val utenteDao: UtenteDao) {

    suspend fun accedi(nomeUtente: String, password: String): Utente? {
        return utenteDao.accedi(nomeUtente, password)
    }

    fun ottieniTuttiUtenti(): Flow<List<Utente>> {
        return utenteDao.ottieniTuttiUtentiAttivi()
    }

    fun ottieniDipendenti(): Flow<List<Utente>> {
        return utenteDao.ottieniUtentiPerRuolo(RuoloUtente.DIPENDENTE)
    }

    suspend fun creaUtente(nomeUtente: String, password: String, nomeCompleto: String, ruolo: RuoloUtente): Long {
        val nuovoUtente = Utente(
            nomeUtente = nomeUtente,
            password = password,
            nomeCompleto = nomeCompleto,
            ruolo = ruolo
        )
        return utenteDao.inserisciUtente(nuovoUtente)
    }

    suspend fun aggiornaUtente(utente: Utente) {
        utenteDao.aggiornaUtente(utente)
    }

    suspend fun eliminaUtente(idUtente: Long) {
        val quantiAdmin = utenteDao.contaAdmin()
        if (quantiAdmin > 1) {
            utenteDao.disattivaUtente(idUtente)
        }
    }

    suspend fun ciSonoAdmin(): Boolean {
        return utenteDao.contaAdmin() > 0
    }
}