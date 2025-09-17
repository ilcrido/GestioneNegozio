package com.example.gestionenegozio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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
import com.example.gestionenegozio.notifiche.NotificaGuadagnoSerale
import com.example.gestionenegozio.notifiche.NotificaScorteMattutina

class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseNegozio
    private lateinit var depositoUtente: RepositoryUtente

    private val richiestaPermessoNotifiche = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concesso ->
        if (concesso) {
            avviaNotifiche()
        }
    }

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

        controllaEChiediPermessoNotifiche()

        setContent {
            TemaNegozio {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loginGestore = remember { LoginGestore(depositoUtente) }
                    val prodottoGestore = remember { ProdottoGestore(depositoProdotto) }
                    val venditaGestore = remember { VenditaGestore(depositoVendita, depositoProdotto, depositoUtente) }
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

    private fun controllaEChiediPermessoNotifiche() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    avviaNotifiche()
                }
                else -> {
                    richiestaPermessoNotifiche.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            avviaNotifiche()
        }
    }

    private fun avviaNotifiche() {
        NotificaGuadagnoSerale(this)
        NotificaScorteMattutina(this)
    }
}