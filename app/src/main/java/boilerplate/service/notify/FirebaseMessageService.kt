package boilerplate.service.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import boilerplate.BuildConfig
import boilerplate.R
import boilerplate.data.local.repository.user.TokenRepository
import boilerplate.data.local.repository.user.UserRepository
import boilerplate.data.remote.api.response.Response
import boilerplate.data.remote.repository.auth.LoginRepository
import boilerplate.model.device.Device
import boilerplate.model.menu.EOfficeMenu
import boilerplate.model.user.User
import boilerplate.ui.main.MainActivity
import boilerplate.utils.SystemUtil
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.result
import boilerplate.utils.extension.toInt
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject
import java.util.Random

class FirebaseMessageService : FirebaseMessagingService() {

	companion object {
		const val ACTION_NOTIFY: String = BuildConfig.APPLICATION_ID + ".NOTIFY"

		const val TO_FRAGMENT: String = "TO_FRAGMENT"
		const val TO_PAGE: String = "TO_PAGE"
		const val ROLE: String = "WITH_ROLE"

		const val CONVERSATION_ID: String = "KEY_CONVERSATION_ID"
		const val WORK_MANAGE_ID: String = "KEY_WORK_MANAGE_ID"
		const val MEETING_ID: String = "WITH_MEETING_ID"

		const val CHANNEL_ID: String = "channel-Darsitec"
		const val CHANNEL_NAME: String = "Thông báo của Darsitec"

		const val KEY_ALERT = "alert"
		const val KEY_CONVERSATION_ID: String = "hoi_thoai_id"
		const val KEY_PUSH_DATA: String = "pushdata"
		const val KEY_BADGE: String = "badge"
		const val KEY_DATA: String = "Data"

		const val TYPE_DOCUMENT: Int = 1
		const val TYPE_DEPARTMENT_WORK: Int = 2
		const val TYPE_PERSONAL_WORK: Int = 3
		const val TYPE_SIGN_OUTGOING: Int = 4
		const val TYPE_SIGN_INTERNAL: Int = 5
		const val TYPE_SIGN_EXTERNAL: Int = 6
		const val TYPE_LAY_Y_KIEN_GUI: Int = 7
		const val TYPE_LAY_Y_KIEN_NHAN: Int = 8
		const val TYPE_HDTV: Int = 9
		const val TYPE_DOCUMENT_COME_REVOKE: Int = 10
		const val TYPE_SIGN_CONCENTRATE: Int = 11
	}

	private val disposable = CompositeDisposable()

	private val gson: Gson by inject<Gson>()
	private val loginRepo: LoginRepository by inject<LoginRepository>()
	private val tokenRepo: TokenRepository by inject<TokenRepository>()
	private val userRepo: UserRepository by inject<UserRepository>()

	private val _sound by lazy {
		Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.sound_message_receiver);
	}

	private lateinit var _channel: NotificationChannel

	override fun onCreate() {
		super.onCreate()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			_channel =
				NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).also {
					val attributes = AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_NOTIFICATION)
						.build()
					it.setSound(_sound, attributes)
					it.setShowBadge(true)
				}
		}
	}

	override fun onMessageReceived(notify: RemoteMessage) {
		Log.d("sss", "onMessageReceived: " + notify)
		val token = tokenRepo.getToken()
		if (token.isEmpty()) return
		val message = notify.data[KEY_ALERT]
		if (message.isNullOrEmpty()) return
		val conversationId = notify.data[KEY_CONVERSATION_ID]
		if (conversationId != null) {
			if (conversationId.contains(KEY_DATA)) {
				val notifyData = Gson().fromJson(conversationId, NotifyData::class.java)
				notifyWorkManager(message, notifyData.data.workItemId)
			} else {
				val count = notify.data[KEY_BADGE]
				notifyChat(message, count?.toInt() ?: 0, conversationId)
			}
			return
		}
		val eOfficePushData = notify.data[KEY_PUSH_DATA]
		if (eOfficePushData != null) {
			val pushData = gson.fromJson(eOfficePushData, PushData::class.java)
			if (pushData.roleId.isNotEmpty() && pushData.roleId.isNotEmpty()) {
				val type = when (pushData.type) {
					TYPE_DOCUMENT -> EOfficeMenu.NOT_HANDLE.index
					TYPE_DOCUMENT_COME_REVOKE -> EOfficeMenu.DOCUMENT_COMING_REVOKE.index

					TYPE_DEPARTMENT_WORK -> EOfficeMenu.DEPARTMENT_NOT_ASSIGN.index
					TYPE_PERSONAL_WORK -> EOfficeMenu.PERSONAL_NOT_DOING.index

					TYPE_SIGN_OUTGOING -> EOfficeMenu.SIGN_GOING.index
					TYPE_SIGN_INTERNAL -> EOfficeMenu.SIGN_INTERNAL.index
					TYPE_SIGN_EXTERNAL -> EOfficeMenu.SIGN_EXTERNAL.index
					TYPE_SIGN_CONCENTRATE -> EOfficeMenu.SIGN_CONCENTRATE.index

//					TYPE_LAY_Y_KIEN_GUI -> EOfficeMenu.NOT_HANDLE.index
//					TYPE_LAY_Y_KIEN_NHAN -> EOfficeMenu.NOT_HANDLE.index
//					TYPE_HDTV -> EOfficeMenu.NOT_HANDLE.index
					else -> -1
				}
				notifyPushData(message, type, pushData)
			}
			return
		}
	}

	override fun onNewToken(token: String) {
		val user = userRepo.getUser()
		disposable.add(
			userRepo.logout()
				.subscribeOn(Schedulers.io())
				.onErrorResumeWith { registerDeviceId(user, token) }
				.flatMap { registerDeviceId(user, token) }
				.result({
					it.result.notNull { device ->
						if (!device.deviceId.isNullOrEmpty()) {
							tokenRepo.saveDeviceId(device.id!!)
						}
					}
				})
		)
	}

	override fun onDestroy() {
		super.onDestroy()
		disposable.dispose()
	}

	private fun registerDeviceId(user: User, newDeviceToken: String): Flowable<Response<Device>> {
		val device: Device = Device().apply {
			id = user.id
			deviceToken = newDeviceToken
			deviceType = 0
		}
		return loginRepo.postRegisterDevice(device)
	}

	private fun getNotifyIntent(): Intent {
		val intent = Intent(this, MainActivity::class.java)
		intent.setAction(ACTION_NOTIFY)
		return intent
	}

	private fun createNotify(intent: Intent, messageBody: String): NotificationCompat.Builder {
		val notifyLarge = RemoteViews(packageName, R.layout.view_notify_large).apply {
			setTextViewText(R.id.tv_content, messageBody)
		}

		val resultIntent = PendingIntent.getActivity(
			this,
			Random().nextInt(),
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
		)
		return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
			.setContentTitle(getString(R.string.app_name))
			.setContentIntent(resultIntent)
			.setContentText(messageBody)
			.setSmallIcon(R.mipmap.ic_app_notification)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setAutoCancel(true)
			.setCustomBigContentView(notifyLarge)
			.also {
				if (userRepo.getSystemSound()) {
					it.setSound(_sound, AudioManager.STREAM_NOTIFICATION)
				}
			}
	}

	private fun notifyWorkManager(messageBody: String, workID: String) {
		if (SystemUtil.isAppIsInBackground(applicationContext)) {
			val intent = getNotifyIntent().apply {
				putExtra(WORK_MANAGE_ID, workID)
			}
			val builder: NotificationCompat.Builder = createNotify(intent, messageBody)
			val notificationManager = getSystemService(NotificationManager::class.java)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				notificationManager.createNotificationChannel(_channel)
			}
			val id = Random().nextInt()
			notificationManager.notify(id, builder.build())
		}
	}

	private fun notifyChat(messageBody: String, badge: Int, conversationId: String) {
		if (SystemUtil.isAppIsInBackground(applicationContext)) {
			val intent = getNotifyIntent().apply {
				putExtra(CONVERSATION_ID, conversationId)
			}
			val builder = createNotify(intent, messageBody).apply {
				setNumber(badge)
				if (messageBody.length > 7) {
					val inboxStyle = NotificationCompat.InboxStyle()
					val sb = SpannableString(messageBody).apply {
						val length = messageBody
							.split(":".toRegex())
							.dropLastWhile { it.isEmpty() }
							.toTypedArray()[0].length
						setSpan(
							StyleSpan(Typeface.BOLD),
							0,
							length,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
						)
					}
					setStyle(inboxStyle.addLine(sb))
				}
			}
			val notificationManager = getSystemService(NotificationManager::class.java)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				notificationManager.createNotificationChannel(_channel)
			}
			notificationManager.notify(conversationId, Random().nextInt(), builder.build())
		}
	}

	private fun notifyPushData(messageBody: String, fragmentType: Int, pushData: PushData) {
		if (SystemUtil.isAppIsInBackground(applicationContext)) {
			val intent = getNotifyIntent().apply {
				if (pushData.id.isNotEmpty()) {
					putExtra(MEETING_ID, pushData.id)
				}
				putExtra(ROLE, pushData.roleId)
				putExtra(TO_FRAGMENT, fragmentType)
			}

			val builder = createNotify(intent, messageBody)
			val notificationManager = getSystemService(NotificationManager::class.java)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				notificationManager.createNotificationChannel(_channel)
			}
			notificationManager.notify(Random().nextInt(), builder.build())
		}
	}
}

data class NotifyData(
	var data: Data = Data()
)

data class Data(
	var workItemId: String = ""
)

data class PushData(
	@SerializedName("ChucDanhId")
	val roleId: String = "",
	@SerializedName("Loai")
	val type: Int = 0,
	@SerializedName("CuocHopId")
	val id: String = "",
	@SerializedName("Key")
	var key: String = "",
) {
	val isMeetingCalendar: Boolean
		get() = type == 12
}