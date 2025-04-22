package com.hardik.messageapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.presentation.adapter.ConversationAdapter

class ConversationSwipeGestureHelper(
    context: Context,
    private val adapter: ConversationAdapter,
    private val leftAction: ((Int) -> Unit)? = null,
    private val rightAction: ((Int) -> Unit)? = null
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var currentlySwipingPosition: Int? = null

    private val swipeActionRightPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.swipe_action_right) }
    private val swipeActionLeftPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.swipe_action_left) }

    val leftSA: SwipeAction.Action = SwipeAction.getAction(SwipeAction.LEFT)
    val rightSA: SwipeAction.Action = SwipeAction.getAction(SwipeAction.RIGHT)

    val actionItems = SwipeAction.getActionItems()

    private val iconDelete = ContextCompat.getDrawable(context, R.drawable.real_ic_delete)
    private val iconEdit = ContextCompat.getDrawable(context, R.drawable.real_ic_archive)

    private val iconMargin = context.resources.getDimensionPixelSize(R.dimen.swipe_icon_margin)
    private val iconSize = context.resources.getDimensionPixelSize(R.dimen.swipe_icon_size)

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val mainView = viewHolder.itemView
        val position = viewHolder.adapterPosition

        val top = mainView.top
        val bottom = mainView.bottom
        val left = mainView.left.toFloat()
        val right = mainView.right.toFloat()

        if (currentlySwipingPosition != null && currentlySwipingPosition != position) {
            resetAllItems(recyclerView)
        }

        if (isCurrentlyActive) {
            currentlySwipingPosition = position
        }

        val itemHeight = bottom - top

        if (dX > 0) {
            // Swipe Right -> Show Edit (left action)
            val rect = RectF(left, top.toFloat(), left + dX, bottom.toFloat())
            c.drawRect(rect, swipeActionLeftPaint)

            iconEdit?.let {
                val iconTop = top + (itemHeight - iconSize) / 2
                val iconLeft = left + iconMargin
                it.setBounds(iconLeft.toInt(), iconTop, (iconLeft + iconSize).toInt(), iconTop + iconSize)
                it.draw(c)
            }

        } else if (dX < 0) {
            // Swipe Left -> Show Delete (right action)
            val rect = RectF(right + dX, top.toFloat(), right, bottom.toFloat())
            c.drawRect(rect, swipeActionRightPaint)

            iconDelete?.let {
                val iconTop = top + (itemHeight - iconSize) / 2
                val iconRight = right - iconMargin
                it.setBounds((iconRight - iconSize).toInt(), iconTop, iconRight.toInt(), iconTop + iconSize)
                it.draw(c)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            when (direction) {
                ItemTouchHelper.LEFT -> rightAction?.invoke(position)
                ItemTouchHelper.RIGHT -> leftAction?.invoke(position)
            }
            adapter.notifyItemChanged(position)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
    }

    private fun resetAllItems(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i) ?: continue
            val viewHolder = recyclerView.getChildViewHolder(child)
            if (viewHolder is ConversationAdapter.ConversationViewHolder) {
                getDefaultUIUtil().clearView(viewHolder.itemView)
                viewHolder.binding.conversationSwipeLeft.visibility = View.GONE
                viewHolder.binding.conversationSwipeRight.visibility = View.GONE
            }
        }
        currentlySwipingPosition = null
    }

    fun getScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                    newState == RecyclerView.SCROLL_STATE_SETTLING
                ) {
                    resetAllItems(recyclerView)
                }
            }
        }
    }
}

//region old gesture
//class ConversationSwipeGestureHelper(
//    context: Context,
//    private val adapter: ConversationAdapter,
//    private val leftAction: ((Int) -> Unit)? = null,
//    private val rightAction: ((Int) -> Unit)? = null
//) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
//
//    private val TAG = ConversationSwipeGestureHelper::class.java.simpleName
//
//    private var currentlySwipingPosition: Int? = null  // Track the currently swiping item
//
//    private val swipeActionRightPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.swipe_action_right) }
//    private val swipeActionLeftPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.swipe_action_left) }
//    private val textPaint = Paint().apply {
//        color = Color.WHITE
//        textSize = 40f
//        textAlign = Paint.Align.CENTER
//    }
//
//    override fun onChildDraw(
//        c: Canvas, recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
//    ) {
//
//        val llView = viewHolder.itemView.findViewById<LinearLayout>(R.id.ll)
//        val mainView = viewHolder.itemView
//        val positionHolder = viewHolder.adapterPosition
//
//        val itemWidth = mainView.width
//
////        val swipeLeftBtn = viewHolder.itemView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.conversationSwipeLeft)
////        val swipeRightBtn = viewHolder.itemView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.conversationSwipeRight)
//
//        val itemTop = llView.top + mainView.top
//        val itemBottom = llView.bottom + mainView.top
//        val itemLeft = mainView.left.toFloat()
//        val itemRight = mainView.right.toFloat()
//
//        // Reset all items when a new swipe attempt starts
//        if (currentlySwipingPosition != null && currentlySwipingPosition != positionHolder) {
//            resetAllItems(recyclerView) // Todo: commenting when need multiple swipe calls
//        }
//        // Store the currently swiping position
//        if (isCurrentlyActive) {
//            currentlySwipingPosition = positionHolder
//        }
//
//        if (dX > 0) { // Swipe Right (Edit)
//
////            swipeLeftBtn.visibility = View.VISIBLE
////            swipeRightBtn.visibility = View.GONE
//
//            val swipeRightLay = RectF(itemLeft, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat())
//
//            c.drawRect(swipeRightLay, swipeActionLeftPaint)
//            //c.drawText("Edit", swipeRightLay.centerX(), swipeRightLay.centerY() + 10, textPaint)
//
//        } else if (dX < 0) { // Swipe Left (Delete)
//
////            swipeRightBtn.visibility = View.VISIBLE
////            swipeLeftBtn.visibility = View.GONE
//
//            val swipeLeftLay = RectF(itemRight + dX, itemTop.toFloat(), itemRight, itemBottom.toFloat() )
//
//            c.drawRect(swipeLeftLay, swipeActionRightPaint)
//            //c.drawText("Delete", swipeLeftLay.centerX(), swipeLeftLay.centerY() + 10, textPaint)
//
//        }else{
//            //Log.e(TAG, "onChildDraw: else | $positionHolder ", )
//        }
//        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//    }
//
//    override fun onMove(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        target: RecyclerView.ViewHolder
//    ): Boolean {
//        val fromPosition = viewHolder.adapterPosition
//        val toPosition = target.adapterPosition
//
//        // Swap items in the adapter's list
//        adapter.notifyItemMoved(fromPosition, toPosition)
//
//        return true
//    }
//
//    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        val position = viewHolder.adapterPosition
//        if (position != RecyclerView.NO_POSITION) {
//            when (direction) {
//                ItemTouchHelper.LEFT -> rightAction?.invoke(position)
//                ItemTouchHelper.RIGHT -> leftAction?.invoke(position)
//            }
//            adapter.notifyItemChanged(position)
//        }
//    }
//
//    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//        super.clearView(recyclerView, viewHolder)
//    }
//
//    /**
//     * Reset all items by hiding the swipe buttons
//     */
//    private fun resetAllItems(recyclerView: RecyclerView) {
//        for (i in 0 until recyclerView.childCount) {
//            val child = recyclerView.getChildAt(i) ?: continue
//            val viewHolder = recyclerView.getChildViewHolder(child)
//
//            if (viewHolder is ConversationAdapter.ConversationViewHolder) {// Todo: put your viewHolder of recyclerView
//                getDefaultUIUtil().clearView(viewHolder.itemView)
//
//                viewHolder.binding.conversationSwipeLeft.visibility = View.GONE
//                viewHolder.binding.conversationSwipeRight.visibility = View.GONE
//            }
//        }
//        currentlySwipingPosition = null
//    }
//
//    /**
//     * Reset all swiped items when scrolling
//     */
//
//    fun getScrollListener(): RecyclerView.OnScrollListener {
//        return object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
//                    newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    resetAllItems(recyclerView) // Todo: commenting when need to scrolling do not rest list or swipe
//                }
//            }
//        }
//    }
//}
//endregion