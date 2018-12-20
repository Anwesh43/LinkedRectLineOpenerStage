package com.anwesh.uiprojects.linkedrectlineopenerstage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.rectlineopenerstage.RectLinerOpenerStage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RectLinerOpenerStage.create(this)
    }
}
