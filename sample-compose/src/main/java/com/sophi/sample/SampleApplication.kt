package com.sophi.sample

import android.app.Application

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configure with the identity the demo last signed in with, so the SDK's persisted shopper and the UI agree.
        DemoSophi.configure(this, DemoTenants.remembered(this), DemoTenants.rememberedUser(this))
    }
}
