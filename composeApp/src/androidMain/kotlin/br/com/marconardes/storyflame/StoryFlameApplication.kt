package br.com.marconardes.storyflame

import android.app.Application
import br.com.marconardes.viewmodel.objectContextProvider

class StoryFlameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        objectContextProvider.applicationContext = this
    }
}
