package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.gestionenegozio.ui.gestore.ProdottoGestore
import com.example.gestionenegozio.dati.entita.Prodotto

enum class FiltriProdotti {
    TUTTI,
    SCORTA_BASSA,
    SCORTA_ESAURITA,
    PREZZO_CRESCENTE,
    PREZZO_DECRESCENTE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdottiSchermata(
    prodottoGestore: ProdottoGestore,
    onApriScanner: () -> Unit = {},
    onAggiungiProdotto: () -> Unit = {},
    onModificaProdotto: (Long) -> Unit = {}
) {
    val prodotti by prodottoGestore.prodotti.collectAsState()
    var prodottoSelezionato by remember { mutableStateOf<Prodotto?>(null) }
    var filtroSelezionato by remember { mutableStateOf(FiltriProdotti.TUTTI) }
    var sogliaScocia by remember { mutableStateOf(10) }
    var mostraFiltri by remember { mutableStateOf(false) }

    // Applica i filtri
    val prodottiFiltrati = remember(prodotti, filtroSelezionato, sogliaScocia) {
        when (filtroSelezionato) {
            FiltriProdotti.TUTTI -> prodotti
            FiltriProdotti.SCORTA_BASSA -> prodotti.filter { it.scorta <= sogliaScocia }
            FiltriProdotti.SCORTA_ESAURITA -> prodotti.filter { it.scorta == 0 }
            FiltriProdotti.PREZZO_CRESCENTE -> prodotti.sortedBy { it.prezzo }
            FiltriProdotti.PREZZO_DECRESCENTE -> prodotti.sortedByDescending { it.prezzo }
        }
    }

    LaunchedEffect(Unit) {
        prodottoGestore.caricaProdotti()
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
                text = "Prodotti",
                style = MaterialTheme.typography.headlineMedium
            )

            Row {
                IconButton(onClick = { mostraFiltri = !mostraFiltri }) {
                    Icon(
                        if (mostraFiltri) Icons.Default.FilterListOff else Icons.Default.FilterList,
                        contentDescription = "Filtri",
                        tint = if (filtroSelezionato != FiltriProdotti.TUTTI)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                FloatingActionButton(
                    onClick = onApriScanner,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scansiona")
                }

                FloatingActionButton(
                    onClick = onAggiungiProdotto
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi prodotto")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pannello filtri
        AnimatedVisibility(
            visible = mostraFiltri,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filtri",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (filtroSelezionato != FiltriProdotti.TUTTI) {
                            TextButton(onClick = { filtroSelezionato = FiltriProdotti.TUTTI }) {
                                Text("Cancella filtri")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtri rapidi
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FiltriProdotti.values()) { filtro ->
                            FilterChip(
                                onClick = { filtroSelezionato = filtro },
                                label = {
                                    Text(
                                        when(filtro) {
                                            FiltriProdotti.TUTTI -> "Tutti (${prodotti.size})"
                                            FiltriProdotti.SCORTA_BASSA -> "Scorta bassa (${prodotti.count { it.scorta <= sogliaScocia }})"
                                            FiltriProdotti.SCORTA_ESAURITA -> "Esauriti (${prodotti.count { it.scorta == 0 }})"
                                            FiltriProdotti.PREZZO_CRESCENTE -> "Prezzo crescente"
                                            FiltriProdotti.PREZZO_DECRESCENTE -> "Prezzo decrescente"
                                        }
                                    )
                                },
                                selected = filtroSelezionato == filtro,
                                leadingIcon = if (filtroSelezionato == filtro) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }

                    // Impostazione soglia scorta (solo se filtro scorta bassa è selezionato)
                    if (filtroSelezionato == FiltriProdotti.SCORTA_BASSA) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Soglia scorta bassa:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (sogliaScocia > 1) sogliaScocia-- }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuisci")
                                }
                                Text(
                                    "$sogliaScocia",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                IconButton(
                                    onClick = { sogliaScocia++ }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumenta")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (prodottiFiltrati.isEmpty() && prodotti.isNotEmpty()) {
            // Nessun prodotto corrisponde ai filtri
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Nessun prodotto corrispondente ai filtri",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Prova a modificare i filtri o ad aggiungere nuovi prodotti",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (prodotti.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nessun prodotto")
                    Text(
                        "Aggiungi il primo prodotto usando lo scanner o il pulsante +",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(prodottiFiltrati) { prodotto ->
                    Card(
                        onClick = { prodottoSelezionato = prodotto },
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
                                    text = prodotto.nome,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "€ ${String.format("%.2f", prodotto.prezzo)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Scorta: ${prodotto.scorta}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (prodotto.scorta <= 10) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                prodotto.codiceBarre?.let { codice ->
                                    if (codice.isNotBlank()) {
                                        Text(
                                            text = "Codice: $codice",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "Tocca per vedere dettagli",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    prodottoGestore.eliminaProdotto(prodotto.id)
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina prodotto")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog informazioni prodotto
    prodottoSelezionato?.let { prodotto ->
        DialogInfoProdotto(
            prodotto = prodotto,
            onModifica = {
                prodottoSelezionato = null
                onModificaProdotto(prodotto.id)
            },
            onElimina = {
                prodottoGestore.eliminaProdotto(prodotto.id)
                prodottoSelezionato = null
            },
            onChiudi = {
                prodottoSelezionato = null
            }
        )
    }
}

@Composable
fun DialogInfoProdotto(
    prodotto: Prodotto,
    onModifica: () -> Unit,
    onElimina: () -> Unit,
    onChiudi: () -> Unit
) {
    Dialog(onDismissRequest = onChiudi) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dettagli Prodotto",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onChiudi) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Informazioni principali
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoRiga(label = "Nome:", valore = prodotto.nome)

                        InfoRiga(
                            label = "Prezzo:",
                            valore = "€ ${String.format("%.2f", prodotto.prezzo)}"
                        )

                        InfoRiga(
                            label = "Scorta:",
                            valore = "${prodotto.scorta} unità",
                            isError = prodotto.scorta <= 10
                        )

                        prodotto.codiceBarre?.let { codice ->
                            if (codice.isNotBlank()) {
                                InfoRiga(label = "Codice a barre:", valore = codice)
                            }
                        }

                        prodotto.descrizione?.let { descrizione ->
                            if (descrizione.isNotBlank()) {
                                InfoRiga(label = "Descrizione:", valore = descrizione)
                            }
                        }

                        prodotto.categoria?.let { categoria ->
                            if (categoria.isNotBlank()) {
                                InfoRiga(label = "Categoria:", valore = categoria)
                            }
                        }
                    }
                }

                // Avviso scorta bassa
                if (prodotto.scorta <= 10) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Avviso",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scorta in esaurimento!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsanti azioni
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onElimina,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Elimina")
                    }

                    Button(
                        onClick = onModifica,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Modifica")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pulsante chiudi
                OutlinedButton(
                    onClick = onChiudi,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chiudi")
                }
            }
        }
    }
}

@Composable
private fun InfoRiga(
    label: String,
    valore: String,
    isError: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valore,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
}