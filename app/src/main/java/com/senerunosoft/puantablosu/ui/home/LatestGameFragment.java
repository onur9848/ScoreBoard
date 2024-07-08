package com.senerunosoft.puantablosu.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.R;
import com.senerunosoft.puantablosu.databinding.FragmentLatestGameBinding;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.service.GameService;
import com.senerunosoft.puantablosu.viewmodel.GameViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LatestGameFragment extends Fragment {

    FragmentLatestGameBinding binding;
    SharedPreferences sharedPreferences;
    IGameService gameService;
    GameViewModel gameViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLatestGameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defVariable();
        getSharedElements();
    }

    private void defVariable() {
        gameService = new GameService();
        sharedPreferences = requireActivity().getSharedPreferences("game", Context.MODE_PRIVATE);
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
    }

    private void getSharedElements() {
        String gameIds = sharedPreferences.getString("gameIds", "");
        if (gameIds.isEmpty()) {
//            binding.textView.setText("Oynanmış oyun bulunmamaktadır.");
        } else {
            getGameNameList(gameIds);
        }

    }

    private void getGameNameList(String gameIds) {
        List<Game> gameList = new ArrayList<>();
        List<String> gameIdList = Arrays.asList(gameIds.split(","));
        for (String gameId : gameIdList) {
            String gameJson = sharedPreferences.getString(gameId, "");
            if (!gameJson.isEmpty()) {
                Game game = gameService.deserializeGame(gameJson);
                gameList.add(game);
            }
        }

        List<String> gameNameList = new ArrayList<>();
        for (Game game : gameList) {
            gameNameList.add(game.getGameTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, gameNameList);
        binding.latestGameList.setAdapter(adapter);

        binding.latestGameList.setOnItemClickListener((parent, view, position, id) -> {
            Game game = gameList.get(position);
            gameViewModel.setGameInfo(game);
            NavHostFragment.findNavController(LatestGameFragment.this)
                    .navigate(R.id.boardScreenFragment);
        });


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
