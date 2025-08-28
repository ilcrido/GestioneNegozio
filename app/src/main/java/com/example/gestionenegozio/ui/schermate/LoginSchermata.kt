package com.example.gestionenegozio.ui.schermate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import com.example.gestionenegozio.ui.gestore.LoginGestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSchermata(
    loginGestore: LoginGestore,
    onLoginRiuscito: () -> Unit
) {
    val statoLogin by loginGestore.statoLogin.collectAsState()
    var passwordVisibile by remember { mutableStateOf(false) }

    LaunchedEffect(statoLogin.loginRiuscito) {
        if (statoLogin.loginRiuscito) {
            onLoginRiuscito()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Gestione Negozio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Accedi al sistema",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = statoLogin.nomeUtente,
                    onValueChange = loginGestore::aggiornaNomeUtente,
                    label = { Text("Nome utente") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !statoLogin.caricamento,
                    isError = statoLogin.errore != null
                )

                OutlinedTextField(
                    value = statoLogin.password,
                    onValueChange = loginGestore::aggiornaPassword,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !statoLogin.caricamento,
                    isError = statoLogin.errore != null,
                    visualTransformation = if (passwordVisibile) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisibile = !passwordVisibile }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = if (passwordVisibile) {
                                    "Nascondi password"
                                } else {
                                    "Mostra password"
                                }
                            )
                        }
                    }
                )

                if (statoLogin.errore != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = statoLogin.errore!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = loginGestore::accedi,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !statoLogin.caricamento
                ) {
                    if (statoLogin.caricamento) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Accedi")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Admin di default: admin / admin123",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}