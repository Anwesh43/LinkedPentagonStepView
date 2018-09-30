package com.anwesh.uiprojects.linkedpentagonstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.pentagonstepview.PentagonStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PentagonStepView.create(this)
    }
}
