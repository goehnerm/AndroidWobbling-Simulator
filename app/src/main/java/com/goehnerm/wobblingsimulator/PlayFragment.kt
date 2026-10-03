package com.goehnerm.wobblingsimulator

import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.os.SystemClock
import com.goehnerm.wobblingsimulator.databinding.FragmentPlayBinding
import android.media.SoundPool
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.goehnerm.wobblingsimulator.data.GameDataManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PlayFragment : Fragment() {

    private var _binding: FragmentPlayBinding? = null
    private lateinit var dataManager: GameDataManager
    private var imageIndex = 1
    private var counter = 0
    private var highScore = 0
    private var lastTap: Long = 0L
    private var drawable1: Drawable? = null
    private var drawable2: Drawable? = null
    private var soundPool: SoundPool? = null
    private var tapSoundId: Int = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPlayBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawable1 = ContextCompat.getDrawable(requireContext(), R.drawable._1)
        drawable2 = ContextCompat.getDrawable(requireContext(), R.drawable._2)

        dataManager = GameDataManager(requireContext())
        lifecycleScope.launch {
            dataManager.highScoreFlow.collectLatest { score ->
                highScore = score
                // Example: Update UI text with saved high score
                binding.highScoreText?.text = "High Score: $highScore"
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
        tapSoundId = soundPool?.load(requireContext(), R.raw.wobble, 1) ?: 0
        binding.playButton.setOnClickListener {
            soundPool?.play(tapSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            binding.textView.text = counter.toString()
            val currentTimeMs = SystemClock.elapsedRealtime()
            if (lastTap != 0L) {
                val intervalMs = currentTimeMs - lastTap

                val currentBpm = 60_000.0 / intervalMs
                if (currentBpm !in (170.0)..(220.0)) {
                    gameOver(intervalMs, currentBpm)
                    return@setOnClickListener
                }
            }
            lastTap = currentTimeMs
            counter++
            binding.textView.text = "Score: $counter"
            if (imageIndex == 1){
                binding.mainImage.setImageDrawable(drawable2)
                imageIndex=2
            }else{
                binding.mainImage.setImageDrawable(drawable1)
                imageIndex=1
            }
        }
        binding.restartButton.setOnClickListener {
            restart()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundPool?.release()
        soundPool = null
        _binding = null
    }
    private fun gameOver(intervalMs: Long, bpm: Double) {
        binding.playButton.isEnabled = false
        binding.textView.text = "Score: $counter"
        /*binding.textView.text = String.format(
            Locale.US,
            "FAILED!\nInterval: %d ms\nBPM: %.1f\nScore: %d",
            intervalMs, bpm, counter
        )*/
        lifecycleScope.launch {
            dataManager.saveHighScore(counter)
        }
        binding.restartButton.visibility = View.VISIBLE
    }
    private fun restart() {
        counter = 0
        lastTap = 0L
        imageIndex = 1

        binding.textView.text = "0"
        binding.mainImage.setImageResource(R.drawable._1)
        binding.playButton.isEnabled = true

        binding.restartButton.visibility = View.GONE
    }
}