package com.senerunosoft.puantablosu.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.databinding.FragmentLatestGameBinding
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.service.GameService
import com.senerunosoft.puantablosu.viewmodel.GameViewModel

class LatestGameFragment : Fragment() {

    private var _binding: FragmentLatestGameBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gameService: IGameService
    private lateinit var gameViewModel: GameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLatestGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineVariables()
        getSharedElements()
    }

    private fun defineVariables() {
        gameService = GameService()
        sharedPreferences = requireActivity().getSharedPreferences("game", Context.MODE_PRIVATE)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
    }

    private fun getSharedElements() {
        val gameIds = sharedPreferences.getString("gameIds", "") ?: ""
        if (gameIds.isEmpty()) {
            // binding.textView.text = "Oynanmış oyun bulunmamaktadır."
        } else {
            getGameNameList(gameIds)
        }
    }

    private fun getGameNameList(gameIds: String) {
        val gameList = mutableListOf<Game>()
        val gameIdList = gameIds.split(",")
        
        for (gameId in gameIdList) {
            val gameJson = sharedPreferences.getString(gameId, "") ?: ""
            if (gameJson.isNotEmpty()) {
                gameService.deserializeGame(gameJson)?.let { game ->
                    gameList.add(game)
                }
            }
        }

        val gameNameList = gameList.map { it.gameTitle }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, gameNameList)
        binding.latestGameList.adapter = adapter

        binding.latestGameList.setOnItemClickListener { _, _, position, _ ->
            val game = gameList[position]
            gameViewModel.setGameInfo(game)
            NavHostFragment.findNavController(this)
                .navigate(R.id.boardScreenFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}