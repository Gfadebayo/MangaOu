package com.exzell.mangaplayground.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.exzell.mangaplayground.R

/**
 * Just like the name says, it is an adapter that holds a recycler view
 * Use cases are usually in situations where nested recycler views are needed
 * especially in different orientations
 */
class RecyclerViewAdapter(val mViewAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
                          val mContext: Context,
                          var mManager: RecyclerView.LayoutManager?): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    val PAYLOAD_PROGRESS = 24

    lateinit var mRecyclerView: RecyclerView

    private var isProgressHidden = false

    fun hideProgressBar(hide: Boolean){
        isProgressHidden = true
        notifyItemChanged(0, PAYLOAD_PROGRESS)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(viewType, parent, false))
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mRecyclerView.adapter = mViewAdapter
        if(!(holder.mRecyclerView.layoutManager == mManager))  mManager.let { holder.mRecyclerView.layoutManager = it }

        holder.mBar.visibility = if(isProgressHidden) View.GONE else View.VISIBLE
    }

    override fun getItemViewType(position: Int) = R.layout.generic_loading_recycler_view

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val mRecyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_load)
        val mBar = itemView.findViewById<ProgressBar>(R.id.progress_load)
    }
}