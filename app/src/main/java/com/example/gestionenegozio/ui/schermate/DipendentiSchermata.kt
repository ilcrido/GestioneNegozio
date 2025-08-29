package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionenegozio.dati.entita.RuoloUtente
import com.example.gestionenegozio.ui.gestore.DipendenteGestore
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DipendentiSchermata(
    dipendenteGestore: DipendenteGestore,
    onAggiungiDipendente: () -> Unit = {}
) {
    val dipendenti by dipendenteGestore.dipendenti.collectAsState()
    val messaggio by dipendenteGestore.messaggio.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dipendenti",
                style = MaterialTheme.typography.headlineMedium
            )

            FloatingActionButton(
                onClick = onAggiungiDipendente
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi dipendente")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Sezione Admin",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Solo gli amministratori possono vedere questa sezione",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (dipendenti.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nessun dipendente")
                    Text(
                        "Aggiungi il primo dipendente usando il pulsante +",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dipendenti) { dipendente ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dipendente.nomeCompleto,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "@${dipendente.nomeUtente}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = when(dipendente.ruolo) {
                                        RuoloUtente.ADMIN -> "Amministratore"
                                        RuoloUtente.DIPENDENTE -> "Dipendente"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (dipendente.ruolo == RuoloUtente.ADMIN) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            if (dipendente.ruolo != RuoloUtente.ADMIN || dipendenti.count { it.ruolo == RuoloUtente.ADMIN } > 1) {
                                IconButton(
                                    onClick = {
                                        dipendenteGestore.eliminaDipendente(dipendente.id) {
                                            // Dipendente eliminato
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Elimina")
                                }
                            }
                        }
                    }
                }
            }
        }

        messaggio?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            LaunchedEffect(msg) {
                delay(3000)
                dipendenteGestore.pulisciMessaggio()
            }
        }
    }
}