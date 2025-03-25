package com.hardik.messageapp.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.databinding.ItemConversationBinding
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.ConversationThread.Companion.DIFF_CALLBACK
import com.hardik.messageapp.presentation.util.DateUtil.DATE_FORMAT_dd_MMM
import com.hardik.messageapp.presentation.util.DateUtil.longToString

class ConversationAdapter (
    val swipeLeftBtn: (ConversationThread) -> Unit,
    val swipeRightBtn: (ConversationThread) -> Unit,
    private val onItemClick: (ConversationThread) -> Unit, // Normal click callback
    private val onSelectionChanged: (List<ConversationThread>) -> Unit // Selection callback
) : ListAdapter<ConversationThread, ConversationAdapter.ConversationViewHolder>(DIFF_CALLBACK) {

    private val selectedItems = mutableSetOf<ConversationThread>() // Use item content or unique ID instead of position
    private var isSelectionMode = false


    inner class ConversationViewHolder(val binding: ItemConversationBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConversationThread, position: Int) {
            binding.tvTitle.text = item.displayName
            binding.tvSnippet.text = item.snippet
            binding.tvDate.text = longToString(timestamp = item.date, pattern = DATE_FORMAT_dd_MMM)

            binding.conversationSwipeLeft.apply { visibility = View.GONE }.setOnClickListener { swipeLeftBtn(item) }
            binding.conversationSwipeRight.apply { visibility = View.GONE }.setOnClickListener { swipeRightBtn(item) }

            // Highlight if selected
            binding.rootLayout.setBackgroundColor(
                if (selectedItems.contains(item)) ContextCompat.getColor(binding.root.context, android.R.color.holo_blue_dark)
                else ContextCompat.getColor(binding.root.context, android.R.color.holo_orange_dark)
            )

            // Long press to enable selection mode
            binding.rootLayout.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(item)
                }
                true
            }

            // Click behavior
            binding.rootLayout.setOnClickListener {
                if (isSelectionMode) {
                    // If selection mode is active, toggle selection
                    toggleSelection(item)
                } else {
                    // If not in selection mode, trigger normal click
                    onItemClick(item)
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun toggleSelection(item: ConversationThread) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }

            if (selectedItems.isEmpty()) {
                isSelectionMode = false // Disable selection mode when nothing is selected
            }

            notifyDataSetChanged() // Refresh UI
            onSelectionChanged(selectedItems.toList()) // Send updated selection
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    // Select all items
    fun selectAll() {
        if (currentList.isEmpty()) return

        selectedItems.clear()
        selectedItems.addAll(currentList)//currentList.indices
        isSelectionMode = true
        notifyItemRangeChanged(0, itemCount)
        onSelectionChanged(selectedItems.toList()) // Send selected list
    }

    // Unselect all items
    fun unselectAll() {
        if (selectedItems.isEmpty()) return

        selectedItems.clear()
        isSelectionMode = false
        notifyItemRangeChanged(0, itemCount)
        onSelectionChanged(emptyList()) // Send empty list
    }

    // Get live selected count
    fun getSelectedCount(): Int = selectedItems.size
}