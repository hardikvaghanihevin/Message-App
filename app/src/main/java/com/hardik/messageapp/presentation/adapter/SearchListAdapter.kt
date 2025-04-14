package com.hardik.messageapp.presentation.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ItemSearchContactBinding
import com.hardik.messageapp.databinding.ItemSearchHeaderBinding
import com.hardik.messageapp.databinding.ItemSearchMessageBinding
import com.hardik.messageapp.domain.model.SearchItem
import com.hardik.messageapp.domain.model.SearchItem.Companion.DIFF_CALLBACK
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.DateUtil
import java.util.regex.Pattern

class SearchListAdapter(
    private var searchQuery: String = "",
    private val onItemClick: (SearchItem) -> Unit,
) : ListAdapter<SearchItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    private val TAG = BASE_TAG + SearchListAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_MESSAGE -> MessageViewHolder(ItemSearchMessageBinding.inflate(inflater, parent, false))
            ITEM_CONTACT -> ContactViewHolder(ItemSearchContactBinding.inflate(inflater, parent, false))
            ITEM_HEADER -> HeaderViewHolder(ItemSearchHeaderBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val query = searchQuery.trim() // Always get the latest query

        when (holder) {
            is MessageViewHolder -> holder.bind(item as SearchItem.MessageItem, query)
            is ContactViewHolder -> holder.bind(item as SearchItem.ContactItem, query)
            is HeaderViewHolder -> holder.bind(item as SearchItem.Header)
        }
    }

    inner class MessageViewHolder(private val binding: ItemSearchMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: SearchItem.MessageItem, query: String) {
            binding.tvTitle.text = highlightQuery(item.message.displayName, query, textColor = ContextCompat.getColor(binding.tvTitle.context, R.color.color_cursor_edittext))
            binding.tvSnippet.text = highlightQuery(item.message.messageBody, query, textColor = ContextCompat.getColor(binding.tvSnippet.context, R.color.color_cursor_edittext))
            binding.tvMatchFoundCountOtpLayout.text = "Match found: ${item.message.matchFoundCount}"
            binding.tvDate.text = DateUtil.longToString(item.message.dateSent, DateUtil.DATE_FORMAT_dd_MMM)

            Glide.with(binding.ivProfile.context)
                .load(item.message.photoUri)
                .placeholder(R.drawable.real_ic_user)
                .error(R.drawable.real_ic_user)
                .into(binding.ivProfile)

            itemView.setOnClickListener {
                onItemClick(item)
            }

            // Extract and show OTP if available
            val otp = extractOTP(item.message.messageBody)
            showOtpIfAvailable(otp)
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
                binding.tvOtpOtpLayout.text = otp

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

    inner class ContactViewHolder(private val binding: ItemSearchContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem.ContactItem, query: String) {
            binding.tvContactName.text = highlightQuery(item.contact.displayName, query, textColor = ContextCompat.getColor(binding.tvContactName.context, R.color.color_cursor_edittext))
            binding.tvContactNumber.text = highlightQuery(item.contact.phoneNumbers.joinToString(), query, textColor = ContextCompat.getColor(binding.tvContactNumber.context, R.color.color_cursor_edittext))

            Glide.with(binding.ivContactPhoto.context)
                .load(item.contact.photoUri)
                .placeholder(R.drawable.real_ic_user)
                .error(R.drawable.real_ic_user)
                .into(binding.ivContactPhoto)

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    inner class HeaderViewHolder(val binding: ItemSearchHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem.Header) {
            binding.tvHeader.text = item.title
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


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchItem.MessageItem -> ITEM_MESSAGE
            is SearchItem.ContactItem -> ITEM_CONTACT
            is SearchItem.Header -> ITEM_HEADER
        }
    }

    companion object {
        private const val ITEM_MESSAGE = 1
        private const val ITEM_CONTACT = 2
        private const val ITEM_HEADER = 3
    }
}

