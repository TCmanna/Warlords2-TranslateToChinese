package com.ebicep.warlords.game.option;

import com.ebicep.warlords.game.Game;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A game exists out of multiple options, who all change the behavior of the
 * game.
 */
public interface Option {

    /**
     * Registers this option to a game. An Option can only be registered to one
     * game, attempting to register an option to multiple game instances may
     * yield undefined behavior.
     *
     * @param game The game instance
     */
    default void register(@Nonnull Game game) {
    }

    /**
     * Called when the game is started (For a typical game, a transition to the
     * <code>PlayingState</code>). Use this method to start your long running
     * tasks
     *
     * @param game The game instance
     */
    default void start(@Nonnull Game game) {
    }

    /**
     * Called when the game transitions to a end state. Generally, the game
     * no longer accepts players, and players can leave the game without
     * affecting their standings.
     *
     * @param game The game instance
     */
    default void onGameEnding(@Nonnull Game game) {
    }

    /**
     * Called when the game transitions to a closed state. This is also when any listeners and gametasks are stopped.
     *
     * @param game The game instance
     */
    default void onGameCleanup(@Nonnull Game game) {
    }

    /**
     * Checks if the given list of options is a valid game configuration. This
     * is used for checking if the current set of options for a valid game. Note
     * that even if this method returns normally, the game may still be in an
     * valid state
     *
     * @param options The list of options to check
     * @throws IllegalArgumentException If the list of options contains a
     * mistake, to be further defined by the option itself
     */
    default void checkConflicts(List<Option> options) {
    }
}
