package com.mobiiworld.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobiiworld.SquareApplication
import com.mobiiworld.models.Square
import com.mobiiworld.repository.NewsRepository
import com.mobiiworld.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.mobiiworld.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    val newsRepository: NewsRepository //parameter
) : AndroidViewModel(app){ //inheriting from android view model to use application context
    //here we use application context to get the context throughout the app running,
    //so it will work even if the activity changes or destroys, the app context will still work until the app's running

    //LIVEDATA OBJECT
    private val _breakingNews= MutableLiveData<Resource<ArrayList<Square>>>()

    val breakingNews: LiveData<Resource<ArrayList<Square>>> get() = _breakingNews

    var lastDataSize = 0
    var page = 1


//    init {
//        getBreakingNews(QUERY_PAGE_SIZE)
//    }

    //we cannot start the function in the coroutine so we start the it here
    /*
    viewModelScope makes the function alive only as long as the ViewModel is alive
     */
    fun getBreakingNews(perPage: Int)= viewModelScope.launch {
        //breakingNews.postValue(Resource.Loading()) //init loading state before the network call
        safeBreakingNewsCall(perPage,page)
    }


// update for bookmarked

    fun updateBookmark(square: Square){
        viewModelScope.launch {
            newsRepository.upsert(square)
        }
    }


    private fun handleBreakingNewsResponse(response: Response<ArrayList<Square>>): Resource<ArrayList<Square>> {
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                saveArticle(resultResponse)
                lastDataSize = resultResponse.size
                if (lastDataSize == QUERY_PAGE_SIZE) {
                    page += 1
                }
                val oldArticles= breakingNews.value?.data //else, add all articles to old
                oldArticles?.addAll(resultResponse) //add new articles to old articles
                return  Resource.Success(oldArticles ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    /*
    function to save articles to db: coroutine
     */
    fun saveArticle(square: Square)= viewModelScope.launch {
        newsRepository.upsert(square)
    }

    /*
    function to save articles to db: coroutine
     */
    fun saveArticle(square: List<Square>)= viewModelScope.launch {
        newsRepository.upsert(square)
    }

    /*
    function to get all saved news articles
     */
    fun getSavedArticle() = newsRepository.getSavedNews()

    /*
    function to delete article from db
     */
    fun deleteSavedArticle(square: Square)= viewModelScope.launch {
        newsRepository.deleteArticle(square)
    }

    private suspend fun safeBreakingNewsCall(perPage:Int,page:Int){
        _breakingNews.postValue(Resource.Loading(breakingNews.value?.data))
        val savedData = getSavedArticle()
        if (savedData?.value == null){
            try{
                if (hasInternetConnection()){
                    val response = newsRepository.getRepositories(perPage,page)
                    //handling response
                    val data = handleBreakingNewsResponse(response)
                    _breakingNews.postValue(data)
                }else{
                    _breakingNews.postValue(Resource.Error("No Internet Connection"))
                }

            } catch (t: Throwable){
                when(t){
                    is IOException-> _breakingNews.postValue(Resource.Error("Network Failure"))
                    else-> _breakingNews.postValue(Resource.Error("Conversion Error"))
                }
            }
        } else {
            _breakingNews.postValue(Resource.Success(ArrayList(savedData.value)))
        }
    }




    private fun hasInternetConnection(): Boolean{
        val connectivityManager= getApplication<SquareApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork= connectivityManager.activeNetwork?: return false
        val capabilities= connectivityManager.getNetworkCapabilities(activeNetwork)?: return false

        return when{
            capabilities.hasTransport(TRANSPORT_WIFI)-> true
            capabilities.hasTransport(TRANSPORT_CELLULAR)-> true
            capabilities.hasTransport(TRANSPORT_ETHERNET)->true
            else -> false
        }
    }
}