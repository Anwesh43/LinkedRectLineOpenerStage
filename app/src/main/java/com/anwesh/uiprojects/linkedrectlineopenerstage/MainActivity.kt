package com.anwesh.uiprojects.linkedrectlineopenerstage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.rectlineopenerstage.RectLinerOpenerStage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : RectLinerOpenerStage = RectLinerOpenerStage.create(this)
        fullScreen()
        view.addOnAnimationCompleteListener({createToast("completed animation number ${it}")},
                {createToast("animation number ${it} is reset")})
    }

    fun createToast(txt : String) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}
