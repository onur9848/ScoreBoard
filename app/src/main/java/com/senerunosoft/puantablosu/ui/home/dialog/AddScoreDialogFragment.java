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
        List<Player> playerList = game.getPlayerList();
        List<SingleScore> singleScoreList = new ArrayList<>();
        for (Player player : playerList) {
            TextInputEditText playerInput = requireView().findViewById(player.getId().hashCode());
            if (playerInput != null) {
                String score = playerInput.getText().toString();
                if (!score.isEmpty()) {
                    SingleScore singleScore = new SingleScore(player.getId(), Integer.parseInt(score));
                    singleScoreList.add(singleScore);
                }
            }
        }
        boolean isSuccess = gameService.addScore(game, singleScoreList);
        if (!isSuccess){
            AlertDialog errorDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Hata")
                    .setMessage("Skorlar eklenemedi")
                    .setPositiveButton("Tamam", null)
                    .create();
            errorDialog.show();
        }
        gameViewModel.setGameInfo(game);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("game", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(game.getGameId(), gameService.serializeGame(game));
        editor.apply();
        dismiss();
    }

    private void playerInputsInit() {
        gameViewModel.getGameInfo().observe(getViewLifecycleOwner(), gameInfo -> {
            game = gameInfo;
            List<Player> playerList = game.getPlayerList();

            if (playerList.size() > 0) {
                for (Player player : playerList) {
                    addPlayerInput(player);
                }
            }

        });
    }

    public void addPlayerInput(Player player) {

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
