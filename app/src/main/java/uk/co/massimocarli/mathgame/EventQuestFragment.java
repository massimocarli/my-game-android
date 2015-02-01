package uk.co.massimocarli.mathgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.quest.Quests;


/**
 * This is the Fragment we use to show how Achievements work
 */
public class EventQuestFragment extends BaseGameFragment {

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
     * The number of points to add
     */
    private int mPointsToAdd = INITIAL_POINTS;

    /**
     * @return A new instance of fragment AchievementsFragment.
     */
    public static EventQuestFragment newInstance() {
        EventQuestFragment fragment = new EventQuestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_events_quest, container, false);
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
        fragmentLayout.findViewById(R.id.increment_point_button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String event1Id = getString(R.string.event_1);
                Games.Events.increment(getGoogleApiClient(), event1Id, 1);
            }
        });
        fragmentLayout.findViewById(R.id.increment_point_button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String event2Id = getString(R.string.event_2);
                Games.Events.increment(getGoogleApiClient(), event2Id, 1);
            }
        });
        fragmentLayout.findViewById(R.id.increment_point_button_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String event3Id = getString(R.string.event_3);
                Games.Events.increment(getGoogleApiClient(), event3Id, 1);
            }
        });
        fragmentLayout.findViewById(R.id.show_mission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] selectors = new int[]{Quests.SELECT_OPEN};
                Intent questsIntent = Games.Quests.getQuestsIntent(getGoogleApiClient(), selectors);
                startActivityForResult(questsIntent, 0);
            }
        });

        return fragmentLayout;
    }

}
