package com.daniebeler.nowandagain

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.daniebeler.nowandagain.notifications.DailyReminderWorker
import com.daniebeler.nowandagain.notifications.Notifications
import com.daniebeler.nowandagain.notifications.ReminderScheduler

class NowAndAgainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
        ReminderScheduler.schedule(this)


        //WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<DailyReminderWorker>().build())
    }
}