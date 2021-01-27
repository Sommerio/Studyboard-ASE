package com.studyboard.datagenerator;

import com.studyboard.model.Deck;
import com.studyboard.model.User;
import com.studyboard.repository.DeckRepository;
import com.github.javafaker.Faker;
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

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final Faker faker;

    public DeckDataGenerator(DeckRepository deckRepository, UserRepository userRepository) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
        faker = new Faker();
    }

    @PostConstruct
    private void generateDecks() {
        if(deckRepository.count() > 0) {
            LOGGER.info("Deck already generated");
        } else {
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
                deckRepository.save(deck);
            }
        }
    }

}
