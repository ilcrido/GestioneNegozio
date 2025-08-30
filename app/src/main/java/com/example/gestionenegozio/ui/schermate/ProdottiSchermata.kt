package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionenegozio.ui.gestore.ProdottoGestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdottiSchermata(
    prodottoGestore: ProdottoGestore,
    onApriScanner: () -> Unit = {},
    onAggiungiProdotto: () -> Unit = {}
) {
    val prodotti by prodottoGestore.prodotti.collectAsState()
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
                FloatingActionButton(
                    onClick = onApriScanner,
                    modifier = Modifier.padding(end = 8.dp)
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

        if (prodotti.isEmpty()) {
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
                items(prodotti) { prodotto ->
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
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                prodotto.codiceBarre?.let { codice ->
                                    Text(
                                        text = "Codice: $codice",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
}