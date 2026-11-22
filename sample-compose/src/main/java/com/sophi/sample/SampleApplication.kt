package com.sophi.sample

import android.app.Application

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SampleSophi.configure(this)
    }
}
