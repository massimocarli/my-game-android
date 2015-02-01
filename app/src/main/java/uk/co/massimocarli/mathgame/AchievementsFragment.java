package uk.co.massimocarli.mathgame;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;


/**
 * This is the Fragment we use to show how Achievements work
 */
public class AchievementsFragment extends BaseGameFragment {

    /**
     * The Request Id for the Achievements
     */
    private static final int REQUEST_ACHIEVEMENTS = 37;

    /**
     * @return A new instance of fragment AchievementsFragment.
     */
    public static AchievementsFragment newInstance() {
        AchievementsFragment fragment = new AchievementsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_achievements, container, false);
        fragmentLayout.findViewById(R.id.unblock_achievement_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We unlock the first achievement
                Games.Achievements.unlockImmediate(getGoogleApiClient(), getString(R.string.achievement_1))
                        .setResultCallback(new ResultCallback<Achievements.UpdateAchievementResult>() {
                            @Override
                            public void onResult(Achievements.UpdateAchievementResult updateAchievementResult) {
                                showToast("Achievements 1 unlocked!");
                            }
                        });
            }
        });
        fragmentLayout.findViewById(R.id.unblock_achievement_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We unlock the second
                Games.Achievements.unlock(getGoogleApiClient(), getString(R.string.achievement_2));
            }
        });
        fragmentLayout.findViewById(R.id.unblock_achievement_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We unlock the second
                Games.Achievements.unlock(getGoogleApiClient(), getString(R.string.achievement_3));
            }
        });
        fragmentLayout.findViewById(R.id.unblock_achievement_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We unlock the second
                Games.Achievements.unlock(getGoogleApiClient(), getString(R.string.achievement_4));
            }
        });
        fragmentLayout.findViewById(R.id.unblock_achievement_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We unlock the second
                Games.Achievements.unlock(getGoogleApiClient(), getString(R.string.achievement_5));
            }
        });
        fragmentLayout.findViewById(R.id.increment_achievement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We increment 1 step
                Games.Achievements.increment(getGoogleApiClient(), getString(R.string.achievement_incremental), 1);
            }
        });
        fragmentLayout.findViewById(R.id.show_achievements).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = Games.Achievements.getAchievementsIntent(getGoogleApiClient());
                startActivityForResult(intent, REQUEST_ACHIEVEMENTS);
            }
        });
        return fragmentLayout;
    }

}
