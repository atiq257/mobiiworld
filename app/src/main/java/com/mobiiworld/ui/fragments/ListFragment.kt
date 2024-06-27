package com.mobiiworld.ui.fragments

import android.content.Context
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
import com.mobiiworld.adapters.SquareAdapter
import com.mobiiworld.db.RepositoryDatabase
import com.mobiiworld.repository.SquareRepository
import com.mobiiworld.ui.SquareViewModel
import com.mobiiworld.ui.SquareViewModelProviderFactory
import com.mobiiworld.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.mobiiworld.utils.Resource

class ListFragment : Fragment() {

    companion object {
        fun newInstance() = ListFragment()
    }

    lateinit var viewModel: SquareViewModel
    private lateinit var ctx: Context
    lateinit var squareAdapter: SquareAdapter
    private var rvRepository:RecyclerView? = null
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
        rvRepository = view.findViewById(R.id.rvRepository)
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initializations and assign
        val repository = SquareRepository(RepositoryDatabase(ctx))
        val viewModelProviderFactory =
            SquareViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[SquareViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        squareAdapter.setOnItemClickListener {
            val fragment = DetailFragment.newInstance(it)
            // Begin the transaction
            val fragmentManager = activity?.supportFragmentManager
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.add(R.id.fragment_container, fragment)
            fragmentTransaction?.addToBackStack(DetailFragment::class.java.simpleName)
            // Complete the changes added above
            fragmentTransaction?.commit()
        }
//get saved repository, observe on changes on out database
        viewModel.getSavedRepositories()?.observe(viewLifecycleOwner, Observer { repositories -> //new list of repository
            if (repositories.isNullOrEmpty()){
                isFromDb = false
                viewModel.getRepositories(QUERY_PAGE_SIZE)
            } else {
                if (squareAdapter.itemCount == 0){
                    isFromDb = true
                }
                squareAdapter.differ.submitList(repositories) //update recyclerview //differ will calculate the difference between lists
            }
        })

        //subscribe to live data
        viewModel.repository.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Success->{
                    hideProgressBar()
                    //check null
                    response.data?.let { newsResponse ->
                        squareAdapter.differ.submitList(newsResponse.toList())

                        isLastPage = viewModel.lastDataSize < QUERY_PAGE_SIZE
                        if (isLastPage){
                            rvRepository?.setPadding(0,0,0,0)
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
        squareAdapter= SquareAdapter()
        rvRepository?.apply {
            adapter = squareAdapter
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
                viewModel.getRepositories(QUERY_PAGE_SIZE)
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