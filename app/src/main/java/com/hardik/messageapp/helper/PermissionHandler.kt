package com.hardik.messageapp.helper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hardik.messageapp.helper.Constants.BASE_TAG

object PermissionHandler {
    private const val TAG = BASE_TAG + "A_PermissionHandler"

    private lateinit var multiplePermissionsLauncher: ActivityResultLauncher<Array<String>>

    private var permissionCallback: ((Map<String, Boolean>, Boolean) -> Unit)? = null

    fun requestAllPermissions(activity: AppCompatActivity, callback: (Map<String, Boolean>, Boolean) -> Unit) {
        permissionCallback = callback
        requestEssentialPermissions(activity)
    }

    fun setupPermissionLaunchers(activity: AppCompatActivity) {
        multiplePermissionsLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.e(TAG, "setupPermissionLaunchers: Permissions granted -> $permissions")

            // Check if all permissions are granted
            val allPermissionsGranted = permissions.all { it.value }

            // Invoke callback to notify MainActivity
            permissionCallback?.invoke(permissions, allPermissionsGranted)
        }
    }

    //private val permissionResults = mutableMapOf<String, Boolean>()
    //permissions.forEach { (permission, granted) -> permissionResults[permission] = granted }


    private fun requestEssentialPermissions(activity: AppCompatActivity) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_CONTACTS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val deniedPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            multiplePermissionsLauncher.launch(deniedPermissions.toTypedArray())
        } else {
            // todo : move forward to next work
            permissionCallback?.invoke(emptyMap(), true)
        }
    }



    fun showAppSettingsDialog(activity: AppCompatActivity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permission Required")
            .setMessage("Please allow the necessary permissions in the app settings.")
            .setPositiveButton("Open Settings") { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}

/*
        // Register the launchers here, in onCreate
        PermissionHandler.setupPermissionLaunchers(this)
        PermissionHandler.requestAllPermissions(this) { permissionResults, allPermissionsGranted ->
            if (allPermissionsGranted) {
                //Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                PermissionHandler.showAppSettingsDialog(this)
                //Toast.makeText(this, "Permissions denied or partially granted", Toast.LENGTH_SHORT).show()
            }
            //permissionResults.forEach{ Log.e(TAG, "onCreate: $it", ) }
        }
 */