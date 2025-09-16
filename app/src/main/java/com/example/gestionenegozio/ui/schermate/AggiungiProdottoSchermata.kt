package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gestionenegozio.ui.gestore.ProdottoGestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AggiungiProdottoSchermata(
    codiceBarre: String? = null,
    idProdotto: Long? = null,
    prodottoGestore: ProdottoGestore,
    onSalvato: () -> Unit,
    onIndietro: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descrizione by remember { mutableStateOf("") }
    var prezzo by remember { mutableStateOf("") }
    var scorta by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var errore by remember { mutableStateOf<String?>(null) }
    var prodottoEsistente by remember { mutableStateOf(false) }
    var prodottoTrovato by remember { mutableStateOf<String?>(null) }

    val caricamento by prodottoGestore.caricamento.collectAsState()
    val messaggio by prodottoGestore.messaggio.collectAsState()

    val codiceBarreFinale = codiceBarre ?: ""
    val modalitaModifica = idProdotto != null

    // Auto-compila i campi se stiamo modificando un prodotto esistente
    LaunchedEffect(idProdotto) {
        if (idProdotto != null) {
            prodottoGestore.ottieniProdottoPerId(idProdotto) { prodotto ->
                if (prodotto != null) {
                    nome = prodotto.nome
                    descrizione = prodotto.descrizione ?: ""
                    prezzo = prodotto.prezzo.toString()
                    scorta = prodotto.scorta.toString()
                    categoria = prodotto.categoria ?: ""
                    prodottoEsistente = true
                    prodottoTrovato = "Modifica prodotto esistente"
                }
            }
        }
    }

    // Auto-compila i campi se il prodotto esiste già (tramite codice a barre)
    LaunchedEffect(codiceBarreFinale) {
        if (codiceBarreFinale.isNotBlank() && idProdotto == null) {
            prodottoGestore.ottieniProdottoPerCodice(codiceBarreFinale) { prodotto ->
                if (prodotto != null) {
                    nome = prodotto.nome
                    descrizione = prodotto.descrizione ?: ""
                    prezzo = prodotto.prezzo.toString()
                    scorta = prodotto.scorta.toString()
                    categoria = prodotto.categoria ?: ""
                    prodottoEsistente = true
                    prodottoTrovato = "Prodotto esistente trovato! Puoi modificare i dati se necessario."
                } else {
                    prodottoEsistente = false
                    prodottoTrovato = "Nuovo prodotto - compila tutti i campi."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (modalitaModifica) "Modifica Prodotto" else if (prodottoEsistente) "Modifica Prodotto" else "Nuovo Prodotto")
                },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (nome.isBlank() || prezzo.isBlank() || scorta.isBlank()) {
                                errore = "Compila tutti i campi obbligatori"
                                return@IconButton
                            }

                            val prezzoDouble = prezzo.toDoubleOrNull()
                            val scortaInt = scorta.toIntOrNull()

                            if (prezzoDouble == null || prezzoDouble <= 0) {
                                errore = "Il prezzo deve essere un numero maggiore di zero"
                                return@IconButton
                            }

                            if (scortaInt == null || scortaInt < 0) {
                                errore = "La scorta deve essere un numero non negativo"
                                return@IconButton
                            }

                            if (modalitaModifica) {
                                // Modalità modifica tramite ID
                                prodottoGestore.aggiornaProdottoPerId(
                                    idProdotto = idProdotto!!,
                                    nuovoNome = nome.trim(),
                                    nuovaDescrizione = descrizione.trim().takeIf { it.isNotBlank() },
                                    nuovoPrezzo = prezzoDouble,
                                    nuovaScorta = scortaInt,
                                    nuovaCategoria = categoria.trim().takeIf { it.isNotBlank() }
                                ) { successo ->
                                    if (successo) {
                                        onSalvato()
                                    }
                                }
                            } else if (prodottoEsistente) {
                                // Aggiorna prodotto esistente tramite codice a barre
                                prodottoGestore.aggiornaProdottoPerCodice(
                                    codiceBarre = codiceBarreFinale,
                                    nuovoNome = nome.trim(),
                                    nuovaDescrizione = descrizione.trim().takeIf { it.isNotBlank() },
                                    nuovoPrezzo = prezzoDouble,
                                    nuovaScorta = scortaInt,
                                    nuovaCategoria = categoria.trim().takeIf { it.isNotBlank() }
                                ) { successo ->
                                    if (successo) {
                                        onSalvato()
                                    }
                                }
                            } else {
                                // Crea nuovo prodotto
                                prodottoGestore.aggiungiProdotto(
                                    NuovoProdotto(
                                        nome = nome.trim(),
                                        descrizione = descrizione.trim().takeIf { it.isNotBlank() },
                                        codiceBarre = codiceBarreFinale.takeIf { it.isNotBlank() },
                                        prezzo = prezzoDouble,
                                        scorta = scortaInt,
                                        categoria = categoria.trim().takeIf { it.isNotBlank() }
                                    )
                                ) { successo ->
                                    if (successo) {
                                        onSalvato()
                                    }
                                }
                            }
                        },
                        enabled = !caricamento
                    ) {
                        if (caricamento) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
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
            if (codiceBarreFinale.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (prodottoEsistente) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Codice a barre scansionato:",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (prodottoEsistente) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Text(
                            codiceBarreFinale,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (prodottoEsistente) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )

                        prodottoTrovato?.let { status ->
                            Text(
                                status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (prodottoEsistente) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it; errore = null },
                label = { Text("Nome prodotto *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento,
                isError = errore != null && nome.isBlank()
            )

            OutlinedTextField(
                value = descrizione,
                onValueChange = { descrizione = it; errore = null },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento,
                maxLines = 3
            )

            OutlinedTextField(
                value = prezzo,
                onValueChange = { prezzo = it; errore = null },
                label = { Text("Prezzo (€) *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errore != null && (prezzo.isBlank() || prezzo.toDoubleOrNull() == null)
            )

            OutlinedTextField(
                value = scorta,
                onValueChange = { scorta = it; errore = null },
                label = { Text("Quantità in magazzino *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errore != null && (scorta.isBlank() || scorta.toIntOrNull() == null)
            )

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it; errore = null },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !caricamento
            )

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

            Text(
                "* Campi obbligatori",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class NuovoProdotto(
    val nome: String,
    val descrizione: String?,
    val codiceBarre: String?,
    val prezzo: Double,
    val scorta: Int,
    val categoria: String?
)