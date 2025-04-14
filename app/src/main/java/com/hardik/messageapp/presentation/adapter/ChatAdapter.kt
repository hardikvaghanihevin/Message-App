package com.hardik.messageapp.presentation.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ItemChatMessageBinding
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.model.Message.Companion.DIFF_CALLBACK
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.DateUtil
import com.hardik.messageapp.util.TimeFormatter
import java.util.regex.Pattern

class ChatAdapter(
    private var searchQuery: String = "",
    private val onItemClick: (Message) -> Unit, // Normal click callback
    private val onSelectionChanged: (List<Message>) -> Unit // Selection callback
) : ListAdapter<Message, ChatAdapter.ChatViewHolder>(DIFF_CALLBACK) {
    private val TAG = BASE_TAG + ChatAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        //holder.bind(getItem(position))
        if (position in currentList.indices) {
            val previousItem = if (position > 0) currentList[position - 1] else null
            holder.bind(currentList[position], previousItem, position)
        }
    }

    private val selectedItems = mutableSetOf<Message>() // Use item content or unique ID instead of position
    private var isSelectionMode = false

    inner class ChatViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val tvChatDate: TextView = binding.tvChatDate
        private val spacerTvChatDate: Space = binding.spacerTvChatDate
        private val tvMessageReceive: TextView = binding.tvMessageReceive
        private val tvTimeReceive: TextView = binding.tvTimeReceive
        private val tvMessageSend: TextView = binding.tvMessageSend
        private val tvTimeSend: TextView = binding.tvTimeSend
        private val llChatReceive: View = binding.llChatReceive
        private val llChatSend: View = binding.llChatSend

        fun bind(item: Message, previousItem: Message?, position: Int) {
            val isSent = item.type == MESSAGE_TYPE_SENT
            val messageView = if (isSent) tvMessageSend else tvMessageReceive
            val timeView = if (isSent) tvTimeSend else tvTimeReceive
            val visibleContainer = if (isSent) llChatSend else llChatReceive
            val hiddenContainer = if (isSent) llChatReceive else llChatSend

            //region Reset all views to default states
            messageView.text = ""
            messageView.visibility = View.GONE
            timeView.visibility = View.GONE
            visibleContainer.visibility = View.GONE
            hiddenContainer.visibility = View.GONE
            //endregion

            //region Handle Message Visibility and Time Visibility
            if (item.messageBody.isNotEmpty()) {
                visibleContainer.visibility = View.VISIBLE
                messageView.text = highlightQuery(item.messageBody, query = searchQuery, textColor = ContextCompat.getColor(messageView.context, R.color.color_cursor_edittext))
                messageView.visibility = View.VISIBLE
                timeView.text = DateUtil.longToString(item.timestamp, DateUtil.TIME_FORMAT_hh_mm_a)
            }
            //endregion

            //region Handle Date Visibility
            val currentDate = DateUtil.longToString(item.timestamp, DateUtil.DATE_FORMAT_dd_MMM_yyyy)
            val previousDate = previousItem?.let { DateUtil.longToString(it.timestamp, DateUtil.DATE_FORMAT_dd_MMM_yyyy) }
            val formattedTimestamp = TimeFormatter().formatTimestamp(item.timestamp)

            val isSameDate = currentDate == previousDate
            val shouldShowDate = !formattedTimestamp.isNullOrEmpty() && item.messageBody.isNotEmpty()

            tvChatDate.apply {
                visibility = if (isSameDate) View.GONE else View.VISIBLE.takeIf { shouldShowDate } ?: View.GONE
                text = formattedTimestamp
            }
            spacerTvChatDate.visibility = if (isSameDate || !shouldShowDate) View.VISIBLE else View.GONE
            //endregion

            // Reset background properly
            updateBackground(messageView, item)

            //region Handle click actions
            messageView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(item)
                } else {
                    onItemClick(item)
                    timeView.visibility = if (timeView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }

            messageView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                }
                toggleSelection(item)
                true
            }
            //endregion

            // Extract and show OTP if available
            val otp = extractOTP(item.messageBody)
            showOtpIfAvailable(otp)
        }
        // Function to update the background dynamically
        private fun updateBackground(view: View, item: Message) {
            val bgRes = when {
                selectedItems.contains(item) -> R.drawable.bg_chat_select
                item.type == MESSAGE_TYPE_SENT -> R.drawable.bg_chat_send
                else -> R.drawable.bg_chat_receive
            }
            view.setBackgroundResource(bgRes)
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun toggleSelection(item: Message) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }

            if (selectedItems.isEmpty()) {
                isSelectionMode = false
            }

            notifyItemChanged(adapterPosition) // Refresh only the changed item
            onSelectionChanged(selectedItems.toList()) // Send updated selection
        }

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
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchQuery(newQuery: String) {
        if (searchQuery != newQuery) { // Avoid redundant updates
            searchQuery = newQuery
            notifyDataSetChanged() // Force full refresh
        }
    }

    private fun highlightQuery(text: String, query: String,
                               textColor: Int = Color.BLUE,
                               backgroundColor: Int = Color.TRANSPARENT
    ): SpannableString {
        val spannable = SpannableString(text)

        // Remove previous highlights
        spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
            .forEach { spannable.removeSpan(it) }

        spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
            .forEach { spannable.removeSpan(it) }

        if (query.isBlank()) return spannable

        val pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        while (matcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(textColor),
                matcher.start(),
                matcher.end(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                BackgroundColorSpan(backgroundColor),
                matcher.start(),
                matcher.end(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}