package com.example.musicplayer.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayer.R
import com.example.musicplayer.data.local.AppPreferences
import com.example.musicplayer.data.local.ContentResolverHelper
import com.example.musicplayer.data.local.SongDbHelper
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repo.SongRepository
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.ui.player.PlayerActivity
import com.example.musicplayer.utils.AppPermission
import com.example.musicplayer.utils.PermissionStatus
import com.example.musicplayer.utils.Status

class MainActivity : AppCompatActivity(), SongAdapter.SongListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val songDbHelper by lazy { SongDbHelper(this) }
    private val contentResolverHelper by lazy { ContentResolverHelper(this) }
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MainViewModelFactory(SongRepository(songDbHelper, contentResolverHelper))
        ).get(MainViewModel::class.java)
    }
    private lateinit var songs: List<Song>
    private lateinit var adapter: SongAdapter
    private val storagePermission by lazy { AppPermission.READ_EXTERNAL_STORAGE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavView.setupWithNavController(navController)
//        initSongList()
//        observeData()
    }

//    private fun initSongList() {
//        adapter = SongAdapter(this)
//        binding.songList.adapter = adapter
//    }
//
//    private fun observeData() {
//        viewModel.loadSongsFromDb()
//        viewModel.songs.observe(this) {
//            it?.let {
//                binding.status = it.status
//                when (it.status) {
//                    Status.SUCCESS -> {
//                        adapter.submitList(it.data)
//                        songs = it.data ?: emptyList()
//                        askPermissionIfNoData(it.data)
//                    }
//                    Status.LOADING -> {
//                    }
//                    Status.ERROR -> it.error?.printStackTrace()
//                }
//            }
//        }
//    }
//
//    private fun askPermissionIfNoData(data: List<Song>?) {
//        if (data.isNullOrEmpty()) {
//            requestPermission(storagePermission)
//        } else {
//            if (!isPermissionGranted(storagePermission)) showWantToGivePermissionDialog(
//                storagePermission
//            )
//        }
//    }
//
//    private fun requestPermission(appPermission: AppPermission) {
//        when (permissionStatus(appPermission)) {
//            PermissionStatus.GRANTED -> viewModel.loadSongsFromContentResolver()
//            PermissionStatus.FIRST_TIME, PermissionStatus.RATIONALE_NEEDED -> ActivityCompat.requestPermissions(
//                this, arrayOf(
//                    appPermission.permission
//                ), appPermission.requestCode
//            )
//            PermissionStatus.DENIED -> showPermissionDeniedDialog(storagePermission)
//        }
//    }
//
//    private fun permissionStatus(appPermission: AppPermission): PermissionStatus {
//        return when {
//            AppPreferences.isFirstTimeAsking -> {
//                AppPreferences.isFirstTimeAsking = false
//                PermissionStatus.FIRST_TIME
//            }
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                this,
//                appPermission.permission
//            ) -> PermissionStatus.RATIONALE_NEEDED
//            isPermissionGranted(appPermission) -> PermissionStatus.GRANTED
//            else -> PermissionStatus.DENIED
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            storagePermission.requestCode -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    viewModel.loadSongsFromContentResolver()
//                } else if (permissionStatus(storagePermission) == PermissionStatus.RATIONALE_NEEDED) {
//                    showPermissionRationaleNeededDialog(storagePermission)
//                } else {
//                    requestPermission(storagePermission)
//                }
//            }
//        }
//    }
//
//    private fun showPermissionRationaleNeededDialog(permission: AppPermission) {
//        AlertDialog.Builder(this)
//            .setTitle(permission.messageResId)
//            .setNegativeButton("Not now") { _, _ -> finish() }
//            .setPositiveButton("Continue") { _, _ -> requestPermission(permission) }
//            .setCancelable(false)
//            .show()
//
//    }
//
//    private fun showPermissionDeniedDialog(permission: AppPermission) {
//        AlertDialog.Builder(this)
//            .setTitle(permission.messageResId)
//            .setNegativeButton("Not now") { _, _ -> finish() }
//            .setPositiveButton("Settings") { _, _ -> settingsScreen() }
//            .setCancelable(false)
//            .show()
//
//    }
//
//    private fun showWantToGivePermissionDialog(permission: AppPermission) {
//        AlertDialog.Builder(this)
//            .setTitle("Data might be old, give permission to update the data")
//            .setNegativeButton("Not now") { _, _ -> }
//            .setPositiveButton("Give permission") { _, _ -> settingsScreen() }
//            .setCancelable(false)
//            .show()
//
//    }
//
//    private fun settingsScreen() {
//        val intent = Intent()
//        intent.apply {
//            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//            data = Uri.parse("package:$packageName")
//        }
//        startActivity(intent)
//    }
//
//    private fun isPermissionGranted(appPermission: AppPermission): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            appPermission.permission
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
    override fun onSongClicked(position: Int) {
        PlayerActivity.setSongs(songs)
        PlayerActivity.setSongIndex(position)
        val intent = Intent(this, PlayerActivity::class.java)
        startActivity(intent)
    }

    override fun setSongFavorite(position: Int, isFavorite: Boolean) {
        val favorite = if (isFavorite) 1 else 0
        viewModel.updateSong(songs[position].copy(favorite = favorite))
    }
}

