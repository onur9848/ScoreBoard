package com.senerunosoft.puantablosu.ui.home.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.databinding.FragmentAddScoreDialogBinding
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.service.GameService
import com.senerunosoft.puantablosu.viewmodel.GameViewModel

class AddScoreDialogFragment : DialogFragment() {

    private var _binding: FragmentAddScoreDialogBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var gameViewModel: GameViewModel
    private var game: Game? = null
    private lateinit var gameService: IGameService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddScoreDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineVariables()
        initPlayerInputs()
    }

    private fun savePlayerScore() {
        val currentGame = game
        if (currentGame?.playerList == null) {
            showErrorDialog(getString(R.string.error_title), getString(R.string.error_game_info_not_found))
            return
        }

        val playerList = currentGame.playerList
        val singleScoreList = mutableListOf<SingleScore>()
        var hasValidInput = false

        for (player in playerList) {
            if (player.id.isBlank()) continue

            val playerInput = requireView().findViewById<TextInputEditText>(player.id.hashCode())
            playerInput?.let { input ->
                val scoreText = input.text?.toString()?.trim() ?: ""
                if (scoreText.isNotEmpty()) {
                    try {
                        val score = scoreText.toInt()
                        singleScoreList.add(SingleScore(player.id, score))
                        hasValidInput = true
                    } catch (e: NumberFormatException) {
                        showErrorDialog(
                            getString(R.string.error_invalid_score),
                            getString(R.string.error_enter_valid_numbers, player.name)
                        )
                        return
                    }
                }
            }
        }

        if (!hasValidInput) {
            showErrorDialog(getString(R.string.error_empty_score), getString(R.string.error_enter_at_least_one_score))
            return
        }

        if (singleScoreList.size != playerList.size) {
            showErrorDialog(getString(R.string.error_incomplete_score), getString(R.string.error_enter_all_scores))
            return
        }

        val isSuccess = gameService.addScore(currentGame, singleScoreList)
        if (!isSuccess) {
            showErrorDialog(getString(R.string.error_title), getString(R.string.error_score_not_added))
            return
        }

        gameViewModel.setGameInfo(currentGame)
        
        try {
            val sharedPreferences = requireActivity().getSharedPreferences("game", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(currentGame.gameId, gameService.serializeGame(currentGame))
            editor.apply()
        } catch (e: Exception) {
            showErrorDialog(getString(R.string.error_save_failed), getString(R.string.error_game_not_saved))
            return
        }
        
        dismiss()
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.action_ok), null)
            .show()
    }

    private fun initPlayerInputs() {
        gameViewModel.getGameInfo().observe(viewLifecycleOwner) { gameInfo ->
            if (gameInfo == null) {
                showErrorDialog(getString(R.string.error_title), getString(R.string.error_game_info_not_loaded))
                return@observe
            }
            
            game = gameInfo
            val playerList = gameInfo.playerList

            if (playerList.isNotEmpty()) {
                binding.playerScoreInputContainer.removeAllViews()
                for (player in playerList) {
                    addPlayerInput(player)
                }
            } else {
                showErrorDialog(getString(R.string.error_title), getString(R.string.error_players_not_found))
            }
        }
    }

    private fun addPlayerInput(player: Player) {
        if (player.name.isBlank() || player.id.isBlank()) {
            return
        }

        val playerInputLayout = TextInputLayout(requireContext(), null, R.style.CustomTextInputLayoutStyle).apply {
            hint = player.name
        }

        val playerInputEditText = TextInputEditText(requireContext()).apply {
            id = player.id.hashCode()
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        
        playerInputLayout.addView(playerInputEditText)
        binding.playerScoreInputContainer.addView(playerInputLayout)
    }

    private fun defineVariables() {
        gameService = GameService()
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        binding.saveScoreButton.setOnClickListener { savePlayerScore() }
        binding.closeDialog.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}