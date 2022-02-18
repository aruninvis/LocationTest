package com.example.testlocation.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Loc(   var timestamp: Long = 0L,
             var latitude: Float = 0f,var longitude: Float = 0f) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}