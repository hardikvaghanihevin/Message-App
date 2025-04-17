package com.hardik.messageapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.databinding.FragmentBlockMessageBinding
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.ui.activity.BlockActivity
import com.hardik.messageapp.presentation.ui.viewmodel.BlockViewModel
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [BlockMessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class BlockMessageFragment : BaseFragment(R.layout.fragment_block_message) {
    private val TAG = BASE_TAG + BlockMessageFragment::class.java.simpleName
    private var _binding: FragmentBlockMessageBinding? = null
    private val binding get() = _binding!!

    private val blockBinding get() = (activity as BlockActivity).binding

    private var isAllSelected = false // Track selection state

    private lateinit var blockViewModel: BlockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        blockViewModel = (activity as BlockActivity).blockViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBlockMessageBinding.bind(view)

        (activity as BlockActivity).conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item ->  },
            swipeRightBtn = { item ->  },
            onItemClick = { conversation -> Log.e(TAG, "onCreate: ${conversation}", )},
            onSelectionChanged = { selectedConversations, listSize ->
                Log.e(TAG, "onCreate: ${selectedConversations.size} - $listSize", )
                blockViewModel.onSelectedChanged(selectedConversations)

                (activity as BlockActivity).showBottomMenu(BottomMenu.BOTTOM_MENU_4_DELETE_UNBLOCK).takeIf { selectedConversations.isNotEmpty() } ?: (activity as BlockActivity).hideBottomMenu()
                // Automatically update selection state & drawable
                isAllSelected = selectedConversations.size == listSize
                blockBinding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(
                    if (isAllSelected) R.drawable.ic_all_selected_item else R.drawable.ic_all_unselected_item,
                    0, 0, 0
                )
            }
        )

        binding.recyclerView.adapter = (activity as? BlockActivity)?.conversationAdapter

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
            blockViewModel.blockedConversations.collectLatest { blockConversationList ->
                Log.e(TAG, "onCreate: ${blockConversationList}", )
                //val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                (activity as? BlockActivity)?.conversationAdapter?.setFullList(blockConversationList) {
                    // After setting the new list, scroll back to the previous position
                    if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) }
                    //(activity as BlockActivity).conversationAdapter.filter.filter(blockBinding.toolbarEdtSearch.text.toString())// todo: if any case data update/change so show on query based
                }
            }
        }



        //region bottom menu
        blockBinding.includedNavViewBottomMenu4.navViewBottomLlDelete.setOnClickListener {
            //Log.e(TAG, "onCreate: Delete",)
            //val threadIds = blockViewModel.countSelectedConversationThreads.value.map { it.threadId }
            (activity as BlockActivity).deleteBlockConversation(blockViewModel.countSelectedConversationThreads.value) // delete (permanent) all selected bin threads

            (activity as BlockActivity).conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        blockBinding.includedNavViewBottomMenu4.navViewBottomLlUnblock.setOnClickListener {
            val blockThreads: List<BlockThreadEntity> = blockViewModel.countSelectedConversationThreads.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
            (activity as BlockActivity).unblockConversation(blockThreads = blockThreads) // unblock all selected bin threads

            (activity as BlockActivity).conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        //endregion

    }

    override fun handleSoftBackPress(): Boolean {
        return false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlockMessageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlockMessageFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}