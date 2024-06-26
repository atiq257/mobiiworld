package com.mobiiworld.repository

import com.mobiiworld.api.RetrofitInstance
import com.mobiiworld.models.Square
import com.mobiiworld.db.ArticleDatabase

/*
get data from database and from remote data source (retrofit api)
 */
class NewsRepository(
    val db: ArticleDatabase //parameter
) {

    /*
    function that directly queries our api for the breaking news
     */
    suspend fun getRepositories(perPage:Int,page:Int)=
        RetrofitInstance.api.getRepositories(perPage,page)

    /*
    function to insert article to db
     */
    suspend fun upsert(article: Square)=
        db.getArticleDao().upsert(article)


    /*
    function to insert article to db
     */
    suspend fun upsert(article: List<Square>)=
        db.getArticleDao().upsert(article)

    /*
    function to get saved news from db
     */
    fun getSavedNews()=
        db.getArticleDao().getAllArticles()

    /*
    function to delete articles from db
     */
    suspend fun deleteArticle(article: Square)=
        db.getArticleDao().deleteArticle(article)
}