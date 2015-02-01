package uk.co.massimocarli.mathgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the Fragment we use to show how Realtime Multiplayer works
 */
public class RealtimeMultiplayerFragment extends BaseGameFragment {

    /**
     * The key for invitation
     */
    private static final String INVITATION_ARG_KEY = "InvitationKey";

    /**
     * The Request Id for the waiting room
     */
    private static final int WAITING_ROOM_REQUEST_ID = 37;

    /**
     * The Request Id for the invite room
     */
    private static final int INVITE_ROOM_REQUEST_ID = 38;

    /**
     * The Request Id for the invite inbox
     */
    private static final int INVITE_INBOX_REQUEST_ID = 39;

    /**
     * The min number of auto match players for his game
     */
    private static final int MIN_AUTOMATCH_PLAYERS = 1;

    /**
     * The max number of auto match players for his game
     */
    private static final int MAX_AUTOMATCH_PLAYERS = 2;

    /**
     * The min number of invited players for his game
     */
    private static final int MIN_INVITE_PLAYERS = 1;

    /**
     * The max number of invited players for his game
     */
    private static final int MAX_INVITE_PLAYERS = 3;

    /**
     * The min number to launch the game
     */
    private static final int WAITING_ROOM_MIN_PLAYERS = 1;

    /**
     * The Current room
     */
    private Room mRoom;

    /**
     * This is the interface implementation that manages the Room lifecycle
     */
    private final RoomUpdateListener mRoomUpdateListener = new RoomUpdateListener() {
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            if (!statusCodeManaged(statusCode)) {
                showToast("onRoomCreated ");
                printRoomData("onRoomCreated ", room);
                printRoomParticipants(room);
                Log.i(BaseGameFragment.TAG, "" + room);
                // We launch the waiting room
                launchWaitingRoom(room);
            }
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            if (!statusCodeManaged(statusCode)) {
                showToast("onJoinedRoom ");
                printRoomData("onJoinedRoom ", room);
            }
        }

        @Override
        public void onLeftRoom(int statusCode, String roomId) {
            if (!statusCodeManaged(statusCode)) {
                showToast("onLeftRoom " + roomId);
                Log.i(BaseGameFragment.TAG, "roomId: " + roomId);
            }
        }

        @Override
        public void onRoomConnected(int statusCode, Room room) {
            if (!statusCodeManaged(statusCode)) {
                showToast("onRoomConnected ");
                printRoomData("onRoomConnected ", room);
            }
        }
    };

    /**
     * The interface we implement to manage the different status of the Room
     */
    private final RoomStatusUpdateListener mRoomStatusUpdateListener = new RoomStatusUpdateListener() {
        @Override
        public void onRoomConnecting(Room room) {
            showToast("onRoomConnecting ");
            printRoomData("onRoomConnected ", room);
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            showToast("onRoomAutoMatching ");
            printRoomData("onRoomAutoMatching ", room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, List<String> participantIds) {
            showToast("onPeerInvitedToRoom ");
            printRoomData("onPeerInvitedToRoom ", room);
            printRoomParticipants(room, participantIds);
        }

        @Override
        public void onPeerDeclined(Room room, List<String> participantIds) {
            if (room!= null){
                Games.RealTimeMultiplayer.leave(getGoogleApiClient(), null, room.getRoomId());
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                showToast("onPeerDeclined ");
                printRoomData("onPeerDeclined ", room);
                printRoomParticipants(room, participantIds);
            }
        }

        @Override
        public void onPeerJoined(Room room, List<String> participantIds) {
            showToast("onPeerJoined ");
            printRoomData("onPeerJoined ", room);
            printRoomParticipants(room, participantIds);
        }

        @Override
        public void onPeerLeft(Room room, List<String> participantIds) {
            if (room!= null){
                Games.RealTimeMultiplayer.leave(getGoogleApiClient(), null, room.getRoomId());
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                showToast("onPeerLeft ");
                printRoomData("onPeerLeft ", room);
                printRoomParticipants(room, participantIds);
            }
        }

        @Override
        public void onConnectedToRoom(Room room) {
            printRoomData("onPeerLeft ", room);
        }

        @Override
        public void onDisconnectedFromRoom(Room room) {
            showToast("onDisconnectedFromRoom ");
            printRoomData("onDisconnectedFromRoom ", room);
            Games.RealTimeMultiplayer.leave(getGoogleApiClient(), null, room.getRoomId());
            // clear the flag that keeps the screen on
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onPeersConnected(Room room, List<String> participantIds) {
            showToast("onPeersConnected ");
            printRoomData("onPeersConnected ", room);
            printRoomParticipants(room, participantIds);
        }

        @Override
        public void onPeersDisconnected(Room room, List<String> participantIds) {
            if (room!= null){
                Games.RealTimeMultiplayer.leave(getGoogleApiClient(), null, room.getRoomId());
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                showToast("onPeersDisconnected ");
                printRoomData("onPeersDisconnected ", room);
                printRoomParticipants(room, participantIds);
            }
        }

        @Override
        public void onP2PConnected(String participantId) {
            showToast("onP2PConnected " + participantId);
            Log.i(BaseGameFragment.TAG, "participantId: " + participantId);
        }

        @Override
        public void onP2PDisconnected(String participantId) {
            showToast("onP2PDisconnected " + participantId);
            Log.i(BaseGameFragment.TAG, "participantId: " + participantId);
        }
    };

    /**
     * Implementation of the ReliableMessageSentCallback interface to be notified about the
     * message sending status
     */
    private final RealTimeMultiplayer.ReliableMessageSentCallback mReliableMessageSentCallback =
            new RealTimeMultiplayer.ReliableMessageSentCallback() {
                @Override
                public void onRealTimeMessageSent(final int statusCode, final int tokenId,
                                                  final String recipientParticipantId) {
                    // We manage the different type of status
                    String logMessage = null;
                    switch (statusCode) {
                        case GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED:
                            logMessage = "Message sending failed to player " + recipientParticipantId;
                            break;
                        case GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED:
                            logMessage = "Player" + recipientParticipantId + " has not joined the room";
                            break;
                        case GamesStatusCodes.STATUS_OK:
                        default:
                            logMessage = "Message send successfully to " + recipientParticipantId
                                    + " with token " + tokenId;
                    }
                    showToast(logMessage);
                    Log.i(BaseGameFragment.TAG, logMessage);
                }
            };

    /**
     * RealTimeMessageReceivedListener implementation for the messages
     */
    private final RealTimeMessageReceivedListener mMessageReceivedListener = new RealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
            // We define if the message is reliable or not
            final boolean isReliable = realTimeMessage.isReliable();
            // We get the data
            final String message = new String(realTimeMessage.getMessageData());
            // The sender participant
            final String participantId = realTimeMessage.getSenderParticipantId();
            if (isReliable) {
                showToast("Receiver message: " + message + " as reliable message from:" + participantId);
            } else {
                showToast("Receiver message: " + message + " as unreliable message from:" + participantId);
            }
        }
    };

    /**
     * @return A new instance of fragment RealtimeMultiplayerFragment.
     */
    public static RealtimeMultiplayerFragment newInstance() {
        RealtimeMultiplayerFragment fragment = new RealtimeMultiplayerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @return A new instance of fragment RealtimeMultiplayerFragment with invitation.
     */
    public static RealtimeMultiplayerFragment newInstance(final Invitation invitation) {
        RealtimeMultiplayerFragment fragment = new RealtimeMultiplayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(INVITATION_ARG_KEY, invitation);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentLayout = inflater.inflate(R.layout.fragment_realtime, container, false);
        // The Quick Start Button
        fragmentLayout.findViewById(R.id.realtime_quick_start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We initialize the mask for the user
                final int exclusiveBitMask = 0;
                // We create the Bundle with the auto match criteria
                Bundle autoCriteria = RoomConfig.createAutoMatchCriteria(MIN_AUTOMATCH_PLAYERS, MAX_AUTOMATCH_PLAYERS, 0);
                // We create the Room configuration using the previous bundle. We register the
                // RoomUpdateListener implementation
                RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(mRoomUpdateListener);
                // We set the auto criteria
                roomConfigBuilder.setAutoMatchCriteria(autoCriteria);
                // We register the listener for the different status for the Room
                roomConfigBuilder.setRoomStatusUpdateListener(mRoomStatusUpdateListener);
                // We register the listener for the different messages sent
                roomConfigBuilder.setMessageReceivedListener(mMessageReceivedListener);
                // We create the room
                Games.RealTimeMultiplayer.create(getGoogleApiClient(), roomConfigBuilder.build());
                // Here we have to keep the screen on because if the screen go off during the
                // handshake the game ends
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        // The Invite mode
        fragmentLayout.findViewById(R.id.realtime_invite_friend_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getGoogleApiClient(),
                        MIN_INVITE_PLAYERS, MAX_INVITE_PLAYERS);
                startActivityForResult(intent, INVITE_ROOM_REQUEST_ID);
            }
        });
        // The Invite Inbox
        fragmentLayout.findViewById(R.id.realtime_invite_inbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inviteInboxIntent = Games.Invitations.getInvitationInboxIntent(getGoogleApiClient());
                startActivityForResult(inviteInboxIntent, INVITE_INBOX_REQUEST_ID);
            }
        });
        // The send message code
        final EditText inputMessage = (EditText) fragmentLayout.findViewById(R.id.realtime_send_message_input);
        fragmentLayout.findViewById(R.id.realtime_send_reliable_message_button)
                .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final Editable textMessage = inputMessage.getText();
                                            if (!TextUtils.isEmpty(textMessage)) {
                                                sendReliableMessage(textMessage.toString());
                                            } else {
                                                showToast("Message empty");
                                            }
                                        }
                                    }
                );
        fragmentLayout.findViewById(R.id.realtime_send_unreliable_message_button)
                .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final Editable textMessage = inputMessage.getText();
                                            if (!TextUtils.isEmpty(textMessage)) {
                                                sendUnreliableMessage(textMessage.toString());
                                            } else {
                                                showToast("Message empty");
                                            }
                                        }
                                    }
                );
        return fragmentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // We check if we have an invitation. If yes we create the related room and accept it
        final Invitation invitation = getArguments().getParcelable(INVITATION_ARG_KEY);
        if (invitation != null) {
            RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(mRoomUpdateListener)
                    .setRoomStatusUpdateListener(mRoomStatusUpdateListener)
                    .setMessageReceivedListener(mMessageReceivedListener);
            // We add the information related to the invitation to accept
            roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
            // We accept the invitation
            Games.RealTimeMultiplayer.join(getGoogleApiClient(), roomConfigBuilder.build());
            // We keep the screen on
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WAITING_ROOM_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                // We get the current room
                mRoom = data.getParcelableExtra(Multiplayer.EXTRA_ROOM);
                printRoomData("onActivityResult ", mRoom);
                printRoomParticipants(mRoom);
            }
        } else if (requestCode == INVITE_ROOM_REQUEST_ID) {
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
                // We create now the Room with the participants to invite
                RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(mRoomUpdateListener)
                        .setRoomStatusUpdateListener(mRoomStatusUpdateListener)
                        .setMessageReceivedListener(mMessageReceivedListener);
                // We add the participants
                roomConfigBuilder.addPlayersToInvite(invitedPlayers);
                // We set the auto match criteria if any
                if (autoMatchCriteria != null) {
                    roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
                }
                // We create the room
                Games.RealTimeMultiplayer.create(getGoogleApiClient(), roomConfigBuilder.build());
                // Here we have to keep the screen on because if the screen go off during the
                // handshake the game ends
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } else if (requestCode == INVITE_INBOX_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                // get the selected invitation
                Bundle extras = data.getExtras();
                Invitation invitation = extras.getParcelable(Multiplayer.EXTRA_INVITATION);
                RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(mRoomUpdateListener)
                        .setRoomStatusUpdateListener(mRoomStatusUpdateListener)
                        .setMessageReceivedListener(mMessageReceivedListener);
                // We add the information related to the invitation to accept
                roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
                // We accept the invitation
                Games.RealTimeMultiplayer.join(getGoogleApiClient(), roomConfigBuilder.build());
                // We keep the screen on
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    /**
     * Utility method that manages status code
     *
     * @param statusCode The status code
     * @return False if everything is ok and true otherwise
     */
    private boolean statusCodeManaged(final int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return false;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showToast("Reconnect is required");
                return true;
            case GamesStatusCodes.STATUS_REAL_TIME_CONNECTION_FAILED:
                showToast("Connection failed");
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_DISABLED:
                showToast("The Multiplayer status is disabled");
                return true;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showToast("Internal error");
                return true;

        }
        return false;
    }

    /**
     * This utility method sends the message with a reliable mechanism
     *
     * @param messageToSend The message to send
     */
    private void sendReliableMessage(final String messageToSend) {
        if (mRoom != null) {
            // We get the List of participants
            final List<Participant> participants = mRoom.getParticipants();
            // We get the message as array of bytes
            byte[] message = messageToSend.getBytes();
            // We send the message to all the participants but me
            final String myId = mRoom.getParticipantId(Games.Players.getCurrentPlayerId(getGoogleApiClient()));
            for (Participant p : participants) {
                // If the participants is not me
                final String participantId = p.getParticipantId();
                if (!myId.equals(participantId)) {
                    // We send the message in reliable way
                    Games.RealTimeMultiplayer.sendReliableMessage(getGoogleApiClient(), mReliableMessageSentCallback,
                            message, mRoom.getRoomId(), participantId);
                }
            }
        } else {
            showToast("No room connected");
        }
    }

    /**
     * This utility method sends the message with a unreliable mechanism
     *
     * @param messageToSend The message to send
     */
    private void sendUnreliableMessage(final String messageToSend) {
        if (mRoom != null) {
            // We get the message as array of bytes
            byte[] message = messageToSend.getBytes();
            // Send the message to all the other players
            Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(getGoogleApiClient(), message,
                    mRoom.getRoomId());
        } else {
            showToast("No room connected");
        }
    }

    /**
     * Utility method to launch waiting room
     *
     * @param room The room we're waiting for
     */
    private void launchWaitingRoom(final Room room) {
        Intent waitingIntent = Games.RealTimeMultiplayer.getWaitingRoomIntent(getGoogleApiClient(), room, WAITING_ROOM_MIN_PLAYERS);
        startActivityForResult(waitingIntent, WAITING_ROOM_REQUEST_ID);
    }


    /**
     * Utility method that prints data for the room
     *
     * @return The
     */
    private void printRoomData(final String methodName, final Room room) {
        if (room != null) {
            Log.i(BaseGameFragment.TAG, methodName + " Room Id:" + room.getRoomId());
        } else {
            Log.i(BaseGameFragment.TAG, "No Room!");
        }
    }

    /**
     * Utility class to print participants of a room
     *
     * @param room The room
     */
    private void printRoomParticipants(final Room room) {
        if (room == null) {
            Log.i(BaseGameFragment.TAG, "No participants!");
            return;
        }
        final List<String> participantIds = room.getParticipantIds();
        if (participantIds != null) {
            for (String participantId : participantIds) {
                final String participantName = room.getParticipant(participantId).getDisplayName();
                Log.i(BaseGameFragment.TAG, "Participant: " + participantName);
            }
        }
    }

    /**
     * Utility class to print participants of a room
     *
     * @param room           The room
     * @param participantIds The participants ids for the room
     */
    private void printRoomParticipants(final Room room, final List<String> participantIds) {
        if (participantIds != null) {
            for (String participantId : participantIds) {
                final String participantName = room.getParticipant(participantId).getDisplayName();
                Log.i(BaseGameFragment.TAG, "Participant: " + participantName);
            }
        }
    }

}
