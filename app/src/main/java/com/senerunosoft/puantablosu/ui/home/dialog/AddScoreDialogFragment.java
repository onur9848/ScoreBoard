package com.senerunosoft.puantablosu.ui.home.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Window;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.R;
import com.senerunosoft.puantablosu.databinding.FragmentAddScoreDialogBinding;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.Player;
import com.senerunosoft.puantablosu.model.Score;
import com.senerunosoft.puantablosu.model.SingleScore;
import com.senerunosoft.puantablosu.service.GameService;
import com.senerunosoft.puantablosu.viewmodel.GameViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddScoreDialogFragment extends DialogFragment {

    private FragmentAddScoreDialogBinding binding;
    private GameViewModel gameViewModel;
    private Game game;
    private IGameService gameService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddScoreDialogBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defVariable();
        playerInputsInit();
    }

    private void savePlayerScore() {
        if (game == null || game.getPlayerList() == null) {
            showErrorDialog(getString(R.string.error_title), getString(R.string.error_game_info_not_found));
            return;
        }
        
        List<Player> playerList = game.getPlayerList();
        List<SingleScore> singleScoreList = new ArrayList<>();
        boolean hasValidInput = false;
        
        for (Player player : playerList) {
            if (player == null || player.getId() == null) continue;
            
            TextInputEditText playerInput = requireView().findViewById(player.getId().hashCode());
            if (playerInput != null) {
                String scoreText = playerInput.getText() != null ? playerInput.getText().toString().trim() : "";
                if (!scoreText.isEmpty()) {
                    try {
                        int score = Integer.parseInt(scoreText);
                        SingleScore singleScore = new SingleScore(player.getId(), score);
                        singleScoreList.add(singleScore);
                        hasValidInput = true;
                    } catch (NumberFormatException e) {
                        showErrorDialog(getString(R.string.error_invalid_score), 
                                      getString(R.string.error_enter_valid_numbers, player.getName()));
                        return;
                    }
                }
            }
        }
        
        if (!hasValidInput) {
            showErrorDialog(getString(R.string.error_empty_score), getString(R.string.error_enter_at_least_one_score));
            return;
        }
        
        if (singleScoreList.size() != playerList.size()) {
            showErrorDialog(getString(R.string.error_incomplete_score), getString(R.string.error_enter_all_scores));
            return;
        }
        
        boolean isSuccess = gameService.addScore(game, singleScoreList);
        if (!isSuccess) {
            showErrorDialog(getString(R.string.error_title), getString(R.string.error_score_not_added));
            return;
        }
        
        gameViewModel.setGameInfo(game);
        try {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("game", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(game.getGameId(), gameService.serializeGame(game));
            editor.apply();
        } catch (Exception e) {
            showErrorDialog(getString(R.string.error_save_failed), getString(R.string.error_game_not_saved));
            return;
        }
        dismiss();
    }
    
    private void showErrorDialog(String title, String message) {
        AlertDialog errorDialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_ok), null)
                .create();
        errorDialog.show();
    }

    private void playerInputsInit() {
        gameViewModel.getGameInfo().observe(getViewLifecycleOwner(), gameInfo -> {
            if (gameInfo == null) {
                showErrorDialog(getString(R.string.error_title), getString(R.string.error_game_info_not_loaded));
                return;
            }
            game = gameInfo;
            List<Player> playerList = game.getPlayerList();

            if (playerList != null && playerList.size() > 0) {
                // Clear existing inputs before adding new ones
                if (binding != null && binding.playerScoreInputContainer != null) {
                    binding.playerScoreInputContainer.removeAllViews();
                }
                for (Player player : playerList) {
                    if (player != null) {
                        addPlayerInput(player);
                    }
                }
            } else {
                showErrorDialog(getString(R.string.error_title), getString(R.string.error_players_not_found));
            }
        });
    }

    public void addPlayerInput(Player player) {
        if (player == null || player.getName() == null || player.getId() == null) {
            return;
        }
        
        if (binding == null || binding.playerScoreInputContainer == null) {
            return;
        }

        TextInputLayout playerInputLayout = new TextInputLayout(requireContext(), null, R.style.CustomTextInputLayoutStyle);
        playerInputLayout.setHint(player.getName());

        TextInputEditText playerInputEditText = new TextInputEditText(requireContext());
        playerInputEditText.setId(player.getId().hashCode());
        playerInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        playerInputLayout.addView(playerInputEditText);

        binding.playerScoreInputContainer.addView(playerInputLayout);
    }
    private void defVariable() {
        gameService = new GameService();
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        binding.saveScoreButton.setOnClickListener(v -> savePlayerScore());
        binding.closeDialog.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
