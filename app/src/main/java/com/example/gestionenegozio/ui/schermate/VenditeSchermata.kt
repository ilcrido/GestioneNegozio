package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.gestionenegozio.ui.gestore.VenditaGestore
import com.example.gestionenegozio.ui.gestore.VenditaConDettagli
import com.example.gestionenegozio.dati.entita.MetodoPagamento
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenditeSchermata(
    venditaGestore: VenditaGestore,
    onNuovaVendita: () -> Unit = {}
) {
    val venditeRecenti by venditaGestore.venditeRecenti.collectAsState()
    val statisticheOggi by venditaGestore.statisticheOggi.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    var venditaSelezionata by remember { mutableStateOf<VenditaConDettagli?>(null) }

    LaunchedEffect(Unit) {
        venditaGestore.caricaVenditeRecenti()
    }

    LaunchedEffect(venditeRecenti) {
        println("DEBUG UI: Ricevute ${venditeRecenti.size} vendite nella UI")
    }

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
                text = "Vendite",
                style = MaterialTheme.typography.headlineMedium
            )

            FloatingActionButton(
                onClick = onNuovaVendita
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuova vendita")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STATISTICHE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Vendite oggi")
                    Text(
                        "€ ${String.format("%.2f", statisticheOggi?.incassoOggi ?: 0.0)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Vendite mese")
                    Text(
                        "€ ${String.format("%.2f", statisticheOggi?.incassoMese ?: 0.0)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STORICO VENDITE
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Storico vendite",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (venditeRecenti.isEmpty()) {
                    Text(
                        "Nessuna vendita registrata",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(venditeRecenti) { vendita ->
                            Card(
                                onClick = { venditaSelezionata = vendita },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    // Prima riga: ID vendita e data
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Vendita #${vendita.vendita.id}",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = dateFormat.format(Date(vendita.vendita.creatoIl)),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Seconda riga: Dipendente
                                    Text(
                                        text = "Dipendente: ${vendita.nomeDipendente}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Terza riga: Metodo pagamento e totale
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when(vendita.vendita.metodoPagamento) {
                                                MetodoPagamento.CONTANTI -> "Contanti"
                                                MetodoPagamento.CARTA -> "Carta"
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "€ ${String.format("%.2f", vendita.vendita.totale)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Indicazione che è cliccabile
                                    Text(
                                        text = "Tocca per vedere i dettagli",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog dettagli vendita
    venditaSelezionata?.let { vendita ->
        Dialog(onDismissRequest = { venditaSelezionata = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header del dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dettagli Vendita #${vendita.vendita.id}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { venditaSelezionata = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Chiudi")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informazioni generali
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Data: ${dateFormat.format(Date(vendita.vendita.creatoIl))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Dipendente: ${vendita.nomeDipendente}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Pagamento: ${when(vendita.vendita.metodoPagamento) {
                                    MetodoPagamento.CONTANTI -> "Contanti"
                                    MetodoPagamento.CARTA -> "Carta"
                                }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Prodotti venduti:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista prodotti venduti
                    if (vendita.elementi.isEmpty()) {
                        Text(
                            "Nessun dettaglio disponibile",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f, false)
                        ) {
                            items(vendita.elementi) { elemento ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                elemento.nome,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "${elemento.quantita} x € ${String.format("%.2f", elemento.prezzoUnitario)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            "€ ${String.format("%.2f", elemento.prezzoTotale)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Totale
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "TOTALE:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "€ ${String.format("%.2f", vendita.vendita.totale)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}