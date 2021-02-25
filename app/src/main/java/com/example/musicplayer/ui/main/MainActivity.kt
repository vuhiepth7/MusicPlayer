package com.example.musicplayer.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayer.R
import com.example.musicplayer.data.local.AppPreferences
import com.example.musicplayer.data.service.MediaPlayerService
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.utils.AppPermission
import com.example.musicplayer.utils.PermissionStatus

class MainActivity : AppCompatActivity(), MediaPlayerService.MediaPlayerCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()
    private val storagePermission by lazy { AppPermission.READ_EXTERNAL_STORAGE }
    private lateinit var playerService: MediaPlayerService
    private var isBound = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            viewModel.setCurrentProgress(playerService.currentPosition())
            binding.progressIndicator.progress = playerService.currentPosition()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_library, R.id.nav_player))
        setupActionBarWithNavController(navController, appBarConfiguration)

        requestPermission(storagePermission)
        bindService()
        setupClickListeners()
        observeData()
        createNotificationChannel()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_player -> {
                    binding.bottomNavView.visibility = View.GONE
                    binding.miniPlayer.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                    if (viewModel.currentSong.value != null) binding.miniPlayer.visibility =
                        View.VISIBLE
                }
            }
        }
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

    private fun bindService() {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MediaPlayerService.LocalBinder
                playerService = binder.getService()
                playerService.registerCallback(this@MainActivity)
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
            }
        }
        if (!isBound) {
            val intent = Intent(this, MediaPlayerService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            playPause.setOnClickListener { viewModel.togglePlayPause() }
            miniPlayer.setOnClickListener { navController.navigate(R.id.nav_player) }
        }
    }

    private fun observeData() {
        viewModel.apply {
            togglePlayPauseEvent.observeForever {
                it?.getContentIfNotHandled()?.let {
                    with(playerService) {
                        viewModel.setIsPlaying(!isPlaying())
                        if (isPlaying()) {
                            pauseMedia()
                            handler.removeCallbacks(updateSeekBarRunnable)
                        } else {
                            playMedia()
                            handler.postDelayed(updateSeekBarRunnable, 0)
                        }
                    }
                }
            }

            isPlaying.observe(this@MainActivity) { isPlaying ->
                if (isPlaying) binding.playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, theme))
                else binding.playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, theme))
            }

            currentSong.observeForever {
                binding.song = it
                if (playerService.getSong()?.songId != it.songId) {
                    playerService.setSong(it)
                    viewModel.setIsPlaying(!playerService.isPlaying())
                    viewModel.setLooping(false)
                    handler.postDelayed(updateSeekBarRunnable, 0)
                    binding.progressIndicator.max = it.duration.toInt()
                }
            }

            seekToEvent.observe(this@MainActivity) {
                it.getContentIfNotHandled()?.let { progress -> playerService.seekTo(progress) }
            }

            isLooping.observe(this@MainActivity) { playerService.setLooping(it) }
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

    private fun showPermissionRationaleNeededDialog(permission: AppPermission) {
        AlertDialog.Builder(this)
            .setTitle(permission.messageResId)
            .setNegativeButton("Exit app") { _, _ -> finish() }
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MediaPlayerService.NOTIFICATION_CHANNEL_ID,
                MediaPlayerService.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onCompletion() {
        if (!viewModel.skipNext()) {
            viewModel.setIsPlaying(false)
        }
    }

    override fun onMediaPlayPause() {
        Log.e("MainActivity", "onMediaPlayPause")
        viewModel.togglePlayPause()
    }

    override fun onMediaSkipNext() {
        viewModel.skipNext()
    }

    override fun onMediaSkipPrevious() {
        viewModel.skipPrevious()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onRestart() {
        super.onRestart()
        handler.postDelayed(updateSeekBarRunnable, 0)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateSeekBarRunnable)
    }
}

