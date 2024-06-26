package com.mobiiworld.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.lifecycle.ViewModelProvider
import com.mobiiworld.R
import com.mobiiworld.db.ArticleDatabase
import com.mobiiworld.models.Square
import com.mobiiworld.repository.NewsRepository
import com.mobiiworld.ui.NewsViewModel
import com.mobiiworld.ui.NewsViewModelProviderFactory

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {
    private var square: Square? = null
    private var ratingBar:AppCompatRatingBar? = null
    private var bookmark:AppCompatImageButton? = null
    private var txtTitle:TextView? = null
    lateinit var viewModel: NewsViewModel
    private lateinit var ctx:Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            square = it.getParcelable(ARG_PARAM1)
        }
        val repository = NewsRepository(ArticleDatabase(ctx))
        val viewModelProviderFactory =
            NewsViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detail, container, false)
        ratingBar = view.findViewById(R.id.ratingStar)
        bookmark = view.findViewById(R.id.imgBookmark)
        txtTitle = view.findViewById(R.id.txtTitle)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtTitle?.text = square?.name
        bookmark?.isSelected = square?.isBookmarked?:false

        ratingBar?.rating = (square?.stargazers_count?:0).toFloat()

        bookmark?.setOnClickListener {
            square?.isBookmarked = !(square?.isBookmarked?:false)
            square?.let { it1 -> viewModel.updateBookmark(it1) }
            bookmark?.isSelected = square?.isBookmarked?:false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment DetailFragment.
         */
        @JvmStatic
        fun newInstance(square: Square) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, square)
                }
            }
    }
}