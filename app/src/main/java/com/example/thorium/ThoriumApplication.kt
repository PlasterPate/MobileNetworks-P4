package com.example.thorium

import android.app.Application
import android.util.Log
import com.example.thorium.data.repository.LocalRepository
import com.example.thorium.local.database.CellDatabase
import io.reactivex.schedulers.Schedulers

class ThoriumApplication : Application() {
    companion object {
        lateinit var cellDatabase: CellDatabase
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ThoriumApplication", "onCreate called")

        cellDatabase = CellDatabase.getInstance(this)


        // Clear database tables
        LocalRepository.deleteAllCellInfo()
            .subscribeOn(Schedulers.io())
            .subscribe()
        LocalRepository.deleteAllLocation()
            .subscribeOn(Schedulers.io())
            .subscribe()

    }
}