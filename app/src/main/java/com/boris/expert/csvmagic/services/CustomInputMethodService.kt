package com.boris.expert.csvmagic.services

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.view.activities.MainActivity


class CustomInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

//    private lateinit var serviceCallbacks: ServiceCallbacks
     public var flag = ""
    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        val keyboard = Keyboard(this, R.xml.number_pad)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)
//        serviceCallbacks = applicationContext as ServiceCallbacks
        return keyboardView
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        when (primaryCode) {
            49 -> {
                flag = "image_r"
                val intent = Intent(Constants.RECEIVER_INTENT)
                intent.putExtra(Constants.RECEIVER_MESSAGE, flag)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            50 -> {

                flag = "photo_r"
                val intent = Intent(Constants.RECEIVER_INTENT)
                intent.putExtra(Constants.RECEIVER_MESSAGE, flag)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            else -> {
            }
        }
    }

    override fun onPress(primaryCode: Int) {}

    override fun onRelease(primaryCode: Int) {}

    override fun onText(text: CharSequence?) {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeDown() {}

    override fun swipeUp() {}


}