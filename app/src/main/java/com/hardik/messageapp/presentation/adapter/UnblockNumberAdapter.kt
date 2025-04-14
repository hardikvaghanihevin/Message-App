package com.hardik.messageapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.databinding.ItemBlockNumberBinding
import com.hardik.messageapp.util.Constants.BASE_TAG

class UnblockNumberAdapter(
    private val onItemClick: (String) -> Unit, // Normal click callback
) : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback()) {
    private val TAG = BASE_TAG + UnblockNumberAdapter::class.java.simpleName

    fun setFullList(fullList: List<String>, commitCallback: () -> Unit) {
        submitList(null) { submitList(fullList, commitCallback) }
    }
    inner class UnblockNumberViewHolder(val binding: ItemBlockNumberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, position: Int) {
            binding.tvContactName.text = item
            if (position == currentList.size - 1) { binding.divider.visibility = View.GONE} else { binding.divider.visibility = View.VISIBLE }
            binding.ivUnblock.setOnClickListener { onItemClick(item) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UnblockNumberViewHolder(ItemBlockNumberBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: String = getItem(position)
        when (holder) {
            is UnblockNumberViewHolder -> holder.bind(item, position)
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }
}