package com.hardik.messageapp.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ItemDummyBinding
import com.hardik.messageapp.helper.Constants.BASE_TAG

class DummyAdapter(
    val onDelete: (String) -> Unit,
    val onEdit: (String) -> Unit,
    private val onSelectionChanged: (Int) -> Unit // Callback for selection count
) : ListAdapter<String, DummyAdapter.DummyViewHolder>(DIFF_CALLBACK) {

    private val TAG = BASE_TAG + DummyAdapter::class.java.simpleName

//    private val selectedItems = mutableSetOf<Int>()
    private val selectedItems = mutableSetOf<String>() // Use item content or unique ID instead of position
    private var isSelectionMode = false

    inner class DummyViewHolder(val binding: ItemDummyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val llBackground: LinearLayout = itemView.findViewById(R.id.ll)

        fun bind(item: String, position: Int) {
            binding.textView.text = item
            binding.editButton.apply { visibility = View.GONE }.setOnClickListener { onEdit(item) }
            binding.deleteButton.apply { visibility = View.GONE }.setOnClickListener { onDelete(item) }

            // Highlight if selected
            binding.textView.setBackgroundColor(
                if (selectedItems.contains(item)) ContextCompat.getColor(binding.root.context, android.R.color.holo_blue_dark)
                else ContextCompat.getColor(binding.root.context, android.R.color.holo_orange_dark)
            )

            // Long press to enable selection mode
            binding.textView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(item)
                }
                true
            }

            // Click to toggle selection
            binding.textView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(item)
                }
            }
        }

       /* private fun toggleSelection(position: Int) {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            notifyItemChanged(position)

            // Update live selection count
            onSelectionChanged(selectedItems.size)

            // Exit selection mode if no items are selected
            if (selectedItems.isEmpty()) {
                isSelectionMode = false
            }
        }*/
        @SuppressLint("NotifyDataSetChanged")
        private fun toggleSelection(item: String) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }

            if (selectedItems.isEmpty()) {
                isSelectionMode = false // Disable selection mode when nothing is selected
            }

            notifyDataSetChanged() // Refresh UI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DummyViewHolder {
        val binding = ItemDummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DummyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DummyViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }

    // Select all items
    fun selectAll() {
        if (currentList.isEmpty()) return

        selectedItems.clear()
        selectedItems.addAll(currentList)//currentList.indices
        isSelectionMode = true
        notifyItemRangeChanged(0, itemCount)
        onSelectionChanged(selectedItems.size)
    }

    // Unselect all items
    fun unselectAll() {
        if (selectedItems.isEmpty()) return

        selectedItems.clear()
        isSelectionMode = false
        notifyItemRangeChanged(0, itemCount)
        onSelectionChanged(0)
    }

    // Get live selected count
    fun getSelectedCount(): Int = selectedItems.size
}

