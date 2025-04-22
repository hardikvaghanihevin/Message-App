package com.hardik.messageapp.presentation.custom_view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.DialogBlockConversationBinding
import com.hardik.messageapp.databinding.DialogDeleteConversationBinding
import com.hardik.messageapp.databinding.DialogMoveConversationToBinBinding
import com.hardik.messageapp.databinding.DialogSelectSwipeActionBinding
import com.hardik.messageapp.databinding.ItemOptionSelectLay01Binding
import com.hardik.messageapp.util.SwipeAction
import com.hardik.messageapp.util.dpToPx


fun showMoveConversationToBinDialog(activity: AppCompatActivity, onConfirm: (alsoBlock: Boolean, isPositive: Boolean) -> Unit) {
    val dialogView = activity.layoutInflater.inflate(R.layout.dialog_move_conversation_to_bin, null)
    val dialogBinding = DialogMoveConversationToBinBinding.bind(dialogView)

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)

    dialog.window?.apply {
        setLayout((activity.resources.displayMetrics.widthPixels * 0.95).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.BOTTOM)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background + rounded corners
        attributes.apply {
            // Add bottom margin of 16dp
            y = 16.dpToPx(activity) //resources.getDimensionPixelSize(R.dimen.activity_margin)
        }
        setWindowAnimations(R.style.DialogAnimation)
    }

    dialog.show()

    // Click listeners (fill as needed)
    dialogBinding.apply {
        var isBlockSelected = false // default false

        dialogBinding.dialogTvAlsoBlock.apply {
            setOnClickListener {
                isBlockSelected = !isBlockSelected // toggle state

                val drawableRes = if (isBlockSelected) { R.drawable.ic_round_selected_item }
                else { R.drawable.ic_round_unselected_item }

                setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, drawableRes), null, null, null)
            }
        }

        dialogButtonNegative.setOnClickListener {
            dialog.dismiss()
            onConfirm(false, false)
        }

        dialogButtonPositive.setOnClickListener {
            // Move to bin logic
            dialog.dismiss()
            onConfirm(isBlockSelected, true)
        }
    }
}


fun showBlockConversationDialog(activity: AppCompatActivity, onConfirm: (isPositive: Boolean) -> Unit) {
    val dialogView = activity.layoutInflater.inflate(R.layout.dialog_block_conversation, null)
    val dialogBinding = DialogBlockConversationBinding.bind(dialogView)

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)

    dialog.window?.apply {
        setLayout((activity.resources.displayMetrics.widthPixels * 0.95).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.BOTTOM)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background + rounded corners
        attributes.apply {
            // Add bottom margin of 16dp
            y = 16.dpToPx(activity) //resources.getDimensionPixelSize(R.dimen.activity_margin)
        }
        setWindowAnimations(R.style.DialogAnimation)
    }

    dialog.show()

    // Click listeners (fill as needed)
    dialogBinding.apply {

        dialogButtonNegative.setOnClickListener {
            dialog.dismiss()
            onConfirm(false)
        }

        dialogButtonPositive.setOnClickListener {
            // Move to bin logic
            dialog.dismiss()
            onConfirm(true)
        }


    }
}


fun showDeletePermanentConversationDialog(
    activity: AppCompatActivity,
    count: Int,
    isAllSelected: Boolean = false,
    onConfirm: (isPositive: Boolean) -> Unit
) {
    val dialogView = activity.layoutInflater.inflate(R.layout.dialog_delete_conversation, null)
    val dialogBinding = DialogDeleteConversationBinding.bind(dialogView)

    val message = if (isAllSelected) {
        activity.resources.getString(R.string.permanently_delete_all_conversations)
    } else {
        activity.resources.getQuantityString(
            R.plurals.permanently_delete_conversations,
            count,
            count
        )
    }

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)

    dialog.window?.apply {
        setLayout((activity.resources.displayMetrics.widthPixels * 0.95).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.BOTTOM)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background + rounded corners
        attributes.apply {
            // Add bottom margin of 16dp
            y = 16.dpToPx(activity) //resources.getDimensionPixelSize(R.dimen.activity_margin)
        }
        setWindowAnimations(R.style.DialogAnimation)
    }


    dialog.show()

    // Click listeners (fill as needed)
    dialogBinding.apply {
        dialogInfo.apply { text = message }

        dialogButtonNegative.setOnClickListener {
            dialog.dismiss()
            onConfirm(false)
        }

        dialogButtonPositive.setOnClickListener {
            // Move to bin logic
            dialog.dismiss()
            onConfirm(true)
        }


    }
}


fun showSwipeActionDialog(activity: AppCompatActivity, swipeAction: SwipeAction, onConfirm: (isPositive: Boolean) -> Unit) {
    val dialogView = activity.layoutInflater.inflate(R.layout.dialog_select_swipe_action, null)
    val dialogBinding = DialogSelectSwipeActionBinding.bind(dialogView)

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)

    dialog.window?.apply {
        setLayout((activity.resources.displayMetrics.widthPixels * 0.95).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        setGravity(Gravity.BOTTOM)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background + rounded corners
        attributes.apply {
            // Add bottom margin of 16dp
            y = 16.dpToPx(activity) //resources.getDimensionPixelSize(R.dimen.activity_margin)
        }
        setWindowAnimations(R.style.DialogAnimation)
    }


    dialog.show()

    val selectedSwipeAction = SwipeAction.getAction(swipeAction)
    val selectedItem = R.drawable.ic_round_selected_item

    // Click listeners (fill as needed)
    dialogBinding.apply {
        dialogTitle.apply {
            text = when(swipeAction){
                SwipeAction.LEFT -> { resources.getString(R.string.select_left_swipe_action) }
                SwipeAction.RIGHT -> { resources.getString(R.string.select_right_swipe_action) }
            }
        }
        val actionNone = ItemOptionSelectLay01Binding.bind(includedItemActionNone.root)
        val actionArchive = ItemOptionSelectLay01Binding.bind(includedItemActionArchive.root)
        val actionDelete = ItemOptionSelectLay01Binding.bind(includedItemActionDelete.root)
        val actionCall = ItemOptionSelectLay01Binding.bind(includedItemActionCall.root)
        val actionBlock = ItemOptionSelectLay01Binding.bind(includedItemActionBlock.root)
        val actionMarkAsRead = ItemOptionSelectLay01Binding.bind(includedItemActionMarkAsRead.root)
        val actionMarkAsUnread = ItemOptionSelectLay01Binding.bind(includedItemActionMarkAsUnread.root)

        val actionItems = mapOf(
            SwipeAction.Action.NONE to actionNone.apply { tvItemOptionTitle.text = activity.getString(R.string.action_none) },
            SwipeAction.Action.ARCHIVE to actionArchive.apply { tvItemOptionTitle.text = activity.getString(R.string.action_archive) },
            SwipeAction.Action.DELETE to actionDelete.apply { tvItemOptionTitle.text = activity.getString(R.string.action_delete) },
            SwipeAction.Action.CALL to actionCall.apply { tvItemOptionTitle.text = activity.getString(R.string.action_call) },
            SwipeAction.Action.BLOCK to actionBlock.apply { tvItemOptionTitle.text = activity.getString(R.string.action_block) },
            SwipeAction.Action.MARK_AS_READ to actionMarkAsRead.apply { tvItemOptionTitle.text = activity.getString(R.string.action_mark_as_read) },
            SwipeAction.Action.MARK_AS_UNREAD to actionMarkAsUnread.apply { tvItemOptionTitle.text = activity.getString(R.string.action_mark_as_unread) }
        )


        resetSwipeActionSelection(actionItems.values.toList())
        markSelected(actionItems[selectedSwipeAction]!!, selectedItem)

        var tempSelectedAction = selectedSwipeAction

        // Click listeners to temporarily select
        actionItems.forEach { (action, binding) ->
            binding.root.setOnClickListener {
                tempSelectedAction = action
                resetSwipeActionSelection(actionItems.values.toList())
                markSelected(binding, selectedItem)
            }
        }

        // Negative Button – just dismiss
        dialogBinding.dialogButtonNegative.setOnClickListener {
            dialog.dismiss()
            onConfirm(false)
        }

        // Positive Button – save selected action
        dialogBinding.dialogButtonPositive.setOnClickListener {
            SwipeAction.setAction(swipeAction, tempSelectedAction)
            dialog.dismiss()
            onConfirm(true)
        }
    }
}
fun resetSwipeActionSelection(bindingList: List<ItemOptionSelectLay01Binding>) {
    val unselectedIconRes = R.drawable.ic_round_unselected_item

    bindingList.forEachIndexed { index, binding ->
        binding.tvItemOptionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, unselectedIconRes, 0)

        // Example: If you want to hide a divider or bottom line for the last item
        if (index == bindingList.lastIndex) {
            binding.itemOptionDivider.root.visibility = View.GONE // or binding.viewDivider.gone() if using extensions
        } else {
            binding.itemOptionDivider.root.visibility = View.VISIBLE
        }
    }
}
private fun markSelected(binding: ItemOptionSelectLay01Binding, drawableRes: Int) {
    binding.tvItemOptionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableRes, 0)
}
/*
private fun customizeDialog(check: Int) {
    val dialog = Dialog(this)
    val view: View = LayoutInflater.from(this).inflate(R.layout.swipe_action_selection_dialog, null)
    dialog.setContentView(view)
    dialog.setCancelable(false)
    val window = dialog.window
    if (window != null) {
        val layoutParams = window.attributes
        layoutParams.width = (getScreenWidth(this) * 0.92).toInt()
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.y = resources.getDimensionPixelSize(R.dimen.activity_margin)
        window.attributes = layoutParams
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setWindowAnimations(R.style.DialogAnimation)
    }



    val done = view.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.yesbtn)
    val cancel = view.findViewById<TextView>(R.id.nobtn)
    val title = view.findViewById<TextView>(R.id.conformationtitle)
    title.text = if (check == 0) getString(R.string.Right_swipe) else getString(R.string.left_swipe)


    noneImageView = view.findViewById(R.id.none_img)
    archiveImageView = view.findViewById(R.id.archive_layout_icon)
    deleteImageView = view.findViewById(R.id.delete_layout_icon)
    blockImageView = view.findViewById(R.id.block_layout_icon)
    markReadImageView = view.findViewById(R.id.mark_read_layout_icon)
    markUnreadImageView = view.findViewById(R.id.mark_unread_layout_icon)

    updateCheckIcons(currentSelectedAction)
    view.findViewById<LinearLayout>(R.id.none_lv).setOnClickListener {
        currentSelectedAction = Constants.None_Action
        updateCheckIcons(currentSelectedAction)
    }

    view.findViewById<LinearLayout>(R.id.archive_layout).setOnClickListener {
        currentSelectedAction = Constants.Archive_Action
        updateCheckIcons(currentSelectedAction)
    }

    view.findViewById<LinearLayout>(R.id.delete_layout).setOnClickListener {
        currentSelectedAction = Constants.Delete_Action
        updateCheckIcons(currentSelectedAction)
    }

    view.findViewById<LinearLayout>(R.id.block_layout).setOnClickListener {
        currentSelectedAction = Constants.Block_Action
        updateCheckIcons(currentSelectedAction)
    }

    view.findViewById<LinearLayout>(R.id.mark_read_layout).setOnClickListener {
        currentSelectedAction = Constants.Mark_Read_Action
        updateCheckIcons(currentSelectedAction)
    }

    view.findViewById<LinearLayout>(R.id.mark_unread_layout).setOnClickListener {
        currentSelectedAction = Constants.Mark_Unread_Action
        updateCheckIcons(currentSelectedAction)
    }

    done.setOnClickListener {

        if (check == 0) {
            SharePreference.setRightSwipePref(this, Constants.RIGHT_SWIPE_PREFERENCE, currentSelectedAction)
            rightSwipeInit(currentSelectedAction)
        } else {
            SharePreference.setLeftSwipePref(this, Constants.LEFT_SWIPE_PREFERENCE, currentSelectedAction)
            leftSwipeInit(currentSelectedAction)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(Constants.SEND_BROADCAST_TO_REFRESH_SWIPE_CLASS)
            .putExtra(
                "Visible_ProgressBar",
                false
            )
        )
        dialog.dismiss()
    }

    cancel.setOnClickListener { dialog.dismiss() }
    dialog.show()
}
*/
