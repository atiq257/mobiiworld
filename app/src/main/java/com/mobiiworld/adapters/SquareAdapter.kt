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

class SquareAdapter : RecyclerView.Adapter<SquareAdapter.RepositoryViewHolder>() {

    inner class RepositoryViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        return RepositoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_repositoy, parent, false))
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repository = differ.currentList[position]
        holder.txtName.text = repository.name
        holder.rating.rating = (repository.stargazers_count?:0).toFloat()
        holder.itemView.apply {
            setOnClickListener {
                onItemClickListener?.let { it(repository) }
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