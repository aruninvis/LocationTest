package com.example.testlocation.db


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoc(loc: Loc)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllLocation(): LiveData<List<Loc>>

//
//    @Query("DELETE FROM running_table WHERE timestamp < (UNIX_TIMESTAMP() - 3600)")
//    suspend  fun deleteOldData()
//
//    @Transaction
//    suspend  fun deleteAndCreate(loc: Loc,context:CoroutineContext) = withContext(context){
//        insertLoc(loc)
//        deleteOldData()
//    }
}