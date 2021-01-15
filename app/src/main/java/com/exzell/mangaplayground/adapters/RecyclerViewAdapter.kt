package com.exzell.mangaplayground.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
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

    private var isProgressHidden = false

    private val mPayload = "PROGRESS CHANGED"

    fun hideProgressBar(hide: Boolean){
        isProgressHidden = true
        notifyItemChanged(0, mPayload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(viewType, parent, false))
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.mBar.visibility = if (isProgressHidden) View.GONE else View.VISIBLE

        if (payloads.isEmpty() || !payloads.contains(mPayload)) {
            holder.mRecyclerView.adapter = mViewAdapter

            if (holder.mRecyclerView.layoutManager != mManager) mManager.let { holder.mRecyclerView.layoutManager = it }
        }
    }

    override fun getItemViewType(position: Int) = R.layout.generic_loading_recycler_view

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val mRecyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_load)
        val mBar = itemView.findViewById<ProgressBar>(R.id.progress_load)
    }
}