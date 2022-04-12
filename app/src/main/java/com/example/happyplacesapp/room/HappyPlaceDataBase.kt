package com.example.happyplacesapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HappyPlaceEntity::class,UserProfileEntity::class], version = 1)
abstract class HappyPlaceDataBase:RoomDatabase() {

    abstract fun happyPlaceDao():HappyPlaceDao

    companion object{
        @Volatile
        private  var Instance:HappyPlaceDataBase? = null

        fun getInstance(context: Context):HappyPlaceDataBase{
            synchronized(this){
                var instance = Instance

                if(instance == null){

                    instance = Room.databaseBuilder(context.applicationContext,
                        HappyPlaceDataBase::class.java,"happyPlaces_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    Instance = instance

                }
                return instance
            }
        }
    }

}