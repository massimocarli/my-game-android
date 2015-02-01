package uk.co.massimocarli.mathgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.Leaderboards;


/**
 * This is the Fragment we use to show how Leaderboard works
 */
public class LeaderboardFragment extends BaseGameFragment {

    /**
     * The step for the seek bar
     */
    private static final int SEEK_BAR_STEP = 10;

    /**
     * The initial value for the points in seek bar
     */
    private static final int MAX_POINTS = 200;

    /**
     * The initial value for the points in seek bar
     */
    private static final int INITIAL_POINTS = MAX_POINTS / 2;

    /**
     * The Request Id for the Achievements
     */
    private static final int REQUEST_THE_BEST_LEADERBOARD = 37;

    /**
     * The Request Id for the Achievements
     */
    private static final int REQUEST_ALL_LEADERBOARDS = 38;

    /**
     * The number of points to add
     */
    private int mPointsToAdd = INITIAL_POINTS;

    /**
     * @return A new instance of fragment AchievementsFragment.
     */
    public static LeaderboardFragment newInstance() {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_leaderboards, container, false);
        // The pointsSeekBar for the points to add
        final SeekBar pointsSeekBar = (SeekBar) fragmentLayout.findViewById(R.id.points_seek_bar);
        pointsSeekBar.setProgress(INITIAL_POINTS);
        pointsSeekBar.incrementProgressBy(SEEK_BAR_STEP);
        pointsSeekBar.setMax(MAX_POINTS);
        // The Button
        final Button addPointsButton = (Button) fragmentLayout.findViewById(R.id.add_points_button);
        addPointsButton.setText(getString(R.string.seekbar_points_label, INITIAL_POINTS));
        // The events
        pointsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPointsToAdd = progress;
                addPointsButton.setText(getString(R.string.seekbar_points_label, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // We manage add points button
        addPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Games.Leaderboards.submitScoreImmediate(getGoogleApiClient(),
                        getString(R.string.leaderboard_thebest), mPointsToAdd)
                        .setResultCallback(new ResultCallback<Leaderboards.SubmitScoreResult>() {
                            @Override
                            public void onResult(Leaderboards.SubmitScoreResult submitScoreResult) {
                                final Status status = submitScoreResult.getStatus();
                                if (status.isSuccess()) {
                                    showToast("Points added with success");
                                } else {
                                    showToast("Error adding points " + status.getStatusMessage());
                                }

                            }
                        });
            }
        });
        // We add the button for the single leaderboard
        fragmentLayout.findViewById(R.id.show_the_best_leaderboard_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent showTheBestIntent = Games.Leaderboards.getLeaderboardIntent(getGoogleApiClient(), getString(R.string.leaderboard_thebest));
                startActivityForResult(showTheBestIntent, REQUEST_THE_BEST_LEADERBOARD);
            }
        });
        // We add the button for all the leaderboard
        fragmentLayout.findViewById(R.id.show_all_leaderboards_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent showAllIntent = Games.Leaderboards.getAllLeaderboardsIntent(getGoogleApiClient());
                startActivityForResult(showAllIntent, REQUEST_ALL_LEADERBOARDS);
            }
        });
        return fragmentLayout;
    }

}
