package com.example.testlocation.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Loc::class],
    version = 1
)
abstract class LocationDatabase : RoomDatabase(){
    abstract fun getLocationDao(): LocationDao
}