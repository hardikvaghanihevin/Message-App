package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import android.view.View
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ActivitySwipeActionBinding
import com.hardik.messageapp.presentation.custom_view.showSwipeActionDialog
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.SwipeAction
import com.hardik.messageapp.util.SwipeAction.Companion.getActionItems
import com.hardik.messageapp.util.applyStyledShape
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SwipeActionActivity : BaseActivity() {
    private val TAG = BASE_TAG + SwipeActionActivity::class.java.simpleName

    private lateinit var binding: ActivitySwipeActionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipeActionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSwipeActionsUI()

        //region toolbar management
        binding.toolbarBack.setOnClickListener {
            if (!handleOnSoftBackPress()) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        //endregion toolbar management
    }

    override fun handleOnSoftBackPress() = false

    private fun setupSwipeActionsUI() {
        // Get current actions
        val leftAction: SwipeAction.Action = SwipeAction.getAction(SwipeAction.LEFT)
        val rightAction: SwipeAction.Action = SwipeAction.getAction(SwipeAction.RIGHT)


        val actionItems = getActionItems()

        // Apply to UI
        binding.apply {
            // LEFT
            tvSwipeActionLeftChange.setOnClickListener { showSwipeActionChangeDialog(SwipeAction.LEFT) { setupSwipeActionsUI() } }
            tvSwipeActionLeftChanged.text = actionItems[leftAction]?.first

            val iconLeft = actionItems[leftAction]?.second
            ivSwipeActionLeftChangedIcon.apply { iconLeft?.let { setImageResource(it) } }

            when (leftAction) {
                SwipeAction.Action.NONE -> {
                    ivLeftAction.apply {
                        applyStyledShape(
                            shapeAppearanceOverlayResId = R.style.SwipeActionSelectedLeftBGImage_CornerSmall,
                            strokeColorResId = null,
                            strokeWidthDimenResId = null,
                            paddingEndResId = R.dimen.swipe_action_bg_item_padding_plus
                        )
                    }
                    llLeftActionIconText.visibility = View.GONE
                }

                else -> {
                    ivLeftAction.apply {
                        applyStyledShape(
                            shapeAppearanceOverlayResId = R.style.SwipeActionSelectedLeftBGImage,
                            strokeColorResId = null,
                            strokeWidthDimenResId = null,
                            paddingEndResId = R.dimen.swipe_action_bg_item_padding_minus
                        )
                    }
                    llLeftActionIconText.visibility = View.VISIBLE
                }
            }


            // RIGHT
            tvSwipeActionRightChange.setOnClickListener { showSwipeActionChangeDialog(SwipeAction.RIGHT) { setupSwipeActionsUI() } }
            tvSwipeActionRightChanged.text = actionItems[rightAction]?.first

            val iconRight = actionItems[rightAction]?.second
            ivSwipeActionRightChangedIcon.apply { iconRight?.let { setImageResource(it) } }

            when (rightAction) {
                SwipeAction.Action.NONE -> {
                    ivRightAction.apply {
                        applyStyledShape(
                            shapeAppearanceOverlayResId = R.style.SwipeActionSelectedRightBGImage_CornerSmall,
                            strokeColorResId = null,
                            strokeWidthDimenResId = null,
                            paddingStartResId = R.dimen.swipe_action_bg_item_padding_plus
                        )
                    }
                    llRightActionIconText.visibility = View.GONE
                }

                else -> {
                    ivRightAction.apply {
                        applyStyledShape(
                            shapeAppearanceOverlayResId = R.style.SwipeActionSelectedRightBGImage,
                            strokeColorResId = null,
                            strokeWidthDimenResId = null,
                            paddingStartResId = R.dimen.swipe_action_bg_item_padding_minus

                        )
                    }
                    llRightActionIconText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showSwipeActionChangeDialog(swipeAction: SwipeAction, onConfirm: (isPositive: Boolean) -> Unit) {
        showSwipeActionDialog(this@SwipeActionActivity, swipeAction) { onConfirm(it) }
    }

}