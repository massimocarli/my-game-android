package uk.co.massimocarli.mathgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the Fragment we use to show how Turn Base multiplayer works
 */
public class TurnMultiplayerGameFragment extends BaseGameFragment {

    /**
     * The key for invitation
     */
    private static final String INVITATION_ARG_KEY = "InvitationKey";

    /**
     * The Request Id for the invite
     */
    private static final int TURN_INVITE_REQUEST_ID = 137;

    /**
     * The Request Id for the invite inbox
     */
    private static final int INVITE_INBOX_REQUEST_ID = 139;

    /**
     * The min number of invited players for his game
     */
    private static final int MIN_INVITE_PLAYERS = 1;

    /**
     * The max number of invited players for his game
     */
    private static final int MAX_INVITE_PLAYERS = 7;

    /**
     * The State for the Match
     */
    private TurnMatchState mMatchState;

    /**
     * The Current Match
     */
    private TurnBasedMatch mMatch;

    /**
     * Implememtation of the interface for the callback of the turns
     */
    private OnTurnBasedMatchUpdateReceivedListener mMatchUpdateReceivedListener =
            new OnTurnBasedMatchUpdateReceivedListener() {
                @Override
                public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
                    mMatch = turnBasedMatch;
                    showToast("Make your move");
                }

                @Override
                public void onTurnBasedMatchRemoved(String s) {
                    Log.i(TAG, "onTurnBasedMatchRemoved");
                }
            };


    /**
     * The callback for the match creation
     */
    private final ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> mMatchInitiatedCallback =
            new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                    if (initiateMatchResult.getStatus().isSuccess()) {
                        // We get the information related to the created Match
                        mMatch = initiateMatchResult.getMatch();
                        // We check if we already have data. If not we have to create
                        // the data for the match
                        byte[] matchData = mMatch.getData();
                        if (matchData == null) {
                            // we have to init the Match
                            initMatch(mMatch);
                        } else {
                            // In this case the Match already exists so we have to make our move
                            showToast("Make your move");
                        }
                    } else {
                        showToast("Error inviting players");
                    }
                }
            };

    /**
     * @return A new instance of fragment AchievementsFragment.
     */
    public static TurnMultiplayerGameFragment newInstance() {
        TurnMultiplayerGameFragment fragment = new TurnMultiplayerGameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static TurnMultiplayerGameFragment newInstance(final TurnBasedMatch turnBasedMatch) {
        TurnMultiplayerGameFragment fragment = new TurnMultiplayerGameFragment();
        Bundle args = new Bundle();
        args.putParcelable(INVITATION_ARG_KEY, turnBasedMatch);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_turn, container, false);
        fragmentLayout.findViewById(R.id.turn_invite_friend_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inviteIntent = Games.TurnBasedMultiplayer
                        .getSelectOpponentsIntent(getGoogleApiClient(), MIN_INVITE_PLAYERS, MAX_INVITE_PLAYERS);
                startActivityForResult(inviteIntent, TURN_INVITE_REQUEST_ID);
            }
        });
        // The Invite Inbox
        fragmentLayout.findViewById(R.id.turn_invite_inbox_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inviteInboxIntent = Games.TurnBasedMultiplayer.getInboxIntent(getGoogleApiClient());
                startActivityForResult(inviteInboxIntent, INVITE_INBOX_REQUEST_ID);
            }
        });
        // The Button for the move
        fragmentLayout.findViewById(R.id.turn_invite_send_move_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We make out move
                makeMove(mMatch);
            }
        });
        // We register the listener
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(getGoogleApiClient(), mMatchUpdateReceivedListener);
        // We return the layout
        return fragmentLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TURN_INVITE_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                // We get the invited players
                final ArrayList<String> invitedPlayers = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                // We get information related to the auto match player
                Bundle autoMatchCriteria = null;
                int minAutoMatchPlayers =
                        data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
                int maxAutoMatchPlayers =
                        data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
                if (minAutoMatchPlayers > 0) {
                    autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                            minAutoMatchPlayers, maxAutoMatchPlayers, 0);
                } else {
                    autoMatchCriteria = null;
                }
                TurnBasedMatchConfig turnBasedMatchConfig = TurnBasedMatchConfig.builder()
                        .addInvitedPlayers(invitedPlayers)
                        .setAutoMatchCriteria(autoMatchCriteria)
                        .build();
                Games.TurnBasedMultiplayer.createMatch(getGoogleApiClient(), turnBasedMatchConfig)
                        .setResultCallback(mMatchInitiatedCallback);
                // Here we have to keep the screen on because if the screen go off during the
                // handshake the game ends
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }


    /**
     * This is the method that init the match
     *
     * @param match The Match to initialize
     */
    private void initMatch(final TurnBasedMatch match) {
        // We create the MatchState
        mMatchState = TurnMatchState.get(TurnMatchState.STATE_0);
        // The Other player
        String otherPlayerId = getOtherPlayerId(match);
        Games.TurnBasedMultiplayer.takeTurn(getGoogleApiClient(), match.getMatchId(),
                mMatchState.asByteArray(), otherPlayerId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        if (result.getStatus().isSuccess()) {
                            // We get the new Match state
                            mMatch = result.getMatch();
                            if (mMatch != null) {
                                mMatchState = TurnMatchState.get(mMatch.getData());
                            }
                            showToast("Turn OK");
                        }
                    }
                });
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // We check if we have an invitation. If yes we create the related room and accept it
        final TurnBasedMatch match = getArguments().getParcelable(INVITATION_ARG_KEY);
        if (match != null) {
            mMatch = match;
            mMatchState = TurnMatchState.get(match.getData());
            // We get the information related to the turn match
            showToast("Make your move");
        }
    }

    /**
     * This is the method that make the move
     *
     * @param match The Match for the move
     */
    private void makeMove(final TurnBasedMatch match) {
        if (mMatch == null) {
            showToast("No Match for the move");
            return;
        }
        // We verify if the turn is our turn
        final boolean isMyTurn = match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN;
        if (isMyTurn) {
            // We change the status of the game
            TurnMatchState currentState = TurnMatchState.get(match.getData());
            // We go to the next state
            currentState.nextState();
            // If the state is not the final we persist it otherwise we finish the game
            if (currentState.isFinal()) {
                // In this case we finish the match
                showToast("Finish Match!");
                manageEndGame(match);
            } else {
                String otherPlayerId = getOtherPlayerId(match);
                Games.TurnBasedMultiplayer.takeTurn(getGoogleApiClient(), match.getMatchId(),
                        currentState.asByteArray(), otherPlayerId).setResultCallback(
                        new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                if (result.getStatus().isSuccess()) {
                                    mMatch = result.getMatch();
                                    if (mMatch != null) {
                                        mMatchState = TurnMatchState.get(mMatch.getData());
                                    }
                                    showToast("Turn OK");
                                } else {
                                    showToast("Error making turn " + result.getStatus().getStatusMessage());
                                }
                            }
                        });
            }
        } else {
            showToast("It's not my turn");
        }
    }

    /**
     * Utility method to manage the end of the game
     *
     * @param match The match to finish
     */
    private void manageEndGame(final TurnBasedMatch match) {
        Games.TurnBasedMultiplayer.finishMatch(getGoogleApiClient(), match.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                        if (updateMatchResult.getStatus().isSuccess()) {
                            showToast("Match finished successfully!");
                        } else {
                            showToast("Error closing match!");
                        }

                    }
                });
    }

    /**
     * Return the first player different from the current one
     *
     * @param match The Match
     * @return The other participant Id
     */
    private String getOtherPlayerId(final TurnBasedMatch match) {
        String playerId = Games.Players.getCurrentPlayerId(getGoogleApiClient());
        List<Participant> participants = match.getParticipants();
        for (Participant p : participants) {
            if (!playerId.equals(p.getPlayer().getPlayerId())) {
                return p.getParticipantId();
            }
        }
        return null;
    }

}
