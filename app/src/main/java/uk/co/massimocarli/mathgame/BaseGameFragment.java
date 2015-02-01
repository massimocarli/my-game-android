package uk.co.massimocarli.mathgame;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Massimo Carli on 03/12/14.
 */
public class BaseGameFragment extends Fragment {

    /**
     * The Tag for the Log
     */
    public static final String TAG = BaseGameFragment.class.getSimpleName();

    /**
     * The reference to the provider for the GoogleApiClient object
     */
    private GoogleApiClientProvider mGoogleApiClientProvider;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof GoogleApiClientProvider) {
            mGoogleApiClientProvider = (GoogleApiClientProvider) activity;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mGoogleApiClientProvider = null;
    }


    /**
     * @return The GoogleApiClient object if any
     */
    protected GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClientProvider != null) {
            return mGoogleApiClientProvider.getGoogleApiClient();
        }
        return null;
    }


    /**
     * Utility method that shows a Toast (short) with the given message
     *
     * @param text The message for the Toast
     */
    protected void showToast(final String text) {
        Log.i(TAG, text);
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
