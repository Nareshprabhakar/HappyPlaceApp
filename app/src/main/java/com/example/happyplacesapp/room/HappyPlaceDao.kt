package com.example.happyplacesapp.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HappyPlaceDao {


    @Insert
    suspend fun insert(happyPlaceEntity: HappyPlaceEntity)

    @Insert
    suspend fun insert(userProfileEntity: UserProfileEntity)

    @Update
    suspend fun update(happyPlaceEntity: HappyPlaceEntity)

    @Update
    suspend fun update(userProfileEntity: UserProfileEntity)

    @Delete
    suspend fun delete(happyPlaceEntity: HappyPlaceEntity)

    @Query("SELECT * FROM 'happy_places'")
     fun fetchAllHappyPlaces():Flow<List<HappyPlaceEntity>>

    @Query("SELECT * FROM 'user'")
    fun fetchUser():Flow<UserProfileEntity>


    @Query("SELECT * FROM 'happy_places' where id = :id ")
     fun fetchHappyPlaceById(id:Int):Flow<HappyPlaceEntity>


}