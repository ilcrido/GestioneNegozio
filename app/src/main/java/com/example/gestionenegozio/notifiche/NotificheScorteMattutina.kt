package com.example.gestionenegozio.notifiche

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.gestionenegozio.R
import com.example.gestionenegozio.dati.repository.RepositoryProdotto
import com.example.gestionenegozio.dati.database.DatabaseNegozio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class NotificaScorteMattutina(private val context: Context) {

    companion object {
        const val CANALE_SCORTE = "scorte_channel"
        const val NOTIFICA_SCORTE_ID = 1004
        const val SOGLIA_DEFAULT = 10
    }

    init {
        creaCanaleNotifica()
        programmaNotificaMattutina()
    }

    private fun creaCanaleNotifica() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canale = NotificationChannel(
                CANALE_SCORTE,
                "Controllo Scorte",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Allarmi per prodotti sotto soglia"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(canale)
        }
    }

    fun programmaNotificaMattutina() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val scorteRequest = OneTimeWorkRequestBuilder<ControlloScorteWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "controllo_scorte_mattino",
            ExistingWorkPolicy.REPLACE,
            scorteRequest
        )
    }
}

class ControlloScorteWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = DatabaseNegozio.ottieniDatabase(applicationContext)
            val repositoryProdotto = RepositoryProdotto(database.prodottoDao())

            val tuttiProdotti = repositoryProdotto.ottieniTuttiProdotti().first()
            val prodottiEsauriti = tuttiProdotti.filter { it.scorta == 0 }
            val prodottiScorteBasse = tuttiProdotti.filter { it.scorta in 1..NotificaScorteMattutina.SOGLIA_DEFAULT }

            if (prodottiEsauriti.isNotEmpty() || prodottiScorteBasse.isNotEmpty()) {
                val messaggio = "Ci sono prodotti sotto la soglia"

                val builder = NotificationCompat.Builder(applicationContext, NotificaScorteMattutina.CANALE_SCORTE)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Controllo Scorte")
                    .setContentText(messaggio)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NotificaScorteMattutina.NOTIFICA_SCORTE_ID, builder.build())
            }

            NotificaScorteMattutina(applicationContext).programmaNotificaMattutina()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}