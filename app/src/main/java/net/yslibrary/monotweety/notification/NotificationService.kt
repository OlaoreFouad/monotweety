package net.yslibrary.monotweety.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.StringDef
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import net.yslibrary.monotweety.App
import net.yslibrary.monotweety.R
import net.yslibrary.monotweety.activity.compose.ComposeActivity
import net.yslibrary.monotweety.activity.main.MainActivity
import net.yslibrary.monotweety.analytics.Analytics
import net.yslibrary.monotweety.base.HasComponent
import net.yslibrary.monotweety.base.closeNotificationDrawer
import net.yslibrary.monotweety.data.appinfo.AppInfo
import net.yslibrary.monotweety.setting.domain.FooterStateManager
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

class NotificationService : Service(), HasComponent<NotificationComponent> {

    companion object {
        const val KEY_NOTIFICATION_TWEET_TEXT = "notification_tweet_text"

        const val ACTION = "net.yslibrary.monotweety.notification.NotificationService.Action"

        const val KEY_COMMAND = "notification_command"

        const val COMMAND_SHOW_NOTIFICATION =
            "net.yslibrary.monotweety.notification.NotificationService.ShowNotification"
        const val COMMAND_CLOSE_NOTIFICATION =
            "net.yslibrary.monotweety.notification.NotificationService.CloseNotification"
        const val COMMAND_DIRECT_TWEET =
            "net.yslibrary.monotweety.notification.NotificationService.DirectTweet"
        const val COMMAND_SHOW_TWEET_DIALOG =
            "net.yslibrary.monotweety.notification.NotificationService.ShowTweetDialog"

        fun commandIntent(@CommandType command: String): Intent {
            val intent = Intent()
            intent.action = ACTION
            intent.putExtra(KEY_COMMAND, command)

            return intent
        }

        fun callingIntent(context: Context): Intent {
            val intent = Intent(context, NotificationService::class.java)

            return intent
        }
    }

    private val binder: IBinder by lazy { ServiceBinder() }
    private val commandReceiver: NotificationCommandReceiver by lazy { NotificationCommandReceiver() }

    @set:[Inject]
    var viewModel by Delegates.notNull<NotificationServiceViewModel>()

    @set:[Inject]
    var notificationManager by Delegates.notNull<NotificationManagerCompat>()

    @set:[Inject]
    var analytics by Delegates.notNull<Analytics>()

    private val disposables = CompositeDisposable()


    override val component: NotificationComponent by lazy {
        DaggerNotificationComponent.builder()
            .userComponent(App.userComponent(this))
            .notificationServiceModule(NotificationServiceModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")

        // check login status before inject dependencies & initialize
        App.appComponent(this).isLoggedIn()
            .execute()
            .subscribe { loggedIn ->
                if (loggedIn) {
                    injectDependencies()
                    setEvents()
                    registerCommandReceiver()
                } else {
                    stopSelf()
                }
            }.addTo(disposables)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand: ${intent?.toString()}")

        val notification = showNotification()
        startForeground(R.id.tweet_notification, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")

        try {
            unregisterReceiver(commandReceiver)
        } catch (e: Exception) {
            // suppress exception
            // http://stackoverflow.com/questions/12421449/android-broadcastreceiver-unregisterreceiver-issue-not-registered#answer-31276205
            // https://github.com/yshrsmz/omnitweety-android/issues/22
        }

        disposables.dispose()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    private fun injectDependencies() {
        component.inject(this)
    }

    private fun registerCommandReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION)
        registerReceiver(commandReceiver, intentFilter)
    }

    fun setEvents() {
        viewModel.error
            .subscribe {
                closeNotificationDrawer()
                viewModel.onUpdateNotificationRequested()
                showError(it)
            }
            .addTo(disposables)

        viewModel.overlongStatus
            .subscribe {
                closeNotificationDrawer()
                showTweetFailedBecauseOfLength()
                viewModel.onUpdateNotificationRequested()
                showTweetDialog(it.status)
                analytics.tweetFromNotificationButTooLong()
            }
            .addTo(disposables)

        viewModel.updateCompleted
            .subscribe {
                viewModel.onUpdateNotificationRequested()
                closeNotificationDrawer()
                showTweetSucceeded()
                analytics.tweetFromNotification()
            }.addTo(disposables)

        viewModel.updateNotificationRequests
            .subscribe { updateNotification(it.footerState, it.timelineApp) }
            .addTo(disposables)

        viewModel.stopNotificationService
            .subscribe { stopSelf() }
            .addTo(disposables)
    }

    fun buildNotification(footerState: FooterStateManager.State, appInfo: AppInfo): Notification {
        val directTweetIntent = PendingIntent.getBroadcast(
            applicationContext, 0,
            commandIntent(COMMAND_DIRECT_TWEET), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openDialogIntent = PendingIntent.getActivity(
            applicationContext, 1,
            ComposeActivity.callingIntent(applicationContext), PendingIntent.FLAG_CANCEL_CURRENT
        )

        val closeIntent = PendingIntent.getBroadcast(
            applicationContext, 2,
            commandIntent(COMMAND_CLOSE_NOTIFICATION), PendingIntent.FLAG_CANCEL_CURRENT
        )

        val openSettingIntent = TaskStackBuilder.create(applicationContext)
            .addParentStack(MainActivity::class.java)
            .addNextIntent(MainActivity.callingIntent(applicationContext))
            .getPendingIntent(3, PendingIntent.FLAG_UPDATE_CURRENT)

        val remoteInput = RemoteInput.Builder(KEY_NOTIFICATION_TWEET_TEXT)
            .setLabel(getString(R.string.label_whats_happening))
            .build()

        val directTweetAction = NotificationCompat.Action.Builder(
            R.drawable.ic_send_black_24dp,
            getString(R.string.label_tweet_now),
            directTweetIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        val closeAction = NotificationCompat.Action.Builder(
            R.drawable.ic_close_black_24dp,
            getString(R.string.label_close_notification),
            closeIntent
        ).build()

        val settingAction = NotificationCompat.Action.Builder(
            R.drawable.ic_settings_black_24dp,
            getString(R.string.label_open_settings),
            openSettingIntent
        ).build()

        val footerStateString = if (footerState.enabled)
            getString(R.string.label_notification_footer_on, footerState.text)
        else
            getString(R.string.label_notification_footer_off)
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine(getString(R.string.label_notification_content))
        inboxStyle.addLine(footerStateString)

        val builder = NotificationCompat.Builder(applicationContext, Channel.EDITOR.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.label_notification_content))
            .setContentIntent(openDialogIntent)
            .setStyle(inboxStyle)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)


        // add remote input if and only if device sdk version is greater than Nougat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.addAction(directTweetAction)
        }

        builder.addAction(settingAction)

        if (appInfo.installed) {
            val openTimelineIntent = PendingIntent.getActivity(
                applicationContext,
                1,
                packageManager.getLaunchIntentForPackage(appInfo.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT
            )

            val timelineAction = NotificationCompat.Action.Builder(
                R.drawable.ic_view_headline_black_24dp,
                getString(R.string.label_open_timeline),
                openTimelineIntent
            ).build()

            builder.addAction(timelineAction)
        }

        builder.addAction(closeAction)

        return builder.build()
    }

    fun showNotification(): Notification {
        Timber.d("show notification")

        val footerState = viewModel.footerState.blockingFirst()
        val appInfo = viewModel.selectedTimelineApp.blockingFirst()
        val noti = buildNotification(footerState, appInfo)

        noti.flags = NotificationCompat.FLAG_NO_CLEAR

        notificationManager.notify(R.id.tweet_notification, noti)

        return noti
    }

    fun updateNotification(footerState: FooterStateManager.State, appInfo: AppInfo): Notification {
        Timber.d("update notification")

        val noti = buildNotification(footerState, appInfo)

        noti.flags = NotificationCompat.FLAG_NO_CLEAR

        notificationManager.notify(R.id.tweet_notification, noti)

        return noti
    }

    fun showTweetDialog(status: String) {
        startActivity(ComposeActivity.callingIntent(applicationContext, status))
    }

    fun showTweetSucceeded() {
        Completable.fromAction {
            Toast.makeText(
                applicationContext,
                getString(R.string.message_tweet_succeeded),
                Toast.LENGTH_SHORT
            )
                .show()
        }.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun showTweetFailed() {
        Completable.fromAction {
            Toast.makeText(
                applicationContext,
                getString(R.string.error_tweet_failed),
                Toast.LENGTH_SHORT
            )
                .show()
        }.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun showTweetFailedBecauseOfLength() {
        Completable.fromAction {
            Toast.makeText(
                applicationContext,
                getString(R.string.error_tweet_failed_because_of_length),
                Toast.LENGTH_SHORT
            )
                .show()
        }.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun showError(message: String) {
        Completable.fromAction {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    inner class ServiceBinder : Binder() {
        val service: NotificationService
            get() = this@NotificationService
    }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        COMMAND_SHOW_NOTIFICATION,
        COMMAND_CLOSE_NOTIFICATION,
        COMMAND_DIRECT_TWEET,
        COMMAND_SHOW_TWEET_DIALOG
    )
    annotation class CommandType

    inner class NotificationCommandReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra(KEY_COMMAND)

            Timber.d("onReceive - command: $command")

            // TODO: check login status first

            command?.let {
                when (it) {
                    COMMAND_CLOSE_NOTIFICATION -> viewModel.onCloseNotificationCommand()
                    COMMAND_DIRECT_TWEET -> {
                        val bundle = RemoteInput.getResultsFromIntent(intent)
                        bundle?.getString(KEY_NOTIFICATION_TWEET_TEXT)?.let { text ->
                            viewModel.onDirectTweetCommand(text)
                        }
                    }
                    else -> {
                        // no-op
                    }
                }
            }
        }
    }
}
