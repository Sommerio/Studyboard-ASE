package com.studyboard.repository;

import com.studyboard.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("unit-test")
@DataJpaTest
public class FlashcardRepositoryTest {

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
    private static final LocalDateTime DECK_LAST_TIME_USED = LocalDateTime.of(2020, 12, 2, 15, 17);
    private static final LocalDate DECK_CREATION_DATE = LocalDate.of(2020, 12, 2);

    private static final Long SPACE_ID = 1L;
    private static final String SPACE_NAME = "Test";
    private static final LocalDate SPACE_CREATION_DATE = LocalDate.of(2020, 12, 2);

    private static final Long DOCUMENT_ID = 1L;
    private static final Boolean DOCUMENT_NEEDS_TRANSCRIPTION = false;
    private static final String DOCUMENT_TRANSCRIPTION = "";
    private static final String DOCUMENT_NAME = "Test_Document";
    private static final String DOCUMENT_PATH = "";

    private static final Long FLASHCARD_ID_2 = 2L;
    private static final String FLASHCARD_QUESTION_2 = "test_question2";
    private static final String FLASHCARD_ANSWER_2 = "test_answer2";
    private static final Double FLASHCARD_EASINESS_2 = 2.5;
    private static final Integer FLASHCARD_INTERVAL_2 = 0;
    private static final Integer FLASHCARD_CORRECTNESS_STREAK_2 = 0;
    private static final LocalDateTime FLASHCARD_NEXT_DUE_DATE_2 = LocalDateTime.now();

    private static final Long FLASHCARD_ID_3 = 3L;
    private static final String FLASHCARD_QUESTION_3 = "test_question3";
    private static final String FLASHCARD_ANSWER_3 = "test_answer3";
    private static final Double FLASHCARD_EASINESS_3 = 2.5;
    private static final Integer FLASHCARD_INTERVAL_3 = 1;
    private static final Integer FLASHCARD_CORRECTNESS_STREAK_3 = 0;
    private static final LocalDateTime FLASHCARD_NEXT_DUE_DATE_3 = LocalDateTime.now().plusDays(1);

    private static final Long FLASHCARD_ID_4 = 4L;
    private static final String FLASHCARD_QUESTION_4 = "test_question4";
    private static final String FLASHCARD_ANSWER_4 = "test_answer4";
    private static final Double FLASHCARD_EASINESS_4 = 2.6;
    private static final Integer FLASHCARD_INTERVAL_4 = 6;
    private static final Integer FLASHCARD_CORRECTNESS_STREAK_4 = 2;
    private static final LocalDateTime FLASHCARD_NEXT_DUE_DATE_4 = LocalDateTime.now().plusDays(6);

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    public void repositorySavesFlashcardCorrectly() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);
        userRepository.save(user);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);
        deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_2);
        flashcard.setQuestion(FLASHCARD_QUESTION_2);
        flashcard.setAnswer(FLASHCARD_ANSWER_2);
        flashcard.setEasiness(FLASHCARD_EASINESS_2);
        flashcard.setInterval(FLASHCARD_INTERVAL_2);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_2);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_2);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_2);

        Flashcard flashcard1 = flashcardRepository.findFlashcardById(FLASHCARD_ID_2);

        assertAll(
                () -> Assertions.assertEquals(FLASHCARD_ID_2, flashcard1.getId()),
                () -> Assertions.assertEquals(FLASHCARD_QUESTION_2, flashcard1.getQuestion()),
                () -> Assertions.assertEquals(FLASHCARD_ANSWER_2, flashcard1.getAnswer()),
                () -> Assertions.assertEquals(FLASHCARD_EASINESS_2, flashcard1.getEasiness()),
                () -> Assertions.assertEquals(FLASHCARD_INTERVAL_2, flashcard1.getInterval()),
                () -> Assertions.assertEquals(FLASHCARD_CORRECTNESS_STREAK_2, flashcard1.getCorrectnessStreak()),
                () -> Assertions.assertEquals(FLASHCARD_NEXT_DUE_DATE_2, flashcard1.getNextDueDate())
        );

    }

    @Test
    public void repositoryReturnsDueCardsOnly() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);
        userRepository.save(user);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);
        deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_2);
        flashcard.setQuestion(FLASHCARD_QUESTION_2);
        flashcard.setAnswer(FLASHCARD_ANSWER_2);
        flashcard.setEasiness(FLASHCARD_EASINESS_2);
        flashcard.setInterval(FLASHCARD_INTERVAL_2);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_2);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_2);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_2);

        flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_3);
        flashcard.setQuestion(FLASHCARD_QUESTION_3);
        flashcard.setAnswer(FLASHCARD_ANSWER_3);
        flashcard.setEasiness(FLASHCARD_EASINESS_3);
        flashcard.setInterval(FLASHCARD_INTERVAL_3);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_3);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_3);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_3);

        flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_4);
        flashcard.setQuestion(FLASHCARD_QUESTION_4);
        flashcard.setAnswer(FLASHCARD_ANSWER_4);
        flashcard.setEasiness(FLASHCARD_EASINESS_4);
        flashcard.setInterval(FLASHCARD_INTERVAL_4);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_4);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_4);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_4);

        List<Flashcard> flashcards = flashcardRepository.findAllDueToday(DECK_ID, LocalDateTime.now());

        Assertions.assertEquals(1, flashcards.size());
        Assertions.assertEquals(FLASHCARD_ID_2, flashcards.get(0).getId());
    }

    @Test
    public void repositoryReturnsChosenAmountOfCardsOrderedByDueDate() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);
        userRepository.save(user);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);
        deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_2);
        flashcard.setQuestion(FLASHCARD_QUESTION_2);
        flashcard.setAnswer(FLASHCARD_ANSWER_2);
        flashcard.setEasiness(FLASHCARD_EASINESS_2);
        flashcard.setInterval(FLASHCARD_INTERVAL_2);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_2);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_2);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_2);

        flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_3);
        flashcard.setQuestion(FLASHCARD_QUESTION_3);
        flashcard.setAnswer(FLASHCARD_ANSWER_3);
        flashcard.setEasiness(FLASHCARD_EASINESS_3);
        flashcard.setInterval(FLASHCARD_INTERVAL_3);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_3);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_3);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_3);

        flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_4);
        flashcard.setQuestion(FLASHCARD_QUESTION_4);
        flashcard.setAnswer(FLASHCARD_ANSWER_4);
        flashcard.setEasiness(FLASHCARD_EASINESS_4);
        flashcard.setInterval(FLASHCARD_INTERVAL_4);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_4);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_4);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_4);

        List<Flashcard> flashcards = flashcardRepository.findByDeckIdOrderByDueDateLimitSize(DECK_ID, 2);

        Assertions.assertEquals(2, flashcards.size());
        Assertions.assertEquals(FLASHCARD_ID_2, flashcards.get(0).getId());
        Assertions.assertEquals(FLASHCARD_ID_3, flashcards.get(1).getId());
    }

    @Test
    public void assignAndRemoveAssignmentToDeck() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);
        userRepository.save(user);

        Deck deck = new Deck();
        deck.setId(DECK_ID);
        deck.setName(DECK_NAME);
        deck.setSize(DECK_SIZE);
        deck.setCreationDate(DECK_CREATION_DATE);
        deck.setLastTimeUsed(DECK_LAST_TIME_USED);
        deck.setUser(user);
        deckRepository.save(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_2);
        flashcard.setQuestion(FLASHCARD_QUESTION_2);
        flashcard.setAnswer(FLASHCARD_ANSWER_2);
        flashcard.setEasiness(FLASHCARD_EASINESS_2);
        flashcard.setInterval(FLASHCARD_INTERVAL_2);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_2);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_2);
        flashcardRepository.save(flashcard);
        flashcardRepository.assignFlashcard(DECK_ID, FLASHCARD_ID_2);

        List<Flashcard> allFlashcards = flashcardRepository.findByDeckId(DECK_ID);

        Assertions.assertEquals(1, allFlashcards.size());

        flashcardRepository.removeAssignment(DECK_ID, FLASHCARD_ID_2);
        allFlashcards = flashcardRepository.findByDeckId(DECK_ID);

        Assertions.assertEquals(0, allFlashcards.size());
    }

    @Test
    public void assignAndRemoveReference() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USER_USERNAME);
        user.setPassword(USER_PASSWORD);
        user.setEmail(USER_EMAIL);
        user.setLoginAttempts(USER_LOGIN_ATTEMPTS);
        user.setRole(USER_ROLE);
        user.setEnabled(USER_ENABLED);
        userRepository.save(user);

        Space space = new Space();
        space.setId(SPACE_ID);
        space.setName(SPACE_NAME);
        space.setCreationDate(SPACE_CREATION_DATE);
        space.setUser(user);
        spaceRepository.save(space);

        Document document = new Document();
        document.setId(2L);
        document.setName(DOCUMENT_NAME);
        document.setNeedsTranscription(DOCUMENT_NEEDS_TRANSCRIPTION);
        document.setTranscription(DOCUMENT_TRANSCRIPTION);
        document.setFilePath(DOCUMENT_PATH);
        document.setSpace(space);
        documentRepository.save(document);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(FLASHCARD_ID_2);
        flashcard.setQuestion(FLASHCARD_QUESTION_2);
        flashcard.setAnswer(FLASHCARD_ANSWER_2);
        flashcard.setEasiness(FLASHCARD_EASINESS_2);
        flashcard.setInterval(FLASHCARD_INTERVAL_2);
        flashcard.setCorrectnessStreak(FLASHCARD_CORRECTNESS_STREAK_2);
        flashcard.setNextDueDate(FLASHCARD_NEXT_DUE_DATE_2);
        Flashcard f = flashcardRepository.save(flashcard);
        flashcard.setId(f.getId());
        flashcardRepository.addReference(flashcard.getId(), document.getId());

        List<Long> references = flashcardRepository.getAllReferences(flashcard.getId());

        Assertions.assertEquals(document.getId(), references.get(0));

        flashcardRepository.removeReference(flashcard.getId(), document.getId());
        references = flashcardRepository.getAllReferences(flashcard.getId());

        Assertions.assertEquals(0, references.size());
    }
}
