package com.example.musicplayer.ui.player

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicplayer.data.receiver.AlarmReceiver
import com.example.musicplayer.databinding.DialogFragmentSleepTimerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SleepTimerDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFragmentSleepTimerBinding
    private lateinit var adapter: SleepTimerAdapter
    private lateinit var alarmManager: AlarmManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentSleepTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        initPlaylistAdapter()
    }


    private fun initPlaylistAdapter() {
        adapter = SleepTimerAdapter(object : SleepTimerAdapter.SleepTimerListener {
            override fun onClicked(position: Int) {
                when (val sleepInterval = adapter.currentList[position]) {
                    SleepInterval.FiveMinutes,
                    SleepInterval.TenMinutes,
                    SleepInterval.FifteenMinutes,
                    SleepInterval.ThirtyMinutes,
                    SleepInterval.FortyFiveMinutes,
                    SleepInterval.OneHour -> setAlarm(sleepInterval)
                    SleepInterval.NoInterval -> cancelAlarm()
                }
                dialog?.cancel()
            }
        })
        adapter.submitList(SleepInterval.values().toList())
        binding.sleepTimerRv.adapter = adapter
    }

    private fun setAlarm(sleepInterval: SleepInterval) {
        val pendingIntent =
            PendingIntent.getBroadcast(
                requireContext(), 0, Intent(
                    requireContext(),
                    AlarmReceiver::class.java
                ), 0
            )
        alarmManager.setExact(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + sleepInterval.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm() {
        val pendingIntent =
            PendingIntent.getBroadcast(
                requireContext(), 0, Intent(
                    requireContext(),
                    AlarmReceiver::class.java
                ), 0
            )
        alarmManager.cancel(pendingIntent)
    }
}

enum class SleepInterval(val text: String, val timeInMillis: Long) {
    FiveMinutes("5 Minutes", 5 * 60 * 1000),
    TenMinutes("10 Minutes", 10 * 60 * 1000),
    FifteenMinutes("15 Minutes", 15 * 60 * 1000),
    ThirtyMinutes("30 Minutes", 30 * 60 * 1000),
    FortyFiveMinutes("45 Minutes", 45 * 60 * 1000),
    OneHour("1 Hour", 60 * 60 * 1000),
    NoInterval("No sleep timer", 0)
}