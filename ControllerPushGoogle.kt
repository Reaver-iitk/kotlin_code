package ru.beward.intercom.controllers.push

import androidx.annotation.MainThread
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import com.sup.dev.java.classes.callbacks.CallbacksList1
import com.sup.dev.java.libs.debug.Debug.info
import ru.beward.intercom.app.Constants

object ControllerPushGoogle {
    private val getFnsTokenCallbacks = CallbacksList1<String?>()

    var tokenNow: String? = null
        private set

    fun init() {
        tokenNow = FirebaseInstanceId.getInstance().token
        if (tokenNow != null) info("XPush", "FCM token: $tokenNow")
    }

    @JvmStatic
    fun onPush(remoteMessage: RemoteMessage) {
        info("XPush", "FCM push received")
        ControllerPush.onPush(remoteMessage.data, remoteMessage.sentTime, tokenNow, Constants.PUSH_SERVICE_CODE_FCM)
    }

    @JvmStatic
    @MainThread
    fun onTokenRefresh() {
        info("XPush", "FCM onTokenRefresh")
        tokenNow = FirebaseInstanceId.getInstance().token
        info("XPush", "FCM token: $tokenNow")
        getFnsTokenCallbacks.invokeAndClear(tokenNow)
        ControllerPush.onFnsTokenRefresh()
    }

}