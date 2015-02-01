package uk.co.massimocarli.mathgame;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * This is the interface implemented by the Activity that maintaines a reference to the
 * GoogleApiClient object
 * <p/>
 * Created by Massimo Carli on 03/12/14.
 */
public interface GoogleApiClientProvider {

    /**
     * @return The reference to the GoogleApiClient object
     */
    GoogleApiClient getGoogleApiClient();

}
