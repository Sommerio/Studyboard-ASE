package com.studyboard.datagenerator;

import com.studyboard.model.Deck;
import com.studyboard.model.Flashcard;
import com.studyboard.model.User;
import com.studyboard.repository.DeckRepository;
import com.github.javafaker.Faker;
import com.studyboard.repository.FlashcardRepository;
import com.studyboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Profile("generateData")
@Component
public class DeckDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckDataGenerator.class);
    private static final int NUMBER_OF_DECKS_TO_GENERATE = 15;
    private static final int NUMBER_OF_FLASHCARDS_TO_GENERATE = 20;


    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final FlashcardRepository flashcardRepository;
    private final Faker faker;

    public DeckDataGenerator(DeckRepository deckRepository, UserRepository userRepository, FlashcardRepository flashcardRepository) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
        this.flashcardRepository = flashcardRepository;
        faker = new Faker();
    }

    @PostConstruct
    private void generateDecksAndCards() {
        LOGGER.info("Generating {} flashcard entries", NUMBER_OF_FLASHCARDS_TO_GENERATE);
        for(int i=0; i < NUMBER_OF_FLASHCARDS_TO_GENERATE; i++) {
                Flashcard flashcard = new Flashcard();
                if (i % 2 == 0) {
                    flashcard.setQuestion(faker.ancient().god());
                    flashcard.setAnswer(faker.friends().quote());
                } else {
                    flashcard.setQuestion(faker.book().title());
                    flashcard.setAnswer(faker.hitchhikersGuideToTheGalaxy().quote());
                }
                flashcard.setConfidenceLevel(0);
                flashcard.setEasiness(2.5);
                flashcard.setCorrectnessStreak(0);
                flashcard.setInterval(0);
                flashcardRepository.save(flashcard);
            }
            User user = new User();
            user.setUsername("test");
            user.setPassword("123456789");
            user.setRole("user");
            user = userRepository.save(user);
            LOGGER.info("Generating {} deck entries", NUMBER_OF_DECKS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_DECKS_TO_GENERATE; i++) {
                Deck deck = new Deck();
                deck.setName(faker.university().name());
                deck.setCreationDate(LocalDate.of(2021, 01, 15));
                deck.setLastTimeUsed(LocalDateTime.of(2021, 01, 15, 23, 59, 20));
                deck.setSize(0);
                deck.setFavorite(false);
                deck.setUser(user);
                LOGGER.debug("saving deck {}", deck);
                Deck saved = deckRepository.save(deck);
                for (int j = 0; j < faker.number().numberBetween(1, 20); j++) {
                    flashcardRepository.assignFlashcard(saved.getId(), faker.number().numberBetween(1, 20));
                }
            }
    }

}
