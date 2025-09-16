package com.example.gestionenegozio.notifiche

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.gestionenegozio.R
import com.example.gestionenegozio.dati.repository.RepositoryVendita
import com.example.gestionenegozio.dati.database.DatabaseNegozio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class NotificaGuadagnoSerale(private val context: Context) {

    companion object {
        const val CANALE_GUADAGNI = "guadagni_channel"
        const val NOTIFICA_GUADAGNI_ID = 1003
    }

    init {
        creaCanaleNotifica()
        programmaNotificaSerale()
    }

    private fun creaCanaleNotifica() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canale = NotificationChannel(
                CANALE_GUADAGNI,
                "Riepilogo Giornaliero",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Guadagno giornaliero"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(canale)
        }
    }

    fun programmaNotificaSerale() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val riepilogoRequest = OneTimeWorkRequestBuilder<RiepilogoSeraleWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "riepilogo_serale",
            ExistingWorkPolicy.REPLACE,
            riepilogoRequest
        )
    }
}

class RiepilogoSeraleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = DatabaseNegozio.ottieniDatabase(applicationContext)
            val repositoryVendita = RepositoryVendita(
                database.venditaDao(),
                database.elementoVenditaDao(),
                database.prodottoDao()
            )

            val inizioGiorno = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val fineGiorno = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val statisticheOggi = repositoryVendita.ottieniStatistiche(inizioGiorno, fineGiorno)

            val messaggio = "Hai guadagnato oggi: €${String.format("%.2f", statisticheOggi.soldiTotali)}"

            val builder = NotificationCompat.Builder(applicationContext, NotificaGuadagnoSerale.CANALE_GUADAGNI)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Guadagno di oggi")
                .setContentText(messaggio)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NotificaGuadagnoSerale.NOTIFICA_GUADAGNI_ID, builder.build())

            NotificaGuadagnoSerale(applicationContext).programmaNotificaSerale()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}