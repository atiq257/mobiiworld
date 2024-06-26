package com.mobiiworld.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mobiiworld.models.Square

/*
Data Access Object
This is an interface for accessing database
Here we define functions to access the local database
save repositories, read repositories, delete repositories
 */
@Dao //annotate to let room know that this is the interface that defines the function

interface RepositoryDao {

    /*
    function to insert or update an repository
    conflictStrategy is to define what to do, if conflict occurs in the database, like already exists in database
    here we, REPLACE
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(square: Square) // function(parameter):return --> here we return ID


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(squareList: List<Square>) // function(parameter):return --> here we return ID

    /*
    query function to return all repositories available in our database
    LiveData: class of Android architecture components that enables fragments to subscribe to changes of live data
    when data changes, liveData will notify all the fragments so they can update the views
    useful in rotation
     */
    @Query("SELECT* FROM squares")
    fun getAllRepositories():LiveData<List<Square>>?


    /*
    Delete function
     */
    @Delete
    suspend fun deleteRepository(square: Square)
}