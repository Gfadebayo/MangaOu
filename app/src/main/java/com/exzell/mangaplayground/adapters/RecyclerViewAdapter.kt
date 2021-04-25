package com.exzell.mangaplayground.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.databinding.GenericLoadingRecyclerViewBinding

/**
 * Just like the name says, it is an adapter that holds a recycler view
 * Use cases are usually in situations where nested recycler views are needed
 * especially in different orientations
 */
class RecyclerViewAdapter(val mViewAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
                          val mContext: Context,
                          val mText: String = "",
                          var mManager: RecyclerView.LayoutManager?): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    var isProgressVisible = true
        private set

    var isTextVisible = false
        private set

    private val mPayload = "SOMETHING CHANGED"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(viewType, parent, false))
    }

    override fun getItemCount() = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.mBinding.progressLoad.visibility = if (isProgressVisible) View.VISIBLE else View.GONE
        holder.mBinding.textOther.visibility = if (isTextVisible) View.VISIBLE else View.GONE

        if (payloads.isEmpty() || !payloads.contains(mPayload)) {
            holder.mBinding.textOther.text = mText

            holder.mBinding.recyclerLoad.adapter = mViewAdapter

            if (holder.mBinding.recyclerLoad.layoutManager != mManager) mManager.let { holder.mBinding.recyclerLoad.layoutManager = it }
        }
    }

    override fun getItemViewType(position: Int) = R.layout.generic_loading_recycler_view

    fun textVisiblilty(visible: Boolean) {
        isTextVisible = visible
        notifyItemChanged(0, mPayload)
    }

    fun progressBarVisiblity(visible: Boolean) {
        isProgressVisible = visible
        notifyItemChanged(0, mPayload)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mBinding: GenericLoadingRecyclerViewBinding = GenericLoadingRecyclerViewBinding.bind(itemView)
    }
}