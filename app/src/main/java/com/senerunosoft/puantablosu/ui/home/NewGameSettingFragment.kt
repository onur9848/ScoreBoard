package com.senerunosoft.puantablosu.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextWatcher
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.databinding.FragmentNewGameSettingBinding
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.service.GameService
import com.senerunosoft.puantablosu.viewmodel.GameViewModel
import java.util.*
import kotlin.random.Random

class NewGameSettingFragment : Fragment() {

    private var binding: FragmentNewGameSettingBinding? = null
    private lateinit var playerList: MutableList<Player>
    private lateinit var gameViewModel: GameViewModel
    private lateinit var gameService: IGameService

    companion object {
        private var errorList: MutableMap<String, Boolean> = mutableMapOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewGameSettingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defVariables()
    }

    private fun defVariables() {
        gameService = GameService()
        playerList = mutableListOf()
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        binding?.addPlayerButton?.setOnClickListener { addPlayer(it) }
        binding?.startBoard?.setOnClickListener { startBoard(it) }
    }

    private fun startBoard(view: View) {
        /*if (true) {
            testFunction()
            return
        }*/
        errorList = mutableMapOf()

        binding?.gameTitleInput?.let { textInputValidate(it) }
        val playerAdapter = binding?.playerList?.adapter as? PlayerAdapter
        playerAdapter?.let { adapter ->
            for (i in 0 until adapter.itemCount) {
                val viewHolder = binding?.playerList?.findViewHolderForAdapterPosition(i) as? PlayerAdapter.PlayerViewHolder
                viewHolder?.playerInput?.let { textInputValidate(it) }
            }
        }

        val errorKeys = errorList.filterValues { it }.keys.toList()

        if (errorKeys.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Hata")
                .setMessage("${errorKeys.joinToString(", ")} alanları boş bırakılamaz.")
                .setPositiveButton("Tamam", null)
                .show()
            return
        }

        val gameTitle = binding?.gameTitleInput?.editText?.text.toString()
        val game = gameService.createGame(gameTitle)
        playerList.forEach { player ->
            gameService.addPlayer(game, player.name)
        }

        gameViewModel.setGameInfo(game)
        val sharedPreferences = requireActivity().getSharedPreferences("game", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gameIds = sharedPreferences.getString("gameIds", "") ?: ""
        val updatedGameIds = if (gameIds.isNotEmpty()) {
            "$gameIds,${game.gameId}"
        } else {
            game.gameId
        }
        editor.putString("gameIds", updatedGameIds)
        val serializeGameData = gameService.serializeGame(game)
        editor.putString(game.gameId, serializeGameData)
        editor.apply()

        NavHostFragment.findNavController(this)
            .navigate(R.id.action_newGameSettingFragment_to_boardScreenFragment)
    }

    private fun testFunction() {
        // create random game info and navigate to board screen
        val game = gameService.createGame("Game 1")
        repeat(3) { i ->
            gameService.addPlayer(game, "Player ${i + 1}")
        }

        repeat(10) {
            val singleScoreList = game.playerList.map { player ->
                SingleScore(player.id, Random.nextInt(100))
            }
            val isSuccess = gameService.addScore(game, singleScoreList)
            if (!isSuccess) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Hata")
                    .setMessage("Skor eklenirken hata oluştu.")
                    .setPositiveButton("Tamam", null)
                    .show()
                return
            }
        }

        gameViewModel.setGameInfo(game)

        NavHostFragment.findNavController(this)
            .navigate(R.id.action_newGameSettingFragment_to_boardScreenFragment)
    }

    private fun addPlayer(view: View) {
        if (playerList.size >= 6) {
            AlertDialog.Builder(requireContext())
                .setTitle("Oyuncu Ekleme")
                .setMessage("Maksimum oyuncu sayısına ulaştınız.")
                .setPositiveButton("Tamam", null)
                .show()
            return
        }
        playerList.add(Player(""))

        val playerAdapter = PlayerAdapter(requireContext(), playerList)
        binding?.playerList?.adapter = playerAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding?.playerList?.layoutManager = layoutManager
    }

    private fun textInputValidate(inputLayout: TextInputLayout) {
        val editText = inputLayout.editText ?: return
        val text = editText.text.toString()
        val hint = inputLayout.hint.toString()
        
        if (text.isEmpty()) {
            inputLayout.error = "Bu alan boş bırakılamaz."
            inputLayout.isErrorEnabled = true
            errorList[hint] = true
        } else {
            inputLayout.error = null
            errorList[hint] = false
        }

        // edit text
        inputLayout.setOnEditTextAttachedListener { layout ->
            layout.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    inputLayout.error = null
                    inputLayout.isErrorEnabled = false
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    inputLayout.error = null
                    inputLayout.isErrorEnabled = false
                }

                override fun afterTextChanged(s: android.text.Editable?) {
                    inputLayout.error = null
                    inputLayout.isErrorEnabled = false
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private class PlayerAdapter(
        private val context: Context,
        private val playerList: MutableList<Player>
    ) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
        
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
            val view = inflater.inflate(R.layout.player_item, parent, false)
            return PlayerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
            val player = playerList[position]
            holder.bind(player)

            holder.deleteButton.setOnClickListener {
                playerList.remove(player)
                notifyItemRemoved(position)
            }
        }

        override fun getItemCount(): Int = playerList.size

        class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val playerInput: TextInputLayout = view.findViewById(R.id.player_item_input)
            val deleteButton: ImageButton = view.findViewById(R.id.delete_player_button)

            fun bind(player: Player) {
                val editText = playerInput.editText ?: return

                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: android.text.Editable?) {
                        player.name = s.toString()
                    }
                })

                if (player.name.isEmpty()) {
                    editText.hint = "Oyuncu ${adapterPosition + 1}"
                } else {
                    editText.setText(player.name)
                }
            }
        }
    }
}