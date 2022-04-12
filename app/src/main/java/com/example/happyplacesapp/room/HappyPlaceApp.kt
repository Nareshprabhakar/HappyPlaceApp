package com.example.happyplacesapp.room

import android.app.Application

class HappyPlaceApp:Application() {
    val db by lazy {
        HappyPlaceDataBase.getInstance(this)
    }
}