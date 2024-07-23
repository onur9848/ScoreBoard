package com.senerunosoft.puantablosu.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.R;
import com.senerunosoft.puantablosu.databinding.FragmentBoardScreenBinding;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.Player;
import com.senerunosoft.puantablosu.model.SingleScore;
import com.senerunosoft.puantablosu.service.GameService;
import com.senerunosoft.puantablosu.viewmodel.GameViewModel;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class BoardScreenFragment extends Fragment {

    private FragmentBoardScreenBinding binding;
    private GameViewModel gameViewModel;
    private IGameService gameService;
    private Game game;
    private int lastRound = 0;
    private boolean isPlayerAdded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // back button to home fragment
        defVariable();
        loadData();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle("Oyundan çıkmak istediğinize emin misiniz?")
                        .setPositiveButton("Evet", (dialog1, which) -> {
                            NavHostFragment.findNavController(BoardScreenFragment.this).popBackStack(R.id.nav_home, false);
                        })
                        .setNegativeButton("Hayır", null)
                        .create();
                dialog.show();
            }
        };
        callback.setEnabled(true);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void loadData() {
        gameViewModel.getGameInfo().observe(getViewLifecycleOwner(), gameInfo -> {
            game = gameInfo;
            binding.gameTitle.setText(gameInfo.getGameTitle().toUpperCase());
            if (!isPlayerAdded) {
                for (Player player : gameInfo.getPlayerList()) {
                    binding.playerList.addView(createPlayerTextView(player.getName()));
                    if (gameInfo.getPlayerList().indexOf(player) != gameInfo.getPlayerList().size() - 1)
                        binding.playerList.addView(addDivider(false));
                    isPlayerAdded = true;
                }
            }


            int roundCount = gameInfo.getScore().size();
            for (int i = lastRound; i < roundCount; i++) {
                binding.scoreBoard.addView(scoreBoardTemplate(i + 1, gameInfo.getPlayerList()));
                if (i != roundCount - 1)
                    binding.scoreBoard.addView(addDivider(true));

                lastRound = i + 1;
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
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
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
        binding.calculateButton.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        List<SingleScore> calculatedScoreList = gameService.getCalculatedScore(game);
        // sort calculated score list
        calculatedScoreList.sort((o1, o2) -> o2.getScore() - o1.getScore());

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Skorlar")
                .setMessage("Oyuncu Skorları")
                .setPositiveButton("Tamam", null)
                .create();

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // Player : {score}
        for (SingleScore singleScore : calculatedScoreList) {
            TextView textView = new TextView(requireContext());
            // first  text player bold: second text italic score
            String playerName = game.getPlayerList().stream().filter(player -> player.getId().equals(singleScore.playerId())).findFirst().get().getName();
            textView.setText(playerName);
            textView.setTextSize(20);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TextView scoreText = new TextView(requireContext());
            scoreText.setText(String.valueOf(singleScore.getScore()));
            scoreText.setTextSize(20);
            scoreText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            linearLayout.addView(textView);
            linearLayout.addView(scoreText);
        }
        alertDialog.setView(linearLayout);
        alertDialog.show();
    }

    private void addScore() {
        NavHostFragment.findNavController(this).navigate(R.id.action_boardScreenFragment_to_addScoreDialogFragment);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
