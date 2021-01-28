package com.studyboard.service;

import com.studyboard.exception.DeckDoesNotExist;
import com.studyboard.exception.FlashcardConstraintException;
import com.studyboard.exception.FlashcardDoesNotExist;
import com.studyboard.model.Deck;
import com.studyboard.model.Flashcard;
import com.studyboard.model.User;
import com.studyboard.repository.DeckRepository;
import com.studyboard.repository.FlashcardRepository;
import com.studyboard.service.implementation.SimpleFlashcardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
public class FlashcardServiceTest {
    private static final long USER_ID = 1L;
    private static final String USER_USERNAME = "userTest";
    private static final String USER_PASSWORD = "password1";
    private static final String USER_EMAIL = "demo1@email.com";
    private static final Integer USER_LOGIN_ATTEMPTS = 0;
    private static final Boolean USER_ENABLED = true;
    private static final String USER_ROLE = "ADMIN";

    private static final Long DECK_ID = 1L;
    private static final String DECK_NAME = "Test";
    private static final Integer DECK_SIZE = 0;
    private static final LocalDateTime DECK_LAST_TIME_USED = LocalDateTime.of(2020, 12, 02, 15, 17);
    private static final LocalDate DECK_CREATION_DATE = LocalDate.of(2020, 12, 2);

    private static final Long DECK_ID_2 = 2L;
    private static final String DECK_NAME_2 = "Test2";
    private static final Integer DECK_SIZE_2 = 0;
    private static final LocalDateTime DECK_LAST_TIME_USED_2 = LocalDateTime.of(2020, 12, 26, 15, 17);
    private static final LocalDate DECK_CREATION_DATE_2 = LocalDate.of(2020, 12, 26);


    private static final Long FLASHCARD_ID = 1L;
    private static final String FLASHCARD_QUESTION = "Question";
    private static final String FLASHCARD_ANSWER = "Answer";
    private static final String FLASHCARD_QUESTION1 = "Question1";
    private static final String FLASHCARD_ANSWER1 = "Answer1";


    @Mock
    private DeckRepository deckRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private SimpleFlashcardService flashcardService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findAllDecksFromAUser() {

        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setFavorite(false);
        deck.setUser(user);

        Mockito.when(deckRepository.findByUserUsernameOrderByLastTimeUsedDesc(USER_USERNAME)).thenReturn(Arrays.asList(deck));

        List<Deck> response = flashcardService.getAllDecks(USER_USERNAME);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(java.util.Optional.of(DECK_ID), java.util.Optional.of(response.get(0).getId()));
        Assertions.assertEquals(DECK_NAME, response.get(0).getName());
        Assertions.assertEquals(DECK_SIZE, response.get(0).getSize());
        Assertions.assertEquals(DECK_CREATION_DATE, response.get(0).getCreationDate());
        Assertions.assertEquals(DECK_LAST_TIME_USED, response.get(0).getLastTimeUsed());
    }

    @Test
    public void creatingDeckSetsValuesCorrectly() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setFavorite(false);
        deck.setUser(user);
        flashcardService.createDeck(deck);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Deck result = flashcardService.findDeckById(DECK_ID);
        Assertions.assertEquals(DECK_ID, result.getId());
        Assertions.assertEquals(DECK_NAME, result.getName());
        Assertions.assertEquals(false, result.isFavorite());
    }

    @Test
    public void findAllFlashcardsOfADeck() {

        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);

        List<Deck> decks = new ArrayList<>();
        decks.add(deck);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Deck response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(0, response.getSize());

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(decks);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);
        flashcardService.assignFlashcard(response.getId(), flashcard.getId());

        response = flashcardService.findDeckById(DECK_ID);
        Mockito.when(flashcardRepository.findByDeckId(DECK_ID)).thenReturn(Arrays.asList(flashcard));
        List<Flashcard> flashcards = flashcardService.getAllFlashcardsOfDeck(response.getId());

        Assertions.assertEquals(1, flashcards.size());
    }

    @Test
    public void searchForDeckThatDoesNotExist_ThrowsException() {
        Assertions.assertThrows(DeckDoesNotExist.class, () -> {
            flashcardService.findDeckById(DECK_ID);
        });
    }

    @Test
    public void searchForFlashcardThatDoesNotExist_ThrowsException() {
        Assertions.assertThrows(FlashcardDoesNotExist.class, () -> {
            flashcardService.getOneFlashcard(FLASHCARD_ID);
        });
    }

    @Test
    public void addingAndAssigningFlashcardIncreasesDeckSize() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);

        List<Deck> decks = new ArrayList<>();
        decks.add(deck);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Deck response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(0, response.getSize());

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(decks);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);
        flashcardService.assignFlashcard(response.getId(), flashcard.getId());

        response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(1, response.getSize());
    }

    @Test
    public void removingAssignmentOfFlashcardDecreasesDeckSize() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);

        List<Deck> decks = new ArrayList<>();
        decks.add(deck);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Deck response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(0, response.getSize());

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(decks);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);
        flashcardService.assignFlashcard(response.getId(), flashcard.getId());

        response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(1, response.getSize());

        flashcardService.removeAssignment(response.getId(), flashcard.getId());

        response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(0, response.getSize());
    }

    @Test
    public void revisionSizeGreaterThanDeckSize_ThrowsException() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            flashcardService.getFlashcardsForRevision(DECK_ID, 1, 2, true);
        });
    }

    @Test
    public void creatingFlashcardSetsInitialValuesCorrectly() {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        Assertions.assertEquals(2.5, response.getEasiness());
        Assertions.assertEquals(0, response.getCorrectnessStreak());
        Assertions.assertEquals(0, response.getInterval());
        Assertions.assertTrue(response.getNextDueDate().isBefore(LocalDateTime.now()));
    }

    @Test
    public void editingFlashcardSetsNewValuesCorrectly() {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);
        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        Assertions.assertEquals(FLASHCARD_QUESTION, response.getQuestion());
        Assertions.assertEquals(FLASHCARD_ANSWER, response.getAnswer());

        flashcard.setQuestion(FLASHCARD_QUESTION1);
        flashcard.setAnswer(FLASHCARD_ANSWER1);
        flashcardService.editFlashcard(flashcard);

        response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        Assertions.assertEquals(FLASHCARD_QUESTION1, response.getQuestion());
        Assertions.assertEquals(FLASHCARD_ANSWER1, response.getAnswer());
    }

    @Test
    public void editingDeckSetsNewValuesCorrectly() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);
        Deck response = flashcardService.findDeckById(DECK_ID);
        Assertions.assertEquals(DECK_NAME, response.getName());

        deck.setName(DECK_NAME_2);
        flashcardService.editDeck(deck);
        response = flashcardService.findDeckById(DECK_ID);

        Assertions.assertEquals(DECK_NAME_2, response.getName());
    }

    @Test
    public void ratingFlashcardWithConfidenceLevelTooHighThrowsException() {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        Flashcard stored = flashcardService.getOneFlashcard(FLASHCARD_ID);
        stored.setConfidenceLevel(7);

        Assertions.assertThrows(FlashcardConstraintException.class, () -> {
            flashcardService.rateFlashcard(flashcard);
        });
    }

    @Test
    public void ratingFlashcardAsCorrectChangesValuesAccordinglyOnFirstReview() throws FlashcardConstraintException {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setConfidenceLevel(5);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        flashcardService.rateFlashcard(flashcard);
        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        double expectedEasiness = 2.5 - 0.8 + 0.28 * 5 - 0.02 * Math.pow(5, 2);
        Assertions.assertEquals(expectedEasiness, response.getEasiness());
        Assertions.assertEquals(1, response.getCorrectnessStreak());
        Assertions.assertEquals(1, response.getInterval());
        Assertions.assertTrue(response.getNextDueDate().isAfter(LocalDateTime.now()));
        Assertions.assertTrue(response.getNextDueDate().isBefore(LocalDateTime.now().plusDays(1)));
    }

    @Test
    public void ratingFlashcardAsIncorrectChangesValuesAccordinglyOnFirstReview() throws FlashcardConstraintException {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setConfidenceLevel(1);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        flashcardService.rateFlashcard(flashcard);
        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        Assertions.assertEquals(2.5, response.getEasiness());
        Assertions.assertEquals(0, response.getCorrectnessStreak());
        Assertions.assertEquals(1, response.getInterval());
        Assertions.assertTrue(response.getNextDueDate().isAfter(LocalDateTime.now()));
        Assertions.assertTrue(response.getNextDueDate().isBefore(LocalDateTime.now().plusDays(1)));
    }

    @Test
    public void ratingFlashcardAsCorrectTwoTimesChangesValuesAccordinglyOnSecondReview() throws FlashcardConstraintException {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        flashcard.setConfidenceLevel(4);
        flashcardService.rateFlashcard(flashcard);
        flashcard.setConfidenceLevel(5);
        flashcardService.rateFlashcard(flashcard);
        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        double expectedEasiness = 2.5 - 0.8 + 0.28 * 4 - 0.02 * Math.pow(4, 2) - 0.8 + 0.28 * 5 - 0.02 * Math.pow(5, 2);
        Assertions.assertEquals(expectedEasiness, response.getEasiness());
        Assertions.assertEquals(2, response.getCorrectnessStreak());
        Assertions.assertEquals(6, response.getInterval());
        Assertions.assertTrue(response.getNextDueDate().isAfter(LocalDateTime.now()));
        Assertions.assertTrue(response.getNextDueDate().isBefore(LocalDateTime.now().plusDays(6)));
    }

    @Test
    public void ratingFlashcardAsCorrectThenIncorrectChangesValuesAccordinglyOnSecondReview() throws FlashcardConstraintException {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        flashcard.setConfidenceLevel(4);
        flashcardService.rateFlashcard(flashcard);
        flashcard.setConfidenceLevel(2);
        flashcardService.rateFlashcard(flashcard);
        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        double expectedEasiness = 2.5 - 0.8 + 0.28 * 4 - 0.02 * Math.pow(4, 2);
        Assertions.assertEquals(expectedEasiness, response.getEasiness());
        Assertions.assertEquals(0, response.getCorrectnessStreak());
        Assertions.assertEquals(1, response.getInterval());
        Assertions.assertTrue(response.getNextDueDate().isAfter(LocalDateTime.now()));
        Assertions.assertTrue(response.getNextDueDate().isBefore(LocalDateTime.now().plusDays(1)));
    }

    @Test
    public void ratingFlashcardCorrectThreeTimesChangesValuesAccordingly() throws FlashcardConstraintException {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());
        flashcardService.createFlashcard(flashcard);

        Mockito.when(flashcardRepository.findFlashcardById(FLASHCARD_ID)).thenReturn(flashcard);

        flashcard.setConfidenceLevel(4);
        flashcardService.rateFlashcard(flashcard);
        flashcard.setConfidenceLevel(5);
        flashcardService.rateFlashcard(flashcard);
        flashcard.setConfidenceLevel(5);
        flashcardService.rateFlashcard(flashcard);
        Flashcard response = flashcardService.getOneFlashcard(FLASHCARD_ID);

        double expectedEasiness = 2.5 - 0.8 + 0.28 * 4 - 0.02 * Math.pow(4, 2) - 0.8 + 0.28 * 5 - 0.02 * Math.pow(5, 2) - 0.8 + 0.28 * 5 - 0.02 * Math.pow(5, 2);
        int expectedInterval = (int) Math.ceil(6 * expectedEasiness);
        Assertions.assertEquals(expectedEasiness, response.getEasiness());
        Assertions.assertEquals(3, response.getCorrectnessStreak());
        Assertions.assertEquals(expectedInterval, response.getInterval());
        Assertions.assertTrue(response.getNextDueDate().isAfter(LocalDateTime.now()));
        Assertions.assertTrue(response.getNextDueDate().isBefore(LocalDateTime.now().plusDays(expectedInterval)));
    }

    @Test
    public void assigningFlashcardReturnsCorrectDeckAssignments() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(new ArrayList<>());
        flashcard.setDocumentReferences(new ArrayList<>());

        flashcardService.assignFlashcard(DECK_ID, FLASHCARD_ID);
        Mockito.when(flashcardRepository.getAllAssignments(FLASHCARD_ID)).thenReturn(Arrays.asList(DECK_ID));
        List<Long> assignments = flashcardService.getAssignments(FLASHCARD_ID);
        Assertions.assertEquals(DECK_ID, assignments.get(0));
    }

    @Test
    public void getFlashcardsDueTodayCorrectly() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(1);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);

        Mockito.when(deckRepository.findDeckById(DECK_ID)).thenReturn(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID);
        flashcard.setQuestion(FLASHCARD_QUESTION);
        flashcard.setAnswer(FLASHCARD_ANSWER);
        flashcard.setDecks(Arrays.asList(deck));
        flashcard.setDocumentReferences(new ArrayList<>());

        Mockito.when(flashcardRepository.findByDeckIdOrderByDueDateLimitSize(DECK_ID, 1)).thenReturn(Arrays.asList(flashcard));

        List<Flashcard> flashcards = flashcardService.getFlashcardsForRevision(DECK_ID, 1, 2, true);
        Assertions.assertEquals(FLASHCARD_ID, flashcards.get(0).getId());
    }

    @Test
    public void findDeckByNameCorreclty() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);

        Mockito.when(deckRepository.findByUserUsernameAndNameContainingOrderByLastTimeUsedDesc(USER_USERNAME, "T")).thenReturn(Arrays.asList(deck));

        List<Deck> decks = flashcardService.findDecksByName(USER_USERNAME, "T");
        Assertions.assertEquals(DECK_ID, decks.get(0).getId());
    }
}
