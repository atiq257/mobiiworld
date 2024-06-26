package com.mobiiworld.api

import com.mobiiworld.models.Square
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SquareApi {
    //?per_page=1
    /* here we define single request that we can execute from the code
     */
    //we use Api interface to access api for request
    //function to get all breaking news from the api
    // we need to specify the type of http request -GET here
    //and we return the responses from the API

    @GET("square/repos")

    //function
    //async
    //coroutine
    suspend fun getRepositories(
        //request parameters to function
    @Query("per_page")  //to paginate the request
    perPage: Int= 1,
    @Query("page")  //to paginate the request
    pageNumber: Int= 1,
    ):Response<ArrayList<Square>> //return response
}