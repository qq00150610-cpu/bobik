package com.example.bobik

import android.app.Application
import com.example.bobik.engine.AgentEngine

class BobikApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AgentEngine.init(this)
    }
}
