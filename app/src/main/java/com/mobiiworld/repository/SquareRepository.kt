package com.mobiiworld.repository

import com.mobiiworld.api.RetrofitInstance
import com.mobiiworld.models.Square
import com.mobiiworld.db.RepositoryDatabase

/*
get data from database and from remote data source (retrofit api)
 */
class SquareRepository(
    val db: RepositoryDatabase //parameter
) {

    /*
    function that directly queries our api for the breaking news
     */
    suspend fun getRepositories(perPage:Int,page:Int)=
        RetrofitInstance.api.getRepositories(perPage,page)

    /*
    function to insert repository to db
     */
    suspend fun upsert(square: Square)=
        db.getRepositoryDao().upsert(square)


    /*
    function to insert repository to db
     */
    suspend fun upsert(squareList: List<Square>)=
        db.getRepositoryDao().upsert(squareList)

    /*
    function to get saved repository from db
     */
    fun getSavedRepositories()=
        db.getRepositoryDao().getAllRepositories()

    /*
    function to delete repository from db
     */
    suspend fun deleteRepository(square: Square)=
        db.getRepositoryDao().deleteRepository(square)
}