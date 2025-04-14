package com.hardik.messageapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.presentation.adapter.ConversationAdapter

class ConversationSwipeGestureHelper(
    context: Context,
    private val adapter: ConversationAdapter,
    private val editAction: ((Int) -> Unit)? = null,
    private val deleteAction: ((Int) -> Unit)? = null
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){

    private val TAG = ConversationSwipeGestureHelper::class.java.simpleName

    private var currentlySwipingPosition: Int? = null  // Track the currently swiping item

    private val swipeActionRightPaint = Paint().apply { color = ContextCompat.getColor(context,
        R.color.swipe_action_right
    ) }
    private val swipeActionLeftPaint = Paint().apply { color = ContextCompat.getColor(context,
        R.color.swipe_action_left
    ) }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val llView = viewHolder.itemView.findViewById<LinearLayout>(R.id.ll)
        val mainView = viewHolder.itemView
        val positionHolder = viewHolder.adapterPosition

        val itemWidth = mainView.width

        val swipeLeftBtn = viewHolder.itemView.findViewById<TextView>(R.id.conversationSwipeLeft)
        val swipeRightBtn = viewHolder.itemView.findViewById<TextView>(R.id.conversationSwipeRight)

        val itemTop = llView.top + mainView.top
        val itemBottom = llView.bottom + mainView.top
        val itemLeft = mainView.left.toFloat()
        val itemRight = mainView.right.toFloat()

        // Reset all items when a new swipe attempt starts
        if (currentlySwipingPosition != null && currentlySwipingPosition != positionHolder) {
            resetAllItems(recyclerView) // Todo: commenting when need multiple swipe calls
        }
        // Store the currently swiping position
        if (isCurrentlyActive) {
            currentlySwipingPosition = positionHolder
        }

        if (dX > 0) { // Swipe Right (Edit)

            swipeLeftBtn.visibility = View.VISIBLE
            swipeRightBtn.visibility = View.GONE

            val swipeRightLay = RectF(itemLeft, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat())

            c.drawRect(swipeRightLay, swipeActionLeftPaint)
            //c.drawText("Edit", swipeRightLay.centerX(), swipeRightLay.centerY() + 10, textPaint)

        } else if (dX < 0) { // Swipe Left (Delete)

            swipeRightBtn.visibility = View.VISIBLE
            swipeLeftBtn.visibility = View.GONE

            val swipeLeftLay = RectF(itemRight + dX, itemTop.toFloat(), itemRight, itemBottom.toFloat() )

            c.drawRect(swipeLeftLay, swipeActionRightPaint)
            //c.drawText("Delete", swipeLeftLay.centerX(), swipeLeftLay.centerY() + 10, textPaint)

        }else{
            //Log.e(TAG, "onChildDraw: else | $positionHolder ", )
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        // Swap items in the adapter's list
        adapter.notifyItemMoved(fromPosition, toPosition)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            when (direction) {
                ItemTouchHelper.LEFT -> deleteAction?.invoke(position)
                ItemTouchHelper.RIGHT -> editAction?.invoke(position)
            }
            adapter.notifyItemChanged(position)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
    }

    /**
     * Reset all items by hiding the swipe buttons
     */
    private fun resetAllItems(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i) ?: continue
            val viewHolder = recyclerView.getChildViewHolder(child)

            if (viewHolder is ConversationAdapter.ConversationViewHolder) {// Todo: put your viewHolder of recyclerView
                getDefaultUIUtil().clearView(viewHolder.itemView)

                viewHolder.binding.conversationSwipeLeft.visibility = View.GONE
                viewHolder.binding.conversationSwipeRight.visibility = View.GONE
            }
        }
        currentlySwipingPosition = null
    }

    /**
     * Reset all swiped items when scrolling
     */

    fun getScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                    newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    resetAllItems(recyclerView) // Todo: commenting when need to scrolling do not rest list or swipe
                }
            }
        }
    }
}
