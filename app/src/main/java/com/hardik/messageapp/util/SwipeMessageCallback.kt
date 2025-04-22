package com.hardik.messageapp.util
/*

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ht.all.video.downloader.app.message.R
import com.ht.all.video.downloader.app.message.data.model.BlockNumberModel
import com.ht.all.video.downloader.app.message.data.model.Conversation
import com.ht.all.video.downloader.app.message.presentation.ui.adapter.ConversationListAdapter
import com.ht.all.video.downloader.app.message.presentation.viewmodel.MessageViewModel
import com.ht.all.video.downloader.app.message.utils.constant.Constants
import com.ht.all.video.downloader.app.message.utils.preference.SharePreference


class SwipeMessageCallback(
    private val context: Context,
    private val adapter: ConversationListAdapter,
    private val messageViewModel: MessageViewModel
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var rightSwipeAction: String = SharePreference.getRightSwipePref(context, Constants.RIGHT_SWIPE_PREFERENCE).toString()
    private var leftSwipeAction: String = SharePreference.getLeftSwipePref(context, Constants.LEFT_SWIPE_PREFERENCE).toString()
    private var leftSideIcon: Drawable = ContextCompat.getDrawable(context, getActionIcon(rightSwipeAction))!!
    private var rightSideIcon: Drawable = ContextCompat.getDrawable(context, getActionIcon(leftSwipeAction))!!
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int
    private val background = ColorDrawable()

    init {
        intrinsicWidth = leftSideIcon.intrinsicWidth
        intrinsicHeight = leftSideIcon.intrinsicHeight
        background.color = ContextCompat.getColor(context, R.color.right_swipe_color)
        Log.d("SwipeMessageCallback", "Initialized with rightSwipeAction: $rightSwipeAction, leftSwipeAction: $leftSwipeAction")
    }

    fun updateSwipeActions() {
        rightSwipeAction = SharePreference.getRightSwipePref(context, Constants.RIGHT_SWIPE_PREFERENCE).toString()
        leftSwipeAction = SharePreference.getLeftSwipePref(context, Constants.LEFT_SWIPE_PREFERENCE).toString()
        leftSideIcon = ContextCompat.getDrawable(context, getActionIcon(rightSwipeAction))!!
        rightSideIcon = ContextCompat.getDrawable(context, getActionIcon(leftSwipeAction))!!
        Log.d("SwipeMessageCallback", "Updated actions: rightSwipeAction: $rightSwipeAction, leftSwipeAction: $leftSwipeAction")
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder.adapterPosition == RecyclerView.NO_POSITION) {
            return 0
        }

        val swipeFlags = when {
            rightSwipeAction == Constants.None_Action && leftSwipeAction == Constants.None_Action -> 0
            rightSwipeAction == Constants.None_Action -> ItemTouchHelper.LEFT
            leftSwipeAction == Constants.None_Action -> ItemTouchHelper.RIGHT
            else -> ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }

        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return

        val conversation = adapter.currentList[position]
        (context as? Activity)?.let {
            when (direction) {
                ItemTouchHelper.RIGHT -> handleRightSwipeAction(conversation)
                ItemTouchHelper.LEFT -> handleLeftSwipeAction(conversation)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView

        // Convert 20dp to pixels
        val iconSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            25f,
            context.resources.displayMetrics
        ).toInt()

        val iconMargin = (itemView.height - iconSize) / 2

        when {
            dX > 0 -> { // Swiping right
                val rightSwipeBg = ContextCompat.getDrawable(context, R.drawable.right_swipe_bg)
                rightSwipeBg?.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                rightSwipeBg?.draw(c)

                val leftMargin = 50

                leftSideIcon.setBounds(
                    itemView.left + leftMargin,
                    itemView.top + iconMargin,
                    itemView.left + leftMargin + iconSize,
                    itemView.top + iconMargin + iconSize
                )
                leftSideIcon.draw(c)
            }

            dX < 0 -> { // Swiping left
                val leftSwipeBg = ContextCompat.getDrawable(context, R.drawable.left_swipe_bg)
                leftSwipeBg?.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                leftSwipeBg?.draw(c)

                val rightMargin = 50

                rightSideIcon.setBounds(
                    itemView.right - rightMargin - iconSize,
                    itemView.top + iconMargin,
                    itemView.right - rightMargin,
                    itemView.top + iconMargin + iconSize
                )
                rightSideIcon.draw(c)
            }

            else -> {
                background.setBounds(0, 0, 0, 0)
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.all_screen_bg))
            }
        }
        updateSwipeActions()
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun getActionIcon(actionType: String):Int{
        Log.d("TAG", "getActionIcon: >>>>>>>>>>>>>>>>>>>>>>>>>>> actionType $actionType")
        return  when (actionType) {
            Constants.Archive_Action -> R.drawable.archived_swipe_icon
            Constants.Delete_Action -> R.drawable.delete_swipe_icon
            Constants.Block_Action -> R.drawable.swipe_block_icon
            Constants.Mark_Read_Action -> R.drawable.swipe_mark_read_icon
            Constants.Mark_Unread_Action -> R.drawable.swipe_mark_unread_icon
            else -> {R.drawable.archived_swipe_icon}
        }
    }

    private fun handleRightSwipeAction(conversation: Conversation) {
        when (rightSwipeAction) {
            Constants.Archive_Action ->
                messageViewModel.updateArchivedStatus(listOf(conversation.threadId), true)

            Constants.Delete_Action ->
                messageViewModel.deleteConversation(listOf(conversation.threadId), true)

            Constants.Block_Action ->{
                messageViewModel.updateBlockStatus(listOf(conversation.threadId),true)
                messageViewModel.insertBlockNumberList(listOf(BlockNumberModel(
                    id = null,
                    number = conversation.phoneNumber,
                    numberToCompare = conversation.phoneNumber,
                    contactName = conversation.title,
                    contactId = conversation.threadId
                )))
            }

            Constants.Mark_Read_Action ->
                messageViewModel.markRead((conversation.threadId))

            Constants.Mark_Unread_Action ->
                messageViewModel.markUnread((conversation.threadId))

        }
    }

    private fun handleLeftSwipeAction(conversation: Conversation) {
        when (leftSwipeAction) {
            Constants.Archive_Action ->
                messageViewModel.updateArchivedStatus(listOf(conversation.threadId), true)
            Constants.Delete_Action ->
                messageViewModel.deleteConversation(listOf(conversation.threadId), true)
            Constants.Block_Action ->{
                messageViewModel.updateBlockStatus(listOf(conversation.threadId),true)
                messageViewModel.insertBlockNumberList(listOf(BlockNumberModel(
                    id = null,
                    number = conversation.phoneNumber,
                    numberToCompare = conversation.phoneNumber,
                    contactName = conversation.title,
                    contactId = conversation.threadId
                )))
            }

            Constants.Mark_Read_Action ->
                messageViewModel.markRead((conversation.threadId))

            Constants.Mark_Unread_Action ->
                messageViewModel.markUnread((conversation.threadId))

        }
    }


}*/
