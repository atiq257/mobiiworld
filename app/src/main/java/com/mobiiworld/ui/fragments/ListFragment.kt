package com.mobiiworld.ui.fragments

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobiiworld.R
import com.mobiiworld.SquareApplication
import com.mobiiworld.adapters.NewsAdapter
import com.mobiiworld.db.ArticleDatabase
import com.mobiiworld.repository.NewsRepository
import com.mobiiworld.ui.NewsViewModel
import com.mobiiworld.ui.NewsViewModelProviderFactory
import com.mobiiworld.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.mobiiworld.utils.Resource

class ListFragment : Fragment() {

    companion object {
        fun newInstance() = ListFragment()
    }

    lateinit var viewModel: NewsViewModel
    private lateinit var ctx: Context
    lateinit var newsAdapter: NewsAdapter
    private var rvBreakingNews:RecyclerView? = null
    private var paginationProgressBar:ProgressBar? = null
    private var isFromDb = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        rvBreakingNews = view.findViewById(R.id.rvBreakingNews)
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initializations and assign
        val repository = NewsRepository(ArticleDatabase(ctx))
        val viewModelProviderFactory =
            NewsViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val fragment = DetailFragment.newInstance(it)
            // Begin the transaction
            val fragmentManager = activity?.supportFragmentManager
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.add(R.id.fragment_container, fragment)
            fragmentTransaction?.addToBackStack(DetailFragment::class.java.simpleName)
            // Complete the changes added above
            fragmentTransaction?.commit()
        }
//get saved news, observe on changes on out database
        viewModel.getSavedArticle()?.observe(viewLifecycleOwner, Observer { articles -> //new list of articles
            if (articles.isNullOrEmpty()){
                isFromDb = false
                viewModel.getBreakingNews(QUERY_PAGE_SIZE)
            } else {
                isFromDb = true
                newsAdapter.differ.submitList(articles) //update recyclerview //differ will calculate the difference between lists
            }
        })

        //subscribe to live data
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Success->{
                    hideProgressBar()
                    //check null
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.toList())

                        isLastPage = viewModel.lastDataSize < QUERY_PAGE_SIZE
                        if (isLastPage){
                            rvBreakingNews?.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error->{
                    hideProgressBar()
                    response.message?.let { message->
                        Log.e("TAG", "An error occured: $message" )
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading->{
                    showProgressBar()
                }
            }
        })

    }



    private fun setupRecyclerView(){
        newsAdapter= NewsAdapter()
        rvBreakingNews?.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@ListFragment.scrollListener)
        }
    }
    private fun hideProgressBar(){
        paginationProgressBar?.visibility= View.INVISIBLE
        isLoading= false
    }
    private fun showProgressBar(){
        paginationProgressBar?.visibility= View.VISIBLE
        isLoading= true
    }

    var isLoading= false
    var isLastPage= false
    var isScrolling= false


    val scrollListener= object : RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            //manually calculating payout numbers for pagination
            val layoutManager= recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition= layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount= layoutManager.childCount
            val totalItemCount= layoutManager.itemCount

            val  isNotLoadingAndNotLastPage= !isLoading && !isLastPage
            val isAtLastItem= firstVisibleItemPosition+ visibleItemCount >= totalItemCount
            val isNotAtBeginning= firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible= totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate && !isFromDb){
                viewModel.getBreakingNews(QUERY_PAGE_SIZE)
                isScrolling= false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling= true
            }
        }
    }


}