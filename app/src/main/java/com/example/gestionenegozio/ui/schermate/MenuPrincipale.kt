package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gestionenegozio.dati.entita.Utente
import com.example.gestionenegozio.dati.entita.RuoloUtente
import com.example.gestionenegozio.interfaccia.schermate.ProdottiSchermata
import com.example.gestionenegozio.ui.gestore.ProdottoGestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipale(
    utenteCorrente: Utente,
    prodottoGestore: ProdottoGestore,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Gestione Negozio")
                },
                actions = {
                    Text(
                        text = "Ciao ${utenteCorrente.nomeCompleto}",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavigazione(
                navController = navController,
                utenteCorrente = utenteCorrente
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "prodotti",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("prodotti") {
                ProdottiSchermata(
                    prodottoGestore = prodottoGestore,
                    onApriScanner = {
                        navController.navigate("scanner")
                    },
                    onAggiungiProdotto = {
                        navController.navigate("aggiungi_prodotto")
                    }
                )
            }
            composable("scanner") {
                ScannerSchermata(
                    onCodiceTrovato = { codice ->
                        navController.navigate("aggiungi_prodotto/$codice") {
                            popUpTo("scanner") { inclusive = true }
                        }
                    },
                    onIndietro = {
                        navController.popBackStack()
                    }
                )
            }
            composable("aggiungi_prodotto") {
                AggiungiProdottoSchermata(
                    codiceBarre = null,
                    prodottoGestore = prodottoGestore,
                    onSalvato = {
                        navController.popBackStack()
                    },
                    onIndietro = {
                        navController.popBackStack()
                    }
                )
            }
            composable("aggiungi_prodotto/{codice}") { backStackEntry ->
                val codice = backStackEntry.arguments?.getString("codice")
                AggiungiProdottoSchermata(
                    codiceBarre = codice,
                    prodottoGestore = prodottoGestore,
                    onSalvato = {
                        navController.popBackStack("prodotti", inclusive = false)
                    },
                    onIndietro = {
                        navController.popBackStack()
                    }
                )
            }
            composable("vendite") {
                VenditeSchermata(
                    onNuovaVendita = {
                        // Per ora mostriamo un messaggio
                    }
                )
            }
            if (utenteCorrente.ruolo == RuoloUtente.ADMIN) {
                composable("dipendenti") {
                    DipendentiSchermata(
                        onAggiungiDipendente = {
                            // Per ora mostriamo un messaggio
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BarraNavigazione(
    navController: NavHostController,
    utenteCorrente: Utente
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinazioneCorrente = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Store, contentDescription = null) },
            label = { Text("Prodotti") },
            selected = destinazioneCorrente == "prodotti",
            onClick = {
                navController.navigate("prodotti") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Vendite") },
            selected = destinazioneCorrente == "vendite",
            onClick = {
                navController.navigate("vendite") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )

        if (utenteCorrente.ruolo == RuoloUtente.ADMIN) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Dipendenti") },
                selected = destinazioneCorrente == "dipendenti",
                onClick = {
                    navController.navigate("dipendenti") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}