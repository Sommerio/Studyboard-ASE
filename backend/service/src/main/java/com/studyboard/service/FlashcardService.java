package com.studyboard.service;

import com.studyboard.exception.FlashcardConstraintException;
import com.studyboard.model.Deck;
import com.studyboard.model.Flashcard;

import java.util.List;

public interface FlashcardService {

    /**
     * Find all decks user created order by the last time they were used for revision
     *
     * @param username of the user who created the deck
     * @return list of all decks order by the last time used attribute
     */
    List<Deck> getAllDecks(String username);

    /**
     * Find a deck with the given id
     *
     * @param deckId of the deck to find
     * @return deck with the specified id
     */
    Deck findDeckById(Long deckId);

    /**
     * Find a deck containing the search parameter in the name
     *
     * @param username of the user searching for own decks
     * @param searchParam to look for in the deck's name
     * @return all decks containing searchParam in the name
     */
    List<Deck> findDecksByName(String username, String searchParam);

    /**
     * Create a single deck
     *
     * @param deck with all the necessary information about a deck
     */
    Deck createDeck(Deck deck);

    /**
     * Update a single deck
     *
     * @param deck - with the information to be updated
     * @return updated deck
     */

    Deck editDeck(Deck deck);

    /**
     * Find all flashcards user created and assigned to one deck
     *
     * @param deckId of the deck in which flashcards are
     * @return list of all flashcards
     */
    List<Flashcard> getAllFlashcardsOfDeck(long deckId);

    /**
     * Get as many flashcards from a deck as the user choose
     *
     * @param deckId of the deck flashcards belong to
     * @param size   - amount of flashcards to revise
     * @param version determines whether to take all due cards or a custom size
     * @param updateLastTimeUsed determines whether the date of usage should be updated (if revision actually occured)
     * @return chosen number of flashcards from a specified deck
     */
    List<Flashcard> getFlashcardsForRevision(long deckId, int size, int version, boolean updateLastTimeUsed);


    /**
     * Find a single flashcard by id
     *
     * @param flashcardId of the flashcard
     * @return the flashcard with the corresponding id
     */
    Flashcard getOneFlashcard(long flashcardId);

    /**
     * Create a single flashcard
     *
     * @param flashcard entity with all the necessary information
     */
    Flashcard createFlashcard(Flashcard flashcard);

    /**
     * Get all decks a flashcard belongs to
     *
     * @param flashcardId of the card
     * @return ids of decks card is assigned to
     */
    List<Long> getAssignments(long flashcardId);

    /**
     * Remove a single flashcard from a deck
     *
     * @param deckId      of the deck in which flashcard is
     * @param flashcardId of the flashcard that should be removed
     */
    void removeAssignment(long deckId, long flashcardId);

    /**
     * Delete a single deck with all of its flashcards
     *
     * @param deckId of the deck that should be deleted
     */
    void deleteDeck(long deckId);

    /**
     * Edit a single flashcard in the deck
     *
     * @param flashcard entity that needs to be edited
     * @return flashcard object with the all the changes
     */
    Flashcard editFlashcard(Flashcard flashcard);

    /**
     * Rate a single flashcard in the deck
     *
     * @param flashcard entity that needs to be edited
     * @throws FlashcardConstraintException when confidence level is outside 1-5 range of values
     */
    void rateFlashcard(Flashcard flashcard) throws FlashcardConstraintException;

}
