package com.hardik.messageapp.presentation.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.SwipeGestureHelper
import com.hardik.messageapp.databinding.ActivityMainBinding
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.helper.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.presentation.adapter.DummyAdapter
import com.hardik.messageapp.presentation.viewmodel.BlockViewModel
import com.hardik.messageapp.presentation.viewmodel.ContactViewModel
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.DummyViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import com.hardik.messageapp.presentation.viewmodel.PinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = BASE_TAG + MainActivity::class.java

    private lateinit var binding: ActivityMainBinding
    private val messageViewModel: MessageViewModel by viewModels()
    private val conversationThreadViewModel: ConversationThreadViewModel by viewModels()
    private val contactViewModel: ContactViewModel by viewModels()
    private val blockViewModel: BlockViewModel by viewModels()
    private val pinViewModel: PinViewModel by viewModels()
    
    private val dummyViewModel: DummyViewModel by viewModels()
    private lateinit var adapter: DummyAdapter


    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            messageViewModel.fetchSmsMessages() // Update SMS list when a new message arrives
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isDefaultSmsApp(this)){
            adapter = DummyAdapter(
                onDelete = { item -> deleteItem(item) },
                onEdit = { item -> editItem(item) },
                onSelectionChanged = { count -> updateSelectionCount(count) }
            )
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter

            // Observe Live Data
            lifecycleScope.launch { dummyViewModel.dummyData.collect{ adapter.submitList(it) } }

            // Attach Swipe Gesture
            val swipeHelper = SwipeGestureHelper(this,
                adapter,
                editAction = { position -> editItem(adapter.currentList[position]) },
                deleteAction = { position -> deleteItem(adapter.currentList[position]) }
            )

            val itemTouchHelper = ItemTouchHelper(swipeHelper)
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)

            // Attach scroll listener separately
            binding.recyclerView.addOnScrollListener(swipeHelper.getScrollListener())

        }else{
            Log.e(TAG, "onCreate: do nothing", )
        }
    }

    private fun updateSelectionCount(count: Int) {
        Toast.makeText(this,"Selected: $count", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if(!isDefaultSmsApp(this)){navigateToSetAsDefaultScreen()} //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    private fun attachSwipeGesture1() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.currentList[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> deleteItem(item)  // Swipe left -> Delete
                    ItemTouchHelper.RIGHT -> editItem(item)   // Swipe right -> Edit
                }
                //adapter.notifyItemChanged(position) // Refresh item to prevent it from disappearing
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView//.findViewById<TextView>(R.id.textView) // Get textView reference
                val paint = Paint()

                if (dX > 0) { // Swiping Right (Edit)
                    paint.color = Color.BLUE
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), paint)
                } else if (dX < 0) { // Swiping Left (Delete)
                    paint.color = Color.RED
                    c.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)
                } else { // When dX == 0, clear everything
                    paint.color = Color.TRANSPARENT
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
    private fun attachSwipeGesture2() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.currentList[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> deleteItem(item)  // Swipe left -> Delete
                    ItemTouchHelper.RIGHT -> editItem(item)   // Swipe right -> Edit
                }
                // adapter.notifyItemChanged(position) // Refresh item to prevent it from disappearing
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val llView = viewHolder.itemView.findViewById<LinearLayout>(R.id.ll) // Get the LinearLayout (ll)
                val mainView = viewHolder.itemView
                val paint = Paint()

                // Set background height based on llView instead of mainView
                val itemTop = llView.top + mainView.top // Align with llView
                val itemBottom = llView.bottom + mainView.top // Align with llView height
                val itemLeft = mainView.left.toFloat()
                val itemRight = mainView.right.toFloat()

                if (dX > 0) { // Swiping Right (Edit)
                    paint.color = Color.BLUE
                    c.drawRect(itemLeft, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat(), paint)
                } else if (dX < 0) { // Swiping Left (Delete)
                    paint.color = Color.RED
                    c.drawRect(itemRight + dX, itemTop.toFloat(), itemRight, itemBottom.toFloat(), paint)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun attachSwipeGesture3() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Do nothing here, because we handle swipe manually
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val llView = viewHolder.itemView.findViewById<LinearLayout>(R.id.ll)
                val mainView = viewHolder.itemView
                val paint = Paint()

                // Dimensions for buttons
                val buttonWidth = 200f
                val threshold = mainView.width * 0.5f // 50% threshold

                val itemTop = llView.top + mainView.top
                val itemBottom = llView.bottom + mainView.top
                val itemLeft = mainView.left.toFloat()
                val itemRight = mainView.right.toFloat()

                if (dX > 0) { // Swiping Right (Edit)
                    paint.color = Color.BLUE
                    c.drawRect(itemLeft, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat(), paint)

                    if (dX > threshold) {
                        drawButton(c, "Edit", itemLeft + dX - buttonWidth, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat())
                    }
                } else if (dX < 0) { // Swiping Left (Delete)
                    paint.color = Color.RED
                    c.drawRect(itemRight + dX, itemTop.toFloat(), itemRight, itemBottom.toFloat(), paint)

                    if (Math.abs(dX) > threshold) {
                        drawButton(c, "Delete", itemRight + dX, itemTop.toFloat(), itemRight + dX + buttonWidth, itemBottom.toFloat())
                    }
                }

                // Stop swipe if under 50% threshold
                if (Math.abs(dX) < threshold) {
                    super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.5f // Require at least 50% swipe before action
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return defaultValue * 2 // Increase escape velocity to prevent accidental swipe action
            }

            override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                return defaultValue * 2 // Prevent action unless swipe is really fast
            }

            private fun drawButton(c: Canvas, text: String, left: Float, top: Float, right: Float, bottom: Float) {
                val buttonPaint = Paint().apply { color = Color.WHITE }
                c.drawRect(left, top, right, bottom, buttonPaint)

                val textPaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
                val textX = (left + right) / 2
                val textY = (top + bottom) / 2 - (textPaint.descent() + textPaint.ascent()) / 2
                c.drawText(text, textX, textY, textPaint)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun attachSwipeGesture4() {
        val buttonWidth = 200f
        var buttonInstance: RectF? = null // Store the button's position
        var swipedPosition = -1

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Do nothing, we handle the swipe manually
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val llView = viewHolder.itemView.findViewById<LinearLayout>(R.id.ll)
                val mainView = viewHolder.itemView
                val paint = Paint()

                // Dimensions for buttons
                val threshold = mainView.width * 0.5f // 50% threshold
                val itemTop = llView.top + mainView.top
                val itemBottom = llView.bottom + mainView.top
                val itemLeft = mainView.left.toFloat()
                val itemRight = mainView.right.toFloat()

                if (dX > 0) { // Swiping Right (Edit)
                    paint.color = Color.BLUE
                    c.drawRect(itemLeft, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat(), paint)

                    if (dX > threshold) {
                        buttonInstance = RectF(itemLeft + dX - buttonWidth, itemTop.toFloat(), itemLeft + dX, itemBottom.toFloat())
                        drawButton(c, "Edit", buttonInstance!!)
                        swipedPosition = viewHolder.adapterPosition
                    }
                } else if (dX < 0) { // Swiping Left (Delete)
                    paint.color = Color.RED
                    c.drawRect(itemRight + dX, itemTop.toFloat(), itemRight, itemBottom.toFloat(), paint)

                    if (Math.abs(dX) > threshold) {
                        buttonInstance = RectF(itemRight + dX, itemTop.toFloat(), itemRight + dX + buttonWidth, itemBottom.toFloat())
                        drawButton(c, "Delete", buttonInstance!!)
                        swipedPosition = viewHolder.adapterPosition
                    }
                }

                // Stop swipe if under 50% threshold
                if (Math.abs(dX) < threshold) {
                    super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.5f
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return defaultValue * 2
            }

            override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                return defaultValue * 2
            }

            private fun drawButton(c: Canvas, text: String, buttonRect: RectF) {
                val buttonPaint = Paint().apply { color = Color.WHITE }
                c.drawRect(buttonRect, buttonPaint)

                val textPaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
                val textX = (buttonRect.left + buttonRect.right) / 2
                val textY = (buttonRect.top + buttonRect.bottom) / 2 - (textPaint.descent() + textPaint.ascent()) / 2
                c.drawText(text, textX, textY, textPaint)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        // Intercept touch events to detect button clicks
        binding.recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                buttonInstance?.let {
                    if (it.contains(e.x, e.y)) {
                        // Click detected on button
                        if (it.left < rv.width / 2) {
                            deleteItem(adapter.currentList[swipedPosition]) // Delete button
                        } else {
                            editItem(adapter.currentList[swipedPosition]) // Edit button
                        }
                        buttonInstance = null // Reset button position
                        rv.adapter?.notifyItemChanged(swipedPosition) // Reset item
                        return true
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }//todo: again action then app will crash






    fun editItem(item:String)  {
        Log.i(TAG, "onCreate: edit:- $item")
        dummyViewModel.editItem(item)  // Update dummy data (for demonstration purposes)
    }
    fun deleteItem(item:String)  {
        Log.v(TAG, "onCreate: delete:- $item")
        dummyViewModel.removeItem(item)
    }

}

