package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionenegozio.dati.entita.MetodoPagamento
import com.example.gestionenegozio.dati.entita.Prodotto
import com.example.gestionenegozio.dati.entita.Utente
import com.example.gestionenegozio.ui.gestore.VenditaGestore
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuovaVenditaSchermata(
    venditaGestore: VenditaGestore,
    utenteCorrente: Utente,
    onVenditaCompletata: () -> Unit,
    onApriScanner: () -> Unit,
    onIndietro: () -> Unit
) {
    val carrello by venditaGestore.carrello.collectAsState()
    val totale by venditaGestore.totale.collectAsState()
    val caricamento by venditaGestore.caricamento.collectAsState()
    val messaggio by venditaGestore.messaggio.collectAsState()

    var cercaProdotto by remember { mutableStateOf("") }
    var risultatiRicerca by remember { mutableStateOf<List<Prodotto>>(emptyList()) }
    var mostraDialogoPagamento by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuova Vendita") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = onApriScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scansiona")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = cercaProdotto,
                onValueChange = { termine ->
                    cercaProdotto = termine
                    if (termine.length > 2) {
                        venditaGestore.cercaProdottoPer(termine) { risultati ->
                            risultatiRicerca = risultati
                        }
                    } else {
                        risultatiRicerca = emptyList()
                    }
                },
                label = { Text("Cerca prodotto") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )

            if (risultatiRicerca.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        risultatiRicerca.take(5).forEach { prodotto ->
                            TextButton(
                                onClick = {
                                    venditaGestore.aggiungiAlCarrello(prodotto)
                                    cercaProdotto = ""
                                    risultatiRicerca = emptyList()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(prodotto.nome)
                                    Text(
                                        "€ ${String.format("%.2f", prodotto.prezzo)} - Scorta: ${prodotto.scorta}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (carrello.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Carrello",
                            style = MaterialTheme.typography.titleMedium
                        )

                        carrello.forEach { elemento ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(elemento.nomeProdotto)
                                    Text(
                                        "${elemento.quantita} x € ${String.format("%.2f", elemento.prezzoUno)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    "€ ${String.format("%.2f", elemento.prezzoTotale)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                IconButton(
                                    onClick = {
                                        venditaGestore.rimuoviDalCarrello(elemento.idProdotto)
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Rimuovi")
                                }
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "TOTALE:",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "€ ${String.format("%.2f", totale)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { mostraDialogoPagamento = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !caricamento
                        ) {
                            Text("Completa Vendita")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Carrello vuoto")
                        Text(
                            "Cerca e aggiungi prodotti per iniziare una vendita",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                    venditaGestore.pulisciMessaggio()
                }
            }
        }

        if (mostraDialogoPagamento) {
            DialogoPagamento(
                totale = totale,
                onConferma = { metodoPagamento ->
                    venditaGestore.completaVendita(utenteCorrente.id, metodoPagamento) {
                        onVenditaCompletata()
                    }
                    mostraDialogoPagamento = false
                },
                onAnnulla = {
                    mostraDialogoPagamento = false
                }
            )
        }
    }
}

@Composable
fun DialogoPagamento(
    totale: Double,
    onConferma: (MetodoPagamento) -> Unit,
    onAnnulla: () -> Unit
) {
    var metodoPagamento by remember { mutableStateOf(MetodoPagamento.CONTANTI) }

    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text("Completa Vendita") },
        text = {
            Column {
                Text("Totale: € ${String.format("%.2f", totale)}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Metodo di pagamento:")

                MetodoPagamento.values().forEach { metodo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = metodoPagamento == metodo,
                            onClick = { metodoPagamento = metodo }
                        )
                        Text(
                            text = when(metodo) {
                                MetodoPagamento.CONTANTI -> "Contanti"
                                MetodoPagamento.CARTA -> "Carta"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConferma(metodoPagamento) }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) {
                Text("Annulla")
            }
        }
    )
}