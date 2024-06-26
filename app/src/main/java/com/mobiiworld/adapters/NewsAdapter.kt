package com.mobiiworld.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobiiworld.R
import com.mobiiworld.models.Square

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val  txtName = itemView.findViewById<TextView>(R.id.tvSource)
        val rating = itemView.findViewById<AppCompatRatingBar>(R.id.ratingStar)
        init {
            rating.setOnTouchListener { _, _ -> true }
        }
    }

    /*
    here we don't use list.notifyDatasetChanged
    because using that it the recyclerview adapter will always update the whole items even if they are not changed

    to solve this, we use DiffUtil
    it calculates the diff between two list and enable us to only update those items that are different
    also runs in background so don't block the main thread
     */
    private val differCallback= object : DiffUtil.ItemCallback<Square>(){
        override fun areItemsTheSame(oldItem: Square, newItem: Square): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Square, newItem: Square): Boolean {
            return oldItem == newItem
        }
    }
    // tool that will take the two list and tell the differences
    val differ= AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_article_preview, parent, false))
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article= differ.currentList[position]
        holder.txtName.text = article.name
        holder.rating.rating = (article.stargazers_count?:0).toFloat()
        holder.itemView.apply {
            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }

    private var onItemClickListener:((Square)->Unit)?=null

    fun setOnItemClickListener(listener: (Square)->Unit){
        onItemClickListener= listener
    }

    override fun getItemCount(): Int {
        return  differ.currentList.size
    }
}