package com.example.music

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.progressindicator.CircularProgressIndicator

class ProgressFragment : Fragment(R.layout.frag_progress) {
    private val vm: WorkViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bar = view.findViewById<CircularProgressIndicator>(R.id.progressBar)
        val text = view.findViewById<TextView>(R.id.txt)

        vm.status.observe(viewLifecycleOwner) { s ->
            when (s) {
                "準備中…" -> {
                    bar.isIndeterminate = true
                    text.text = "準備中…"
                }
                "工作中" -> {
                    bar.isIndeterminate = false
                    val p = vm.progress.value ?: 0
                    bar.max = 100
                    bar.progress = p
                    text.text = "%d%%".format(p)
                }
                "背景工作結束" -> {
                    bar.isIndeterminate = false
                    bar.max = 100
                    bar.progress = 100
                    text.text = "背景工作結束"
                }
                "背景工作中斷" -> {
                    bar.isIndeterminate = false
                    text.text = "背景工作中斷"
                }
                else -> {
                    text.text = s
                }
            }
        }

        vm.progress.observe(viewLifecycleOwner) { p ->
            val currentStatus = vm.status.value ?: ""
            if (currentStatus == "工作中") {
                bar.isIndeterminate = false
                bar.max = 100
                bar.progress = p
                text.text = "%d%%".format(p)
            }
        }
    }
}
