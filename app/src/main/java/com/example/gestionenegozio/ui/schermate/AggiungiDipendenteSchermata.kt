package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionenegozio.dati.entita.RuoloUtente
import com.example.gestionenegozio.ui.gestore.DipendenteGestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AggiungiDipendenteSchermata(
    dipendenteGestore: DipendenteGestore,
    onSalvato: () -> Unit,
    onIndietro: () -> Unit
) {
    var nomeUtente by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nomeCompleto by remember { mutableStateOf("") }
    var ruolo by remember { mutableStateOf(RuoloUtente.DIPENDENTE) }
    var errore by remember { mutableStateOf<String?>(null) }

    val caricamento by dipendenteGestore.caricamento.collectAsState()
    val messaggio by dipendenteGestore.messaggio.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuovo Dipendente") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (nomeUtente.isBlank() || password.isBlank() || nomeCompleto.isBlank()) {
                                errore = "Compila tutti i campi"
                                return@IconButton
                            }

                            if (password.length < 4) {
                                errore = "La password deve essere almeno di 4 caratteri"
                                return@IconButton
                            }

                            dipendenteGestore.aggiungiDipendente(
                                nomeUtente = nomeUtente.trim(),
                                password = password,
                                nomeCompleto = nomeCompleto.trim(),
                                ruolo = ruolo
                            ) { successo ->
                                if (successo) {
                                    onSalvato()
                                }
                            }
                        },
                        enabled = !caricamento
                    ) {
                        if (caricamento) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Salva")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nomeUtente,
                onValueChange = { nomeUtente = it; errore = null },
                label = { Text("Nome utente") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errore = null },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento
            )

            OutlinedTextField(
                value = nomeCompleto,
                onValueChange = { nomeCompleto = it; errore = null },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento
            )

            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Ruolo:", style = MaterialTheme.typography.labelMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = ruolo == RuoloUtente.DIPENDENTE,
                            onClick = { ruolo = RuoloUtente.DIPENDENTE }
                        )
                        Text("Dipendente")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = ruolo == RuoloUtente.ADMIN,
                            onClick = { ruolo = RuoloUtente.ADMIN }
                        )
                        Text("Amministratore")
                    }
                }
            }

            if (errore != null || messaggio != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (errore != null) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Text(
                        text = errore ?: messaggio ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = if (errore != null) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }
    }
}