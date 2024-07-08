package com.senerunosoft.puantablosu.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputLayout;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.R;
import com.senerunosoft.puantablosu.databinding.FragmentNewGameSettingBinding;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.Player;
import com.senerunosoft.puantablosu.model.SingleScore;
import com.senerunosoft.puantablosu.service.GameService;
import com.senerunosoft.puantablosu.viewmodel.GameViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NewGameSettingFragment extends Fragment {

    private FragmentNewGameSettingBinding binding;
    private List<Player> playerList;
    private static Dictionary<String, Boolean> errorList;
    private GameViewModel gameViewModel;
    private IGameService gameService;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewGameSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defVariables();
    }

    private void defVariables() {
        gameService = new GameService();
        playerList = new ArrayList<>();
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        binding.addPlayerButton.setOnClickListener(this::addPlayer);
        binding.startBoard.setOnClickListener(this::startBoard);
    }

    private void startBoard(View view) {
        if (true) {
            testFunction();
            return;
        }
        errorList = new Hashtable<>();

        TextInputValidate(binding.gameTitleInput);
        PlayerAdapter playerAdapter = (PlayerAdapter) binding.playerList.getAdapter();
        if (playerAdapter != null) {
            for (int i = 0; i < playerAdapter.getItemCount(); i++) {
                PlayerAdapter.PlayerViewHolder viewHolder = (PlayerAdapter.PlayerViewHolder) binding.playerList.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    TextInputValidate(viewHolder.playerInput);
                }
            }
        }

        List<String> errorKeys = new ArrayList<>();
        for (Enumeration<String> e = errorList.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            if (errorList.get(key)) {
                errorKeys.add(key);
            }
        }

        if (errorKeys.size() > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Hata")
                    .setMessage(String.format(Locale.getDefault(), "%s alanları boş bırakılamaz.", errorKeys))
                    .setPositiveButton("Tamam", null)
                    .show();
            return;
        }

        Game game = gameService.createGame(binding.gameTitleInput.getEditText().getText().toString());
        for (Player player : playerList) {
            gameService.addPlayer(game, player.getName());
        }

        gameViewModel.setGameInfo(game);

        NavHostFragment.findNavController(NewGameSettingFragment.this)
                .navigate(R.id.action_newGameSettingFragment_to_boardScreenFragment);

    }

    private void testFunction() {
        // create random game info and navigate to board screen
        Game game = gameService.createGame("Game 1");
        for (int i = 0; i < 3; i++) {
            gameService.addPlayer(game, "Player " + (i + 1));
        }

/*        List<Player> playerList = new ArrayList<>();
        playerList.add(new Player("Player 1"));
        playerList.add(new Player("Player 2"));
        playerList.add(new Player("Player 3"));*/

        List<SingleScore> singleScoreList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            singleScoreList.clear();
            for (Player player : game.getPlayerList()) {
                singleScoreList.add(new SingleScore(player.getId(), new Random().nextInt(100)));
            }
            gameService.addScore(game, singleScoreList);
        }

//        GameInfo gameInfo = new GameInfo("Test Game", playerList.size(), playerList);
        gameViewModel.setGameInfo(game);

        NavHostFragment.findNavController(NewGameSettingFragment.this)
                .navigate(R.id.action_newGameSettingFragment_to_boardScreenFragment);
    }

    private void addPlayer(View view) {
        if (playerList.size() >= 6) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Oyuncu Ekleme")
                    .setMessage("Maksimum oyuncu sayısına ulaştınız.")
                    .setPositiveButton("Tamam", null)
                    .show();
            return;
        }
        playerList.add(new Player(""));

        PlayerAdapter playerAdapter = new PlayerAdapter(requireContext(), playerList);
        binding.playerList.setAdapter(playerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.playerList.setLayoutManager(layoutManager);

    }

    private static void TextInputValidate(TextInputLayout inputLayout) {
        if (inputLayout.getEditText() == null) {
            return;
        }
        String text = inputLayout.getEditText().getText().toString();
        if (text.isEmpty()) {
            inputLayout.setError("Bu alan boş bırakılamaz.");
            inputLayout.setErrorEnabled(true);
            errorList.put(inputLayout.getHint().toString(), true);
        } else {
            inputLayout.setError(null);
            errorList.put(inputLayout.getHint().toString(), false);
        }

        // edit text
        inputLayout.addOnEditTextAttachedListener(new TextInputLayout.OnEditTextAttachedListener() {
            @Override
            public void onEditTextAttached(TextInputLayout layout) {
                layout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        inputLayout.setError(null);
                        inputLayout.setErrorEnabled(false);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        inputLayout.setError(null);
                        inputLayout.setErrorEnabled(false);

                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                        inputLayout.setError(null);
                        inputLayout.setErrorEnabled(false);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class PlayerAdapter extends RecyclerView.Adapter {
        LayoutInflater inflater;
        List<Player> playerList;

        public PlayerAdapter(Context context, List<Player> playerList) {
            this.inflater = LayoutInflater.from(context);
            this.playerList = playerList;
        }


        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
            // onCreateViewHolder
            View view = inflater.inflate(R.layout.player_item, viewGroup, false);
            return new PlayerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int i) {
            // onBindViewHolder
            Player player = playerList.get(i);
            ((PlayerViewHolder) viewHolder).bind(player);

            ((PlayerViewHolder) viewHolder).deleteButton.setOnClickListener(v -> {
                playerList.remove(player);
                notifyItemRemoved(i);
            });
        }

        @Override
        public int getItemCount() {
            return playerList.size();
        }

        private static class PlayerViewHolder extends RecyclerView.ViewHolder {
            TextInputLayout playerInput;
            ImageButton deleteButton;

            public PlayerViewHolder(View view) {
                super(view);
                playerInput = view.findViewById(R.id.player_item_input);
                deleteButton = view.findViewById(R.id.delete_player_button);
            }

            void bind(Player player) {
                if (playerInput.getEditText() == null) {
                    return;
                }

                playerInput.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                        player.setName(s.toString());
                    }
                });

                if (player.getName().isEmpty()) {
                    playerInput.getEditText().setHint(String.format(Locale.getDefault(), "Oyuncu %d", getAdapterPosition() + 1));
                } else {
                    playerInput.getEditText().setText(player.getName());
                }
            }
        }
    }
}
