package com.studyboard;

import com.github.javafaker.Faker;
import com.studyboard.model.Authorities;
import com.studyboard.model.Deck;
import com.studyboard.model.Flashcard;
import com.studyboard.model.User;
import com.studyboard.repository.AuthoritiesRepository;
import com.studyboard.repository.DeckRepository;
import com.studyboard.repository.FlashcardRepository;
import com.studyboard.repository.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class StudyboardApplication {
	public static void main(String[] args) {
		SpringApplication.run(StudyboardApplication.class, args);
	}

	@Autowired
	private DeckRepository deckRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FlashcardRepository flashcardRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private AuthoritiesRepository authoritiesRepository;
	private Faker faker = new Faker();

	@Bean
	InitializingBean sendDatabase() {
		return () -> {
			for(int i=0; i < 20; i++) {
				Flashcard flashcard = new Flashcard();
				if (i % 2 == 0) {
					flashcard.setQuestion(faker.shakespeare().kingRichardIIIQuote());
					flashcard.setAnswer(faker.yoda().quote());
					flashcard.setNextDueDate(LocalDateTime.now());
					flashcard.setConfidenceLevel(0);
				} else {
					flashcard.setQuestion(faker.shakespeare().hamletQuote());
					flashcard.setAnswer(faker.hitchhikersGuideToTheGalaxy().quote());
					flashcard.setConfidenceLevel(faker.number().numberBetween(1, 5));
					flashcard.setNextDueDate(LocalDateTime.of(2021, 01, 29, 8, 9, 45));
				}
				flashcard.setEasiness(2.5);
				flashcard.setCorrectnessStreak(0);
				flashcard.setInterval(0);
				flashcardRepository.save(flashcard);
			}
			authoritiesRepository.save(new Authorities("test", "USER"));
			User user = new User();
			user.setUsername("test");
			user.setPassword(passwordEncoder.encode("123456789"));
			user.setRole("user");
			user.setEmail("test@email.com");
			user.setEnabled(true);
			user.setLoginAttempts(0);
			user = userRepository.save(user);
			for (int i = 0; i < 15; i++) {
				Deck deck = new Deck();
				if(i % 3 == 0) {
					deck.setName(faker.ancient().god());
				} else if(i % 3 == 1) {
					deck.setName(faker.book().author());
				} else {
					deck.setName(faker.superhero().name());
				}
				deck.setCreationDate(LocalDate.of(2021, 01, 15));
				deck.setLastTimeUsed(LocalDateTime.of(2021, 01, 15, 23, 59, 20));
				deck.setSize(faker.number().numberBetween(1, 20));
				deck.setFavorite(false);
				deck.setUser(user);
				Deck saved = deckRepository.save(deck);
				int rand = faker.number().numberBetween(1, 20);
				for (int j = 0; j < deck.getSize(); j++) {
					if(rand + j <= deck.getSize()) {
						flashcardRepository.assignFlashcard(saved.getId(), rand + j);
					} else {
						flashcardRepository.assignFlashcard(saved.getId(), rand + j - deck.getSize());
					}
				}
			}
			Deck deck = new Deck();
			deck.setName("Shakespeare");
			deck.setCreationDate(LocalDate.of(2021, 01, 15));
			deck.setLastTimeUsed(LocalDateTime.of(2021, 01, 15, 23, 59, 20));
			deck.setSize(faker.number().numberBetween(1, 20));
			deck.setFavorite(false);
			deck.setUser(user);
			Deck saved = deckRepository.save(deck);
			int rand = faker.number().numberBetween(1, 20);
			for (int j = 0; j < deck.getSize(); j++) {
				Flashcard flashcard = new Flashcard();
				if (j % 3 == 0) {
					flashcard.setQuestion(faker.shakespeare().kingRichardIIIQuote());
					flashcard.setAnswer("King Richard III");
					flashcard.setNextDueDate(LocalDateTime.now());
				} else if(j % 3 == 1){
					flashcard.setQuestion(faker.shakespeare().hamletQuote());
					flashcard.setAnswer("Hamlet");
					flashcard.setNextDueDate(LocalDateTime.now());
				} else {
					flashcard.setQuestion(faker.shakespeare().romeoAndJulietQuote());
					flashcard.setAnswer("Romeo and Juliet");
					flashcard.setNextDueDate(LocalDateTime.now());
				}
				flashcard.setConfidenceLevel(0);
				flashcard.setEasiness(2.5);
				flashcard.setCorrectnessStreak(0);
				flashcard.setInterval(0);
				Flashcard savedF = flashcardRepository.save(flashcard);
				flashcardRepository.assignFlashcard(saved.getId(), savedF.getId());
			}

		};
	}
}
