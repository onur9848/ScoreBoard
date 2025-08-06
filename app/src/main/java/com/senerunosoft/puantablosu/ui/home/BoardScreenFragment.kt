package com.senerunosoft.puantablosu.ui.home

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.databinding.FragmentBoardScreenBinding
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.service.GameService
import com.senerunosoft.puantablosu.viewmodel.GameViewModel

class BoardScreenFragment : Fragment() {

    private var binding: FragmentBoardScreenBinding? = null
    private lateinit var gameViewModel: GameViewModel
    private lateinit var gameService: IGameService
    private lateinit var game: Game
    private var lastRound = 0
    private var isPlayerAdded = false

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBoardScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // back button to home fragment
        defVariable()
        loadData()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Oyundan çıkmak istediğinize emin misiniz?")
                    .setPositiveButton("Evet") { _, _ ->
                        NavHostFragment.findNavController(this@BoardScreenFragment)
                            .popBackStack(R.id.nav_home, false)
                    }
                    .setNegativeButton("Hayır", null)
                    .create()
                dialog.show()
            }
        }
        callback.isEnabled = true
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun loadData() {
        gameViewModel.gameInfo.observe(viewLifecycleOwner) { gameInfo ->
            game = gameInfo
            binding?.gameTitle?.text = gameInfo.gameTitle.uppercase()
            
            if (!isPlayerAdded) {
                gameInfo.playerList.forEachIndexed { index, player ->
                    binding?.playerList?.addView(createPlayerTextView(player.name))
                    if (index != gameInfo.playerList.size - 1) {
                        binding?.playerList?.addView(addDivider(false))
                    }
                }
                isPlayerAdded = true
            }

            val roundCount = gameInfo.score.size
            for (i in lastRound until roundCount) {
                binding?.scoreBoard?.addView(scoreBoardTemplate(i + 1, gameInfo.playerList))
                if (i != roundCount - 1) {
                    binding?.scoreBoard?.addView(addDivider(true))
                }
                lastRound = i + 1
            }
        }
    }

    private fun addDivider(isVertical: Boolean): View {
        val view = View(requireContext())
        val layoutParams = if (isVertical) {
            LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
        } else {
            LinearLayoutCompat.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        view.layoutParams = layoutParams
        view.setBackgroundColor(resources.getColor(R.color.black, null))
        return view
    }

    private fun createPlayerTextView(name: String): TextView {
        val textView = TextView(requireContext())
        textView.text = name
        textView.textSize = 20f
        textView.setTextColor(resources.getColor(R.color.white, null))
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        textView.layoutParams = layoutParams
        textView.maxLines = 1
        textView.gravity = Gravity.CENTER
        return textView
    }

    private fun roundTextView(round: Int): TextView {
        val textView = TextView(requireContext())
        textView.text = "#$round"
        textView.textSize = 20f
        textView.setTextColor(resources.getColor(R.color.white, null))
        val width = binding?.turn?.layoutParams?.width ?: ViewGroup.LayoutParams.WRAP_CONTENT
        val layoutParams = ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.layoutParams = layoutParams
        textView.maxLines = 1
        textView.gravity = Gravity.CENTER
        return textView
    }

    private fun scoreBoardTemplate(round: Int, playerList: List<Player>): LinearLayout {
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            100
        )
        linearLayout.gravity = Gravity.START

        linearLayout.addView(roundTextView(round))
        linearLayout.addView(addDivider(false))

        playerList.forEachIndexed { index, player ->
            val score = gameService.getPlayerRoundScore(game, player.id, round)
            linearLayout.addView(createPlayerTextView(score.toString()))
            if (index != playerList.size - 1) {
                linearLayout.addView(addDivider(false))
            }
        }

        return linearLayout
    }

    private fun defVariable() {
        gameService = GameService()
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        binding?.addScoreButton?.setOnClickListener { addScore() }
        binding?.calculateButton?.setOnClickListener { calculateScore() }
    }

    private fun calculateScore() {
        val calculatedScoreList = gameService.getCalculatedScore(game)
        // sort calculated score list
        calculatedScoreList.sortByDescending { it.score }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Skorlar")

        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(48, 32, 48, 32)
        linearLayout.setBackgroundColor(resources.getColor(R.color.white, null))

        calculatedScoreList.forEach { singleScore ->
            val row = LinearLayout(requireContext())
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, 16, 0, 16)
            row.gravity = Gravity.CENTER_VERTICAL

            val playerNameView = TextView(requireContext())
            val playerName = game.playerList.first { it.id == singleScore.playerId }.name
            playerNameView.text = playerName
            playerNameView.textSize = 18f
            playerNameView.typeface = Typeface.DEFAULT_BOLD
            playerNameView.setTextColor(resources.getColor(R.color.black, null))
            playerNameView.layoutParams = LinearLayout.LayoutParams(
                0, 
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                2f
            )
            playerNameView.gravity = Gravity.START

            val scoreView = TextView(requireContext())
            scoreView.text = singleScore.score.toString()
            scoreView.textSize = 18f
            scoreView.typeface = Typeface.DEFAULT_ITALIC
            scoreView.setTextColor(resources.getColor(R.color.purple_700, null))
            scoreView.layoutParams = LinearLayout.LayoutParams(
                0, 
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                1f
            )
            scoreView.gravity = Gravity.END

            row.addView(playerNameView)
            row.addView(scoreView)
            linearLayout.addView(row)

            val divider = View(requireContext())
            divider.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                2
            )
            divider.setBackgroundColor(resources.getColor(R.color.black, null))
            linearLayout.addView(divider)
        }

        builder.setView(linearLayout)
        builder.setPositiveButton("Tamam", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun addScore() {
        NavHostFragment.findNavController(this)
            .navigate(R.id.action_boardScreenFragment_to_addScoreDialogFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}