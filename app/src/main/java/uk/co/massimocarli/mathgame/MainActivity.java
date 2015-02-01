package uk.co.massimocarli.mathgame;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.quest.Quest;
import com.google.android.gms.games.quest.QuestUpdateListener;
import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.games.request.Requests;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClientProvider {

    private static final String TAG = "MainActivity";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Games.API)
                    .addApi(Plus.API)
                    .addApi(Drive.API)
                    .addScope(Games.SCOPE_GAMES)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        // Here we have to check if we have received an invite
        if (connectionHint != null) {
            // we check if we have received an invite
            Invitation invitation = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null) {
                // We create the multiplayer Fragment
                final RealtimeMultiplayerFragment realtimeMultiplayerFragment = RealtimeMultiplayerFragment.newInstance(invitation);
                // We add the Fragment
                getFragmentManager().beginTransaction().replace(R.id.anchor_point, realtimeMultiplayerFragment).commit();
                return;
            }
            // We check if the game is turn based
            TurnBasedMatch turnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
            if (turnBasedMatch != null) {
                // We create the turn Fragment
                final TurnMultiplayerGameFragment turnMultiplayerFragment = TurnMultiplayerGameFragment.newInstance(turnBasedMatch);
                // We add the Fragment
                getFragmentManager().beginTransaction().replace(R.id.anchor_point, turnMultiplayerFragment).commit();
                return;
            }
            // We check for the Gift
            ArrayList<GameRequest> gameRequests = Games.Requests.getGameRequestsFromBundle(connectionHint);
            if (gameRequests != null) {
                // We create the turn Fragment
                final GiftWishFragment giftWishFragment = GiftWishFragment.newInstance(gameRequests);
                // We add the Fragment
                getFragmentManager().beginTransaction().replace(R.id.anchor_point, giftWishFragment).commit();
            }
            // We Manage quests
            Games.Quests.registerQuestUpdateListener(mGoogleApiClient, new QuestUpdateListener() {
                @Override
                public void onQuestCompleted(Quest quest) {
                    // We get the reward
                    Games.Quests.claim(mGoogleApiClient, quest.getQuestId(),
                            quest.getCurrentMilestone().getMilestoneId());
                    byte[] rewardData = quest.getCurrentMilestone().getCompletionRewardData();
                    Toast.makeText(MainActivity.this, "Reward:" + new String(rewardData), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.menu_option_achievements) {
            getSupportActionBar().setTitle(R.string.achievement_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, AchievementsFragment.newInstance()).commit();
        } else if (itemId == R.id.menu_leaderboards_label) {
            getSupportActionBar().setTitle(R.string.leaderboard_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, LeaderboardFragment.newInstance()).commit();
        } else if (itemId == R.id.menu_realtime_multiplayer_label) {
            getSupportActionBar().setTitle(R.string.realtime_multiplayer_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, RealtimeMultiplayerFragment.newInstance()).commit();
        } else if (itemId == R.id.menu_turn_label) {
            getSupportActionBar().setTitle(R.string.menu_turn_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, TurnMultiplayerGameFragment.newInstance()).commit();
        } else if (itemId == R.id.menu_gift_wish) {
            getSupportActionBar().setTitle(R.string.menu_gift_wish_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, GiftWishFragment.newInstance()).commit();
        } else if (itemId == R.id.menu_event_quest) {
            getSupportActionBar().setTitle(R.string.menu_event_quest_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, EventQuestFragment.newInstance()).commit();
        } else if (itemId == R.id.menu_save_state) {
            getSupportActionBar().setTitle(R.string.menu_save_state_label);
            getFragmentManager().beginTransaction().replace(R.id.anchor_point, SaveStateFragment.newInstance()).commit();
        }


        return super.onOptionsItemSelected(item);
    }
}
