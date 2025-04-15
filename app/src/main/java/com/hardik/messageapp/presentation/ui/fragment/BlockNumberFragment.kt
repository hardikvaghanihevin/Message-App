package com.hardik.messageapp.presentation.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.FragmentBlockNumberBinding
import com.hardik.messageapp.presentation.adapter.UnblockNumberAdapter
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.ui.activity.BlockActivity
import com.hardik.messageapp.presentation.ui.viewmodel.BlockViewModel
import com.hardik.messageapp.presentation.ui.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockNumberFragment : BaseFragment(R.layout.fragment_block_number) {
    private val TAG = BASE_TAG + BlockNumberFragment::class.java.simpleName
    private var _binding: FragmentBlockNumberBinding? = null
    private val binding get() = _binding!!

    private val blockBinding get() = (activity as BlockActivity).binding

    private var isAllSelected = false // Track selection state

    private lateinit var conversationThreadViewModel: ConversationThreadViewModel
    private lateinit var blockViewModel: BlockViewModel

    private lateinit var numberAdapter: UnblockNumberAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conversationThreadViewModel = (activity as BlockActivity).conversationViewModel
        blockViewModel = (activity as BlockActivity).blockViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBlockNumberBinding.bind(view)

        numberAdapter = UnblockNumberAdapter(
            onItemClick = { item ->
                unblockNumber(listOf(item))
            }
        )
        binding.recyclerView.adapter = numberAdapter

        // Add divider
        val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34f, resources.displayMetrics).toInt()

        binding.recyclerView.apply {
            setPadding(0, marginInPx/2, 0, marginInPx*2)  // Add padding programmatically
            clipToPadding = false            // Allow scrolling into padding
            overScrollMode = View.OVER_SCROLL_NEVER // Disable overscroll effect
            //addItemDecoration(CustomDividerItemDecoration(requireContext(), marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
            addItemDecoration(CustomDividerItemDecoration(requireContext(), marginStartRes = R.dimen.item_recycle_decoration_dp_start, marginEndRes = R.dimen.item_recycle_decoration_dp_end, marginTop = 0, marginBottom = 0))

        }

        lifecycleScope.launch {
            blockViewModel.blockedNumbers.collectLatest { blockNumberList ->
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                numberAdapter.setFullList(blockNumberList) {
                    // After setting the new list, scroll back to the previous position
                    if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) }
                }
            }
        }

    }

    private fun unblockNumber(numbers: List<String>) {
        blockViewModel.unblockNumbers(numbers)

        lifecycleScope.launch {
            blockViewModel.isUnblockNumber.collectLatest { isUnblockNumber ->
                conversationThreadViewModel.fetchConversationThreads(needToUpdate = isUnblockNumber)
            }
        }
    }

    override fun handleSoftBackPress(): Boolean {
        return false
    }
}