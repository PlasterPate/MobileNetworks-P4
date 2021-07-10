package com.example.thorium.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.data.repository.LocalRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MapViewModel : ViewModel() {

    private val TAG = "MapViewModel"

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    var allLocations = MutableLiveData<List<CellInfoData>>()
        private set


    fun addCellInfo(cellInfoData: CellInfoData) {
        Log.d(TAG, "Inserting cellData: $cellInfoData")

        LocalRepository.insertCellInfo(cellInfoData)
            .subscribeOn(Schedulers.io())
            .subscribe()

        LocalRepository.insertLocation(cellInfoData)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun getAllInfo() {
        LocalRepository.getAllLocations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cellInfoList ->
                allLocations.value = cellInfoList
                cellInfoList.forEach {
                    Log.d(TAG, it.toString())
                }
                Log.d(TAG, "End of locations")
            }, { t ->
                Log.d(TAG, "getAllLocations error: $t")
            }).also {
                compositeDisposable.add(it)
            }

        LocalRepository.getAllCellInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cellInfoList ->
                cellInfoList.forEach {
                    Log.d(TAG, it.toString())
                }
                Log.d(TAG, "End of cellInfo")
            }, { t ->
                Log.d(TAG, "getAllCellInfos error: $t")
            }).also {
                compositeDisposable.add(it)
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}