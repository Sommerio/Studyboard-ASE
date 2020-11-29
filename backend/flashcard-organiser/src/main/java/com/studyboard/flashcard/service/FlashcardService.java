package com.studyboard.flashcard.service;

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
    public List<Deck> getAllDecks(String username);

    /**
     * Find a single deck by id
     *
     * @param username of the user who created the deck
     * @param deckId of the deck
     * @return the deck with the corresponding id
     */
    public Deck getOneDeck(String username, long deckId);

    /**
     * Create a single deck
     *
     * @param username of the user who is creating the deck
     * @param deck with all the necessary information about a deck
     * @return created deck
     */
    public void createDeck(String username, Deck deck);

    /**
     * Update a single deck
     *
     * @param username of the user who created the deck
     * @param deck   - with the information to be updated
     * @return updated deck
     */
    public Deck updateDeckName(String username, Deck deck);



    public List<Flashcard> getAllFlashcardsOfDeck(long deckId);

    public Flashcard getOneFlashcard(long deckId, long flashcardId);

    public void createFlashcard(long deckId, Flashcard flashcard);

    void deleteDeck(long userId, long deckId);

    void deleteFlashcard(long deckId, long flashcardId);

}
