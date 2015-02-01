package uk.co.massimocarli.mathgame;

import java.nio.charset.Charset;

/**
 * This class encapsulate the information for the TurnGame
 * <p/>
 * Created by Massimo Carli on 07/12/14.
 */
public final class TurnMatchState {

    /**
     * The First State
     */
    public static final String STATE_0 = "0";

    /**
     * The Second State
     */
    public static final String STATE_1 = "1";

    /**
     * The Third State
     */
    public static final String STATE_2 = "2";

    /**
     * The Forth State
     */
    public static final String STATE_3 = "3";

    /**
     * The Forth and final  State
     */
    public static final String FINAL_STATE = "FINAL_STATE";


    /**
     * The state for the TurnMatch
     */
    private String mState;

    /**
     * Private constructor
     *
     * @param state The current state
     */
    private TurnMatchState(final String state) {
        this.mState = state;
    }

    /**
     * Create a TurnMatchState with the given state
     *
     * @param state The current state
     * @return The TurnMatchState for the given state
     */
    public static TurnMatchState get(final String state) {
        return new TurnMatchState(state);
    }

    /**
     * Create a TurnMatchState with the given state
     *
     * @param stateBytes The current state as array of byte
     * @return The TurnMatchState for the given state
     */
    public static TurnMatchState get(final byte[] stateBytes) {
        if (stateBytes == null || stateBytes.length == 0) {
            return new TurnMatchState(null);
        } else {
            return get(new String(stateBytes, Charset.forName("UTF-8")));
        }
    }

    /**
     * @return The current item as array of byte
     */
    public byte[] asByteArray() {
        if (mState != null) {
            return this.mState.getBytes(Charset.forName("UTF-8"));
        } else {
            return new byte[0];
        }
    }

    /**
     * This method promote the Match to the next state
     */
    public TurnMatchState nextState() {
        final String oldState = this.mState;
        if (oldState == null) {
            mState = TurnMatchState.STATE_0;
        } else if (TurnMatchState.STATE_0.equals(oldState)) {
            mState = TurnMatchState.STATE_1;
        } else if (TurnMatchState.STATE_1.equals(oldState)) {
            mState = TurnMatchState.STATE_2;
        } else if (TurnMatchState.STATE_2.equals(oldState)) {
            mState = TurnMatchState.STATE_3;
        } else if (TurnMatchState.STATE_3.equals(oldState)) {
            mState = TurnMatchState.FINAL_STATE;
        } else {
            mState = TurnMatchState.FINAL_STATE;
        }
        return this;
    }

    /**
     * @return True if the state is the final one
     */
    public boolean isFinal() {
        return TurnMatchState.FINAL_STATE.equals(mState);
    }

}
