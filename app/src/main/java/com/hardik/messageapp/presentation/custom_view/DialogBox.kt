package com.hardik.messageapp.presentation.custom_view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.DialogBlockConversationBinding
import com.hardik.messageapp.databinding.DialogDeleteConversationBinding
import com.hardik.messageapp.util.dpToPx


fun showDeleteConversationDialog(activity: AppCompatActivity, onConfirm: (alsoBlock: Boolean, isPositive: Boolean) -> Unit) {
    val dialogView = activity.layoutInflater.inflate(R.layout.dialog_delete_conversation, null)
    val dialogBinding = DialogDeleteConversationBinding.bind(dialogView)

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)

    // Transparent background + rounded corners
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val window = dialog.window
    window?.setLayout(
        (activity.resources.displayMetrics.widthPixels * 0.95).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    window?.setGravity(Gravity.BOTTOM)

    // Add bottom margin of 16dp
    window?.attributes = window?.attributes?.apply {
        y = 16.dpToPx(activity) // Custom extension function below
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

    // Transparent background + rounded corners
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val window = dialog.window
    window?.setLayout(
        (activity.resources.displayMetrics.widthPixels * 0.95).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    window?.setGravity(Gravity.BOTTOM)

    // Add bottom margin of 16dp
    window?.attributes = window?.attributes?.apply {
        y = 16.dpToPx(activity) // Custom extension function below
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


