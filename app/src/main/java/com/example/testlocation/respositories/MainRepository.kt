package com.example.testlocation.respositories

import com.example.testlocation.db.Loc
import com.example.testlocation.db.LocationDao

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class MainRepository @Inject constructor(private val locDAO: LocationDao) {
    suspend fun insertRun(loc: Loc) = locDAO.insertLoc(loc)
     fun getAllLocation() = locDAO.getAllLocation()
}
