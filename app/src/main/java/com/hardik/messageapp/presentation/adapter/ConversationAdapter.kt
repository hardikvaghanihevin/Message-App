package com.hardik.messageapp.presentation.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
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
import com.hardik.messageapp.helper.analyzeSender
import com.hardik.messageapp.presentation.util.IcPlaceholderHelper
import com.hardik.messageapp.presentation.util.TimeFormatterForConversation
import java.util.regex.Pattern

class ConversationAdapter (
    val swipeLeftBtn: (ConversationThread) -> Unit,
    val swipeRightBtn: (ConversationThread) -> Unit,
    private val onItemClick: (ConversationThread) -> Unit, // Normal click callback
    private val onSelectionChanged: (List<ConversationThread>, Int) -> Unit // Selection callback
) : ListAdapter<ConversationThread, ConversationAdapter.ConversationViewHolder>(DIFF_CALLBACK),
    Filterable {
    private val TAG = BASE_TAG + ConversationAdapter::class.java.simpleName

    private var originalList: List<ConversationThread> = listOf()
    fun setFullList(fullList: List<ConversationThread>, commitCallback: () -> Unit) {
        originalList = fullList
        //submitList(fullList, commitCallback)
        submitList(null) { submitList(fullList, commitCallback) }
    }

    private val selectedItems = mutableSetOf<ConversationThread>() // Use item content or unique ID instead of position
    private var isSelectionMode = false

    inner class ConversationViewHolder(val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConversationThread, previousItem: ConversationThread? ,position: Int) {
            binding.apply {
                tvTitle.text = item.displayName
                tvSnippet.text = item.snippet

                val formatter = TimeFormatterForConversation()
                val formattedTime = formatter.formatTimestamp(item.timestamp)

                tvDate.text = formattedTime.trim()//longToString(item.date, DATE_FORMAT_dd_MMM)

                //region Set text style based on read status
                llUnreadCount.visibility = View.VISIBLE.takeIf { item.unSeenCount != 0L } ?: View.GONE
                tvUnreadCount.apply {
                    text = "${item.unSeenCount}".takeIf { item.unSeenCount != 0L } ?: ""
                }

                imgPin.visibility = View.VISIBLE.takeIf { item.isPin } ?: View.GONE

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

                setProfileImageAndText(item) //set Image if available
                setupSwipeActions(item) // swipe action and there clicks
                updateSelectionUI(item) // select/unselect
                setupClickListeners(item) // click listeners

                // Extract and show OTP if available
                val otp = extractOTP(item.snippet)
                showOtpIfAvailable(otp)
            }
        }

        private fun setProfileImageAndText(item: ConversationThread, isSelected: Boolean = false) {
            val senderType = analyzeSender(item.sender)

            var imgUri: Pair<Char, Any> = if (senderType == 1) {
                // From numbers messages
                if (item.contactId != -1) {
                    if (item.photoUri.isNotEmpty()) {
                        Pair('?', item.photoUri)
                    } else {
                        // Set alphabetic placeholder when numbers are saved but the image is not available
                        val placeholder = IcPlaceholderHelper.getPlaceholderDrawable(item.displayName)

                        binding.tvPlaceholderChar.apply {
                            visibility = View.VISIBLE
                            text = placeholder.first.uppercaseChar().toString()
                        }

                        placeholder
                    }
                } else {
                    Pair('?', R.drawable.ic_user)
                }
            } else {
                // From company message
                Pair('?', R.drawable.ic_massage)
            }

            // If selected, use a different drawable for profile
            imgUri = if (isSelected) Pair('?', R.drawable.ic_selected_item) else imgUri.copy()

            // Hide placeholder text if using an image
            if (imgUri.first == '?') { binding.tvPlaceholderChar.visibility = View.GONE }

            // Load profile image using Glide
            Glide.with(binding.ivProfile.context)
                .load(imgUri.second)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(binding.ivProfile)
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
            val isSelected = selectedItems.contains(item)

            binding.rootLayout.background = ContextCompat.getDrawable(context, if (isSelected) R.drawable.bg_conversation_select else R.drawable.bg_conversation_unselect)

            setProfileImageAndText(item, isSelected = isSelected) // Call function separately to set profile

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

//        @SuppressLint("NotifyDataSetChanged")
//        private fun toggleSelection(item: ConversationThread) {
//            if (!selectedItems.add(item)) { selectedItems.remove(item) }
//
//            if (selectedItems.isEmpty()) isSelectionMode = false // Disable selection mode when nothing is selected
//
//            notifyDataSetChanged() // Refresh UI
//            onSelectionChanged(selectedItems.toList()) // Send updated selection
//
//        }
        private fun toggleSelection(item: ConversationThread) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }

            // Enable/disable selection mode based on selection count
            isSelectionMode = selectedItems.isNotEmpty()

            notifyItemChanged(currentList.indexOf(item)) // ✅ Only refresh the clicked item
            onSelectionChanged(selectedItems.toList(), currentList.size)
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
                binding.otpText.apply {
                    text = ContextCompat.getString(this.context, R.string.copy_otp)//otp
                }

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
        if(position in currentList.indices) {
            val previousItem = if (position > 0) currentList[position - 1] else null
            holder.bind(getItem(position), previousItem, position)
        }
    }

    // Select all items
    fun selectAll() {
        if (currentList.isEmpty()) return

        selectedItems.clear()
        selectedItems.addAll(currentList)//currentList.indices
        isSelectionMode = true
        notifyItemRangeChanged(0, itemCount)
        onSelectionChanged(selectedItems.toList(), currentList.size) // Send selected list
    }

    // Unselect all items
    fun unselectAll() {
        if (selectedItems.isEmpty()) return

        // Create a copy to avoid ConcurrentModificationException
        val previouslySelected = selectedItems.toList()

        selectedItems.clear()
        isSelectionMode = false

        // Notify only those items that were previously selected
        previouslySelected.forEach { item ->
            notifyItemChanged(currentList.indexOf(item))
        }

        onSelectionChanged(emptyList(), currentList.size)
    }



    // Get live selected count
    fun getSelectedItemCount(): Int = selectedItems.size
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrBlank()) {
                    originalList
                } else {
                    val query = constraint.toString().lowercase()
                    originalList.filter {
                        it.displayName.lowercase().contains(query) ||
                        it.snippet.lowercase().contains(query) ||
                        it.messageBody.lowercase().contains(query)
                    }
                }

                return FilterResults().apply {
                    values = filteredList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val filteredData = results?.values as? List<ConversationThread> ?: emptyList()
                submitList(filteredData)
            }
        }
    }


}