package com.hardik.messageapp.presentation.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ItemContactBinding
import com.hardik.messageapp.databinding.ItemContactSearchBinding
import com.hardik.messageapp.databinding.ItemHeaderContactBinding
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Contact.Companion.DIFF_CALLBACK
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.IcPlaceholderHelper
import com.hardik.messageapp.util.getBestMatchedNumber
import java.util.regex.Pattern

class ContactAdapter(
    private var searchQuery: String = "",
    private val onItemClick: (Contact) -> Unit, // Normal click callback
) : ListAdapter<Contact, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    private val TAG = BASE_TAG + ContactAdapter::class.java.simpleName

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchQuery(newQuery: String) {
        if (searchQuery != newQuery) { // Avoid redundant updates
            searchQuery = newQuery
            notifyDataSetChanged() // Force full refresh
        }
    }

    inner class ContactViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Contact, query: String, position: Int) {
            binding.tvContactName.text = item.displayName

            setProfileImageAndTextChar(binding.asContactViews, item, position = position)

            itemView.setOnClickListener { onItemClick(item) }
        }

    }

    inner class ContactSearchViewHolder(val binding: ItemContactSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Contact, query: String, position: Int) {
            val number: String = getBestMatchedNumber(item.phoneNumbers, query) ?: item.normalizeNumber
            binding.tvContactName.text = highlightQuery(item.displayName, query, textColor = ContextCompat.getColor(binding.tvContactName.context, R.color.color_cursor_edittext))
            binding.tvContactNumber.text = highlightQuery(number, query, textColor = ContextCompat.getColor(binding.tvContactNumber.context, R.color.color_cursor_edittext))

            setProfileImageAndTextChar(binding.asContactViews, item, position = position)

            itemView.setOnClickListener { onItemClick(item) }
        }

    }

    inner class ContactHeaderViewHolder(val binding: ItemHeaderContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Contact, query: String) {
            binding.tvAlphabetHeader.text = item.displayName.first().uppercase()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_HEADER -> ContactHeaderViewHolder(ItemHeaderContactBinding.inflate(inflater, parent, false))
            ITEM_CONTACT ->  ContactViewHolder(ItemContactBinding.inflate(inflater, parent, false))
            ITEM_CONTACT_SEARCH -> ContactSearchViewHolder(ItemContactSearchBinding.inflate(inflater, parent, false))
           else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: Contact = getItem(position)
        val query = searchQuery.trim()

        when (holder) {
            is ContactHeaderViewHolder -> holder.bind(item, query)
            is ContactViewHolder -> holder.bind(item, query, position)
            is ContactSearchViewHolder -> holder.bind(item, query, position)
        }
    }

    private fun setProfileImageAndTextChar(binding: ContactViews, item: Contact, position: Int, isSelected: Boolean = false) {

        if (item.photoUri != null){
            // Load profile image using Glide
            Glide.with(binding.ivProfile.context)
                .load(item.photoUri)
                .placeholder(R.drawable.real_ic_user)
                .error(R.drawable.real_ic_user)
                .into(binding.ivProfile)

            binding.tvPlaceholderChar.visibility = View.GONE

        }else{

            // Set alphabetic placeholder when numbers are saved but the image is not available
            val placeholder = IcPlaceholderHelper.placeholderList[(position % 12)]

            binding.tvPlaceholderChar.apply {
                visibility = View.VISIBLE
                text = item.displayName.first().uppercaseChar().toString()
            }

            // Load profile image using Glide
            Glide.with(binding.ivProfile.context)
                .load(placeholder)
                .placeholder(R.drawable.real_ic_user)
                .error(R.drawable.real_ic_user)
                .into(binding.ivProfile)

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
        val item = getItem(position)
        return when {
            item.isHeader -> ITEM_HEADER
            searchQuery.isNotEmpty() -> ITEM_CONTACT_SEARCH
            else -> ITEM_CONTACT
        }
    }

    interface ContactViews {
        val ivProfile: ImageView
        val tvPlaceholderChar: TextView
    }

    val ItemContactBinding.asContactViews: ContactViews
        get() = object : ContactViews {
            override val ivProfile: ImageView = this@asContactViews.ivProfile
            override val tvPlaceholderChar: TextView = this@asContactViews.tvPlaceholderChar
        }

    val ItemContactSearchBinding.asContactViews: ContactViews
        get() = object : ContactViews {
            override val ivProfile: ImageView = this@asContactViews.ivProfile
            override val tvPlaceholderChar: TextView = this@asContactViews.tvPlaceholderChar
        }
    companion object {
        private const val ITEM_HEADER = 1
        private const val ITEM_CONTACT = 2
        private const val ITEM_CONTACT_SEARCH = 3
    }
}