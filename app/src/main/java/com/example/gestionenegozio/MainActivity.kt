package com.example.gestionenegozio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gestionenegozio.dati.database.DatabaseNegozio
import com.example.gestionenegozio.dati.repository.RepositoryUtente
import com.example.gestionenegozio.dati.repository.RepositoryProdotto
import com.example.gestionenegozio.dati.repository.RepositoryVendita
import com.example.gestionenegozio.ui.gestore.LoginGestore
import com.example.gestionenegozio.ui.gestore.ProdottoGestore
import com.example.gestionenegozio.ui.gestore.VenditaGestore
import com.example.gestionenegozio.ui.gestore.DipendenteGestore
import com.example.gestionenegozio.ui.schermate.LoginSchermata
import com.example.gestionenegozio.ui.schermate.MenuPrincipale
import com.example.gestionenegozio.ui.theme.TemaNegozio

class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseNegozio
    private lateinit var depositoUtente: RepositoryUtente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = DatabaseNegozio.ottieniDatabase(this)
        depositoUtente = RepositoryUtente(database.utenteDao())
        val depositoProdotto = RepositoryProdotto(database.prodottoDao())
        val depositoVendita = RepositoryVendita(
            database.venditaDao(),
            database.elementoVenditaDao(),
            database.prodottoDao()
        )

        setContent {
            TemaNegozio {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loginGestore = remember { LoginGestore(depositoUtente) }
                    val prodottoGestore = remember { ProdottoGestore(depositoProdotto) }
                    val venditaGestore = remember { VenditaGestore(depositoVendita, depositoProdotto) }
                    val dipendenteGestore = remember { DipendenteGestore(depositoUtente) }

                    val utenteCorrente by loginGestore.utenteCorrente.collectAsState()

                    if (utenteCorrente == null) {
                        LoginSchermata(
                            loginGestore = loginGestore,
                            onLoginRiuscito = { }
                        )
                    } else {
                        MenuPrincipale(
                            utenteCorrente = utenteCorrente!!,
                            prodottoGestore = prodottoGestore,
                            venditaGestore = venditaGestore,
                            dipendenteGestore = dipendenteGestore,
                            onLogout = {
                                loginGestore.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}