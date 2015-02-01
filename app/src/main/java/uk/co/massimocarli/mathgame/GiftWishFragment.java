package uk.co.massimocarli.mathgame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.GameRef;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.games.request.Requests;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This is the Fragment we use to show how Achievements work
 */
public class GiftWishFragment extends BaseGameFragment {

    /**
     * The key for invitation
     */
    private static final String GIFT_WISH_ARG_KEY = "GIFT_WISH_ARG_KEY";

    /**
     * The lifetime for the gift or wish in days
     */
    private static final int GIFT_WISH_LIFETIME_DAYS = 5;

    /**
     * The Request id for the creation of the Gift
     */
    private static final int GIFT_REQUEST_ID = 237;

    /**
     * The Request id for the creation of the Wish
     */
    private static final int WISH_REQUEST_ID = 238;

    /**
     * This is the implementation for the ResultCallback used when we have to accept the gift
     * or wish
     */
    private final ResultCallback<Requests.UpdateRequestsResult> mResultCallback =
            new ResultCallback<Requests.UpdateRequestsResult>() {
                @Override
                public void onResult(Requests.UpdateRequestsResult updateRequestsResult) {

                }
            };

    /**
     * @return A new instance of fragment GiftWishFragment.
     */
    public static GiftWishFragment newInstance() {
        GiftWishFragment fragment = new GiftWishFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @return A new instance of fragment GiftWishFragment.
     */
    public static GiftWishFragment newInstance(final ArrayList<GameRequest> gameRequests) {
        GiftWishFragment fragment = new GiftWishFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(GIFT_WISH_ARG_KEY, gameRequests);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_gift_wish, container, false);
        final Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        fragmentLayout.findViewById(R.id.gift_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We create a Gift
                Intent giftIntent = Games.Requests.getSendIntent(getGoogleApiClient(), GameRequest.TYPE_GIFT,
                        "GiftData".getBytes(), GIFT_WISH_LIFETIME_DAYS, icon, "Gift Example");
                startActivityForResult(giftIntent, GIFT_REQUEST_ID);
            }
        });
        fragmentLayout.findViewById(R.id.wish_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We create a Wish
                Intent giftIntent = Games.Requests.getSendIntent(getGoogleApiClient(), GameRequest.TYPE_WISH,
                        "WishData".getBytes(), GIFT_WISH_LIFETIME_DAYS, icon, "Wish Example");
                startActivityForResult(giftIntent, WISH_REQUEST_ID);
            }
        });
        return fragmentLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GIFT_REQUEST_ID) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    showToast("Gift successfully sent!");
                    break;
                case GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED:
                    showToast("Error sending Gift");
                    break;
            }
        } else if (requestCode == WISH_REQUEST_ID) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    showToast("Wish successfully sent!");
                    break;
                case GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED:
                    showToast("Error sending Wish");
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ArrayList<GameRequest> gameRequests = getArguments().getParcelableArrayList(GIFT_WISH_ARG_KEY);
        if (gameRequests != null) {
            // We get the id for the gift requests
            ArrayList<String> requestIds = new ArrayList<String>();
            // We use a Map for mapping the id for the request to the request itself
            final HashMap<String, GameRequest> gameRequestMap = new HashMap<String, GameRequest>();
            // We initialize the map
            for (GameRequest request : gameRequests) {
                String requestId = request.getRequestId();
                requestIds.add(requestId);
                gameRequestMap.put(requestId, request);
            }
            // We accept all the gifts
            Games.Requests.acceptRequests(getGoogleApiClient(), requestIds)
                    .setResultCallback(new ResultCallback<Requests.UpdateRequestsResult>() {
                        @Override
                        public void onResult(Requests.UpdateRequestsResult result) {
                            // We want to read the state for all the accepted requests
                            for (String requestId : result.getRequestIds()) {
                                if (!gameRequestMap.containsKey(requestId)
                                        || result.getRequestOutcome(requestId)
                                        != Requests.REQUEST_UPDATE_OUTCOME_SUCCESS) {
                                    continue;
                                }
                                final int reqType = gameRequestMap.get(requestId).getType();
                                switch (reqType) {
                                    case GameRequest.TYPE_GIFT:
                                        // Process the game gifts request
                                        manageGift(gameRequestMap.get(requestId));
                                        break;
                                    case GameRequest.TYPE_WISH:
                                        // Process the wish request
                                        manageWish(gameRequestMap.get(requestId));
                                        break;
                                }
                            }
                        }
                    });
        }
    }


    /**
     * Utility method that contains the logic for the Gift management
     *
     * @param giftRequest The request for the gift
     */
    private void manageGift(final GameRequest giftRequest) {
        // We read the info into the gift
        final String gift = new String(giftRequest.getData());
        showToast("Received Gift: " + gift);
    }

    /**
     * Utility method that contains the logic for the Wish management
     *
     * @param wishRequest The request for the wish
     */
    private void manageWish(final GameRequest wishRequest) {
        // We read the info into the wish
        final String wish = new String(wishRequest.getData());
        showToast("Received Wish: " + wish);
    }
}
