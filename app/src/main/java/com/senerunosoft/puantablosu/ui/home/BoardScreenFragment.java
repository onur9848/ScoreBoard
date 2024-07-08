package com.senerunosoft.puantablosu.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.R;
import com.senerunosoft.puantablosu.databinding.FragmentBoardScreenBinding;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.Player;
import com.senerunosoft.puantablosu.service.GameService;
import com.senerunosoft.puantablosu.viewmodel.GameViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class BoardScreenFragment extends Fragment {

    private FragmentBoardScreenBinding binding;
    private GameViewModel gameViewModel;
    private IGameService gameService;
    private Game game;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defVariable();
        loadData();
    }

    private void loadData() {
        gameViewModel.getGameInfo().observe(getViewLifecycleOwner(), gameInfo -> {
            game = gameInfo;
            binding.gameTitle.setText(gameInfo.getGameTitle().toUpperCase());
            for (Player player : gameInfo.getPlayerList()) {
                binding.playerList.addView(createPlayerTextView(player.getName()));
                if (gameInfo.getPlayerList().indexOf(player) != gameInfo.getPlayerList().size() - 1)
                    binding.playerList.addView(addDivider(false));
            }

            int roundCount = gameInfo.getScore().size();
            for (int i = 0; i < roundCount; i++) {
                binding.scoreBoard.addView(scoreBoardTemplate(i + 1, gameInfo.getPlayerList()));
                if (i != roundCount - 1)
                    binding.scoreBoard.addView(addDivider(true));
            }


        });
    }

    private View addDivider(boolean isVertical) {
        View view = new View(requireContext());
        if (isVertical) {
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        } else {
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        view.setBackgroundColor(getResources().getColor(R.color.black));
        return view;
    }

    private TextView createPlayerTextView(String name) {
        TextView textView = new TextView(requireContext());
        textView.setText(name);
        textView.setTextSize(20);
        textView.setTextColor(getResources().getColor(R.color.white));
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        textView.setLayoutParams(layoutParams);
        textView.setMaxLines(1);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private TextView roundTextView(int round) {
        TextView textView = new TextView(requireContext());
        textView.setText("#" + round);
        textView.setTextSize(20);
        textView.setTextColor(getResources().getColor(R.color.white));
        int width = binding.turn.getLayoutParams().width;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width,ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(layoutParams);
        textView.setMaxLines(1);
        textView.setGravity(Gravity.CENTER);
        return textView;

    }

    private LinearLayout scoreBoardTemplate(int round, List<Player> playerList) {
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        linearLayout.setGravity(Gravity.START);

        linearLayout.addView(roundTextView(round));
        linearLayout.addView(addDivider(false));

        for (Player player : playerList) {
//            int score = player.getScoreList().get(round - 1);
            int score = gameService.getPlayerRoundScore(game, player.getId(), round);
            linearLayout.addView(createPlayerTextView(String.valueOf(score)));
            if (playerList.indexOf(player) != playerList.size() - 1)
                linearLayout.addView(addDivider(false));
        }


        return linearLayout;
    }

    private void defVariable() {
        gameService = new GameService();
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        binding.addScoreButton.setOnClickListener(v -> addScore());
    }

    private void addScore() {
        // open dialog and add each player score input
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Skor Ekle")
                .setMessage("Skorları giriniz")
                .setPositiveButton("Tamam", (dialog, which) -> {
                    // get each player score and update the list
                    List<Player> playerList = gameViewModel.getGameInfo().getValue().getPlayerList();
                    for (Player player : playerList) {
                        EditText editText = requireView().findViewById(player.getId().hashCode());
                        if (editText != null) {
                            String score = editText.getText().toString();
                            if (!score.isEmpty()) {
//                                player.getScoreList().add(Integer.parseInt(score));
                            }
                        }
                    }
                    gameViewModel.getGameInfo().getValue().setPlayerList(playerList);
                    gameViewModel.setGameInfo(gameViewModel.getGameInfo().getValue());
                })
                .setNegativeButton("İptal", null)
                .create();
        // add edit text for each player
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for (Player player : gameViewModel.getGameInfo().getValue().getPlayerList()) {
            EditText editText = new EditText(requireContext());
            editText.setHint(player.getName());
            editText.setId(player.getId().hashCode());
            linearLayout.addView(editText);
        }
        alertDialog.setView(linearLayout);

        alertDialog.show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
