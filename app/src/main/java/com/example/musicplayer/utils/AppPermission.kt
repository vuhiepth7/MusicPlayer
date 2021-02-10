package com.example.musicplayer.utils

import android.Manifest
import com.example.musicplayer.R

data class AppPermission(val permission: String, val requestCode: Int, val messageResId: Int) {
    companion object {
        val READ_EXTERNAL_STORAGE = AppPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 10, R.string.storage_permission_required)
    }
}

enum class PermissionStatus {
    GRANTED,
    FIRST_TIME,
    DENIED,
    RATIONALE_NEEDED
}