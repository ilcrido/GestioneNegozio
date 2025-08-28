package com.example.gestionenegozio.ui.schermate

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerSchermata(
    onCodiceTrovato: (String) -> Unit,
    onIndietro: () -> Unit
) {
    val context = LocalContext.current
    var flashAttivo by remember { mutableStateOf(false) }
    var permessoAccordato by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val richiestaPermesso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordato ->
        permessoAccordato = accordato
    }

    LaunchedEffect(Unit) {
        if (!permessoAccordato) {
            richiestaPermesso.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scansiona Codice") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { flashAttivo = !flashAttivo }) {
                        Icon(
                            if (flashAttivo) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (flashAttivo) "Spegni flash" else "Accendi flash"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            permessoAccordato -> {
                ScannerFotocamera(
                    modifier = Modifier.padding(paddingValues),
                    onCodiceTrovato = onCodiceTrovato,
                    flashAttivo = flashAttivo
                )
            }

            !permessoAccordato -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Serve il permesso della fotocamera per scansionare i codici a barre",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { richiestaPermesso.launch(Manifest.permission.CAMERA) }) {
                        Text("Concedi permesso")
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Richiedo permesso fotocamera...")
                }
            }
        }
    }
}

@Composable
fun ScannerFotocamera(
    modifier: Modifier = Modifier,
    onCodiceTrovato: (String) -> Unit,
    flashAttivo: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanner = remember { BarcodeScanning.getClient() }
    var codiciTrovati by remember { mutableStateOf(setOf<String>()) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor) { imageProxy ->
                                analizzaImmagine(imageProxy, scanner) { codice ->
                                    if (!codiciTrovati.contains(codice)) {
                                        codiciTrovati = codiciTrovati + codice
                                        onCodiceTrovato(codice)
                                    }
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )

                        camera.cameraControl.enableTorch(flashAttivo)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Inquadra un codice a barre",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Il codice verrà rilevato automaticamente",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun analizzaImmagine(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onCodiceTrovato: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { valore ->
                        onCodiceTrovato(valore)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}