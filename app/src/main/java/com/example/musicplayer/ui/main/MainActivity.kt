package com.example.musicplayer.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayer.R
import com.example.musicplayer.data.local.AppPreferences
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.utils.AppPermission
import com.example.musicplayer.utils.PermissionStatus

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()
    private val storagePermission by lazy { AppPermission.READ_EXTERNAL_STORAGE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavView.setupWithNavController(navController)

        requestPermission(storagePermission)
    }

    private fun requestPermission(appPermission: AppPermission) {
        when (permissionStatus(appPermission)) {
            PermissionStatus.GRANTED -> viewModel.updateDb()
            PermissionStatus.FIRST_TIME, PermissionStatus.RATIONALE_NEEDED -> ActivityCompat.requestPermissions(
                this, arrayOf(
                    appPermission.permission
                ), appPermission.requestCode
            )
            PermissionStatus.DENIED -> showPermissionDeniedDialog(storagePermission)
        }
    }

    private fun permissionStatus(appPermission: AppPermission): PermissionStatus {
        return when {
            AppPreferences.isFirstTimeAsking -> {
                AppPreferences.isFirstTimeAsking = false
                PermissionStatus.FIRST_TIME
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                appPermission.permission
            ) -> PermissionStatus.RATIONALE_NEEDED
            isPermissionGranted(appPermission) -> PermissionStatus.GRANTED
            else -> PermissionStatus.DENIED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            storagePermission.requestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.updateDb()
                } else if (permissionStatus(storagePermission) == PermissionStatus.RATIONALE_NEEDED) {
                    showPermissionRationaleNeededDialog(storagePermission)
                } else {
                    requestPermission(storagePermission)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showPermissionRationaleNeededDialog(permission: AppPermission) {
        AlertDialog.Builder(this)
            .setTitle(permission.messageResId)
            .setNegativeButton("Exit app") { _, _ -> finish()}
            .setPositiveButton("Allow") { _, _ -> requestPermission(permission) }
            .setCancelable(false)
            .show()

    }

    private fun showPermissionDeniedDialog(permission: AppPermission) {
        AlertDialog.Builder(this)
            .setTitle(permission.messageResId)
            .setNegativeButton("Exit app") { _, _ -> finish() }
            .setPositiveButton("Settings") { _, _ -> settingsScreen() }
            .setCancelable(false)
            .show()

    }

    private fun settingsScreen() {
        val intent = Intent()
        intent.apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun isPermissionGranted(appPermission: AppPermission): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            appPermission.permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

