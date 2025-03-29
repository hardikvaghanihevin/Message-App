package com.hardik.messageapp.presentation.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ItemConversationBinding
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.ConversationThread.Companion.DIFF_CALLBACK
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.util.DateUtil.DATE_FORMAT_dd_MMM
import com.hardik.messageapp.presentation.util.DateUtil.longToString
import java.util.regex.Pattern

class ConversationAdapter (
    val swipeLeftBtn: (ConversationThread) -> Unit,
    val swipeRightBtn: (ConversationThread) -> Unit,
    private val onItemClick: (ConversationThread) -> Unit, // Normal click callback
    private val onSelectionChanged: (List<ConversationThread>) -> Unit // Selection callback
) : ListAdapter<ConversationThread, ConversationAdapter.ConversationViewHolder>(DIFF_CALLBACK) {
    private val TAG = BASE_TAG + ConversationAdapter::class.java.simpleName

    private val selectedItems = mutableSetOf<ConversationThread>() // Use item content or unique ID instead of position
    private var isSelectionMode = false

    inner class ConversationViewHolder(val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConversationThread, position: Int) {
            binding.apply {
                tvTitle.text = item.displayName
                tvSnippet.text = item.snippet
                tvDate.text = longToString(item.date, DATE_FORMAT_dd_MMM)

                //region Set text style based on read status
                if (item.read) {
                    // Item is read - use normal text style
                    tvTitle.setTypeface(null, Typeface.NORMAL)
                    tvSnippet.setTypeface(null, Typeface.NORMAL)
                } else {
                    // Item is unread - use bold text style
                    tvTitle.setTypeface(null, Typeface.BOLD)
                    tvSnippet.setTypeface(null, Typeface.BOLD)
                }
                //endregion

                //region set Image if available
                Glide.with(ivProfile.context)
                    .load(item.photoUri)
                    .placeholder(R.drawable.dummy_ic_user)
                    .error(R.drawable.dummy_ic_user)
                    .into(ivProfile)
                //endregion

                setupSwipeActions(item) // swipe action and there clicks
                updateSelectionUI(item) // select/unselect
                setupClickListeners(item) // click listeners

                // Extract and show OTP if available
                val otp = extractOTP(item.snippet)
                showOtpIfAvailable(otp)
            }
        }

        private fun setupSwipeActions(item: ConversationThread) {
            binding.conversationSwipeLeft.apply {
                visibility = View.GONE
                setOnClickListener { swipeLeftBtn(item) }
            }
            binding.conversationSwipeRight.apply {
                visibility = View.GONE
                setOnClickListener { swipeRightBtn(item) }
            }
        }

        private fun updateSelectionUI(item: ConversationThread) {
            val context = binding.rootLayout.context
            val drawableRes = if (selectedItems.contains(item)) {
                R.drawable.bg_conversation_select
            } else {
                R.drawable.bg_conversation_unselect
            }
            binding.rootLayout.background = ContextCompat.getDrawable(context, drawableRes)
        }

        private fun setupClickListeners(item: ConversationThread) {
            binding.rootLayout.setOnClickListener {
                if (isSelectionMode) toggleSelection(item) else onItemClick(item)
            }
            binding.rootLayout.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(item)
                }
                true
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun toggleSelection(item: ConversationThread) {
            if (!selectedItems.add(item)) { selectedItems.remove(item) }

            if (selectedItems.isEmpty()) isSelectionMode = false // Disable selection mode when nothing is selected

            notifyDataSetChanged() // Refresh UI
            onSelectionChanged(selectedItems.toList()) // Send updated selection

        }

        //region OTP Functions
        private fun extractOTP(message: String): String? {
            val otpPattern = "\\b\\d{4,8}\\b" // Match 4 to 8 digit numbers
            val matcher = Pattern.compile(otpPattern).matcher(message)

            // Check if the message contains "otp" (case-insensitive)
            return if (message.contains("otp", ignoreCase = true) && matcher.find()) {
                matcher.group() // Return OTP if found
            } else {
                null // Return null if "OTP" is not present in the message
            }
        }

        private fun showOtpIfAvailable(otp: String?) {
            if (otp.isNullOrEmpty()) {
                binding.otpLayout.visibility = View.GONE
            } else {
                binding.otpLayout.visibility = View.VISIBLE
                binding.otpText.text = otp

                // Handle OTP click (copy to clipboard)
                binding.otpLayout.setOnClickListener {
                    copyToClipboard(otp)
                    Toast.makeText(binding.root.context, "OTP copied: $otp", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun copyToClipboard(text: String) {
            val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("OTP", text)
            clipboard.setPrimaryClip(clip)
        }
        //endregion

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