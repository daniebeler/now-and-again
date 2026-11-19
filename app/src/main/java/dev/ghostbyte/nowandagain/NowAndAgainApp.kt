package dev.ghostbyte.nowandagain

import android.app.Application
import dev.ghostbyte.nowandagain.notifications.Notifications
import dev.ghostbyte.nowandagain.notifications.ReminderScheduler

class NowAndAgainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
        ReminderScheduler.schedule(this)


        //WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<DailyReminderWorker>().build())
    }
}