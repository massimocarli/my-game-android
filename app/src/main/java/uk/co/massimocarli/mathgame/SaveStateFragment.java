package uk.co.massimocarli.mathgame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.io.IOException;
import java.util.Calendar;


/**
 * This is the Fragment we use to show how Achievements work
 */
public class SaveStateFragment extends BaseGameFragment {

    /**
     * The Request Id for the Savings UI
     */
    private static final int SAVINGS_REQUEST_ID = 437;

    /**
     * The Number of savings data to show
     */
    private static final int NUM_SAVINGS_TO_SHOW = 5;

    /**
     * The current name for the saving if any
     */
    private String mCurrentSavedGame;

    /**
     * @return A new instance of fragment AchievementsFragment.
     */
    public static SaveStateFragment newInstance() {
        SaveStateFragment fragment = new SaveStateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_save_state, container, false);
        fragmentLayout.findViewById(R.id.save_state_show_savings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We launch the saved state UI
                final String label = getString(R.string.save_state_show_savings);
                Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(getGoogleApiClient(),
                        label, true, true, NUM_SAVINGS_TO_SHOW);
                startActivityForResult(savedGamesIntent, SAVINGS_REQUEST_ID);

            }
        });
        return fragmentLayout;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVINGS_REQUEST_ID && data != null) {
            if (data.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                // In this case we have pressed the load so we have the Snapshot object with the data
                SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
                        data.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
                // We get the name for this Snapshot
                mCurrentSavedGame = snapshotMetadata.getUniqueName();
                // We load the data from the SnapShot
                loadSnapshotData();
            } else if (data.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
                // We create the name of the saving for using the timestamp. We could use
                // a random value
                mCurrentSavedGame = "snapshotTemp-" + System.currentTimeMillis();
                // We save the data
                saveSnapshotData(null);
            }
        }
    }


    /**
     * Utility method to manage Snapshot loading
     */
    private void loadSnapshotData() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // We open the Snapshot using its name
                Snapshots.OpenSnapshotResult result = Games.Snapshots.open(getGoogleApiClient(),
                        mCurrentSavedGame, true).await();
                // We get the status of the reading
                int status = result.getStatus().getStatusCode();
                if (status == GamesStatusCodes.STATUS_OK) {
                    // In this case the reading was ok so we get the Snapshot object
                    Snapshot snapshot = result.getSnapshot();
                    // We read the data from it
                    final byte[] data;
                    String strData = null;
                    try {
                        data = snapshot.getSnapshotContents().readFully();
                        strData = new String(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return strData;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    showToast("Restored data: " + result);
                } else {
                    showToast("Error reading data: ");
                }
            }
        }.execute();
    }

    /**
     * Utility method to manage the Snapshot saving
     */
    private void saveSnapshotData(final SnapshotMetadata metadata) {
        new AsyncTask<String, Void, Snapshots.OpenSnapshotResult>() {

            @Override
            protected Snapshots.OpenSnapshotResult doInBackground(String... params) {
                // Here we get the Snapshot using the open() method
                if (metadata == null) {
                    return Games.Snapshots.open(getGoogleApiClient(), mCurrentSavedGame, true)
                            .await();
                } else {
                    return Games.Snapshots.open(getGoogleApiClient(), metadata)
                            .await();
                }
            }

            @Override
            protected void onPostExecute(Snapshots.OpenSnapshotResult openSnapshotResult) {
                super.onPostExecute(openSnapshotResult);
                // Here e have to manage the result that could contain some error
                int status = openSnapshotResult.getStatus().getStatusCode();
                switch (status) {
                    case GamesStatusCodes.STATUS_OK:
                    case GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE:
                        // Everything is ok so we persist the data
                        persist(openSnapshotResult.getSnapshot());
                        break;
                    case GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT:
                        manageConflict(openSnapshotResult);
                        break;
                }
            }
        }.execute();
    }


    /**
     * Utility method to persist a Snapshot
     *
     * @param snapshot The Snapshot to persist
     */
    private void persist(final Snapshot snapshot) {
        // We save the data into the opened Snapshot
        final String savedData = "data-" + System.currentTimeMillis();
        snapshot.getSnapshotContents().writeBytes(savedData.getBytes());
        // We get the image
        final Bitmap coverImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(coverImage)
                .setDescription("Persisted data at : " + Calendar.getInstance().getTime())
                .build();
        Games.Snapshots.commitAndClose(getGoogleApiClient(), snapshot, metadataChange);
        showToast("Persisted data " + savedData);
        Log.i(BaseGameFragment.TAG, "Snapshot :" + snapshot);
    }

    /**
     * Utility method to manage a conflict for a Snapshot
     *
     * @param result The Snapshots.OpenSnapshotResult with the conflict
     */
    private void manageConflict(final Snapshots.OpenSnapshotResult result) {
        // We get the first Snapshot
        Snapshot snapshot = result.getSnapshot();
        // And the conflicting one
        Snapshot conflictSnapshot = result.getConflictingSnapshot();
        // We decide which one to keep as resolving one. To do that we use the information
        // related to the last modified date
        Snapshot resolvedSnapshot = snapshot;
        if (snapshot.getMetadata().getLastModifiedTimestamp() <
                conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
            resolvedSnapshot = conflictSnapshot;
        }
        // We resolve the conflict
        Snapshots.OpenSnapshotResult resolveResult = Games.Snapshots.resolveConflict(
                getGoogleApiClient(), result.getConflictId(), resolvedSnapshot)
                .await();
        final int status = resolveResult.getStatus().getStatusCode();
        if (status == GamesStatusCodes.STATUS_OK) {
            showToast("Conflict fixed");
        } else {
            showToast("Cannot fix the conflict");
        }
    }

}
