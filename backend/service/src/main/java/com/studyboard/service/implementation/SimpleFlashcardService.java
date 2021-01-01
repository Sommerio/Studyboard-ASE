package com.studyboard.service.implementation;

import com.studyboard.exception.DeckDoesNotExist;
import com.studyboard.exception.FlashcardConstraintException;
import com.studyboard.exception.FlashcardDoesNotExist;
import com.studyboard.exception.UserDoesNotExist;
import com.studyboard.model.Deck;
import com.studyboard.model.Document;
import com.studyboard.model.Flashcard;
import com.studyboard.model.User;
import com.studyboard.repository.DeckRepository;
import com.studyboard.repository.FlashcardRepository;
import com.studyboard.repository.UserRepository;
import com.studyboard.service.FlashcardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service used to manage decks and flashcards. Performs decks and flashcards creation, getting, edit and deletion
 */
@Service
public class SimpleFlashcardService implements FlashcardService {

    private final Logger logger = LoggerFactory.getLogger(FlashcardService.class);

    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;

    @Override
    public List<Deck> getAllDecks(String username) {
        logger.info("Getting all decks belonging to the user with username " + username);
        return deckRepository.findByUserUsernameOrderByLastTimeUsedDesc(username);
    }

    @Override
    public List<Deck> findDecksByName(String searchParam) {
        logger.info("Getting all decks containing " + searchParam + " in the name.");
        return deckRepository.findByNameContaining(searchParam);
    }

    @Override
    public void createDeck(Deck deck) {
        deckRepository.save(deck);
        logger.info("Created new deck with name "
                + deck.getName() +
                " for user with username "
                + deck.getUser().getUsername());
    }

    @Override
    public Deck updateDeckName(Deck deck) {
        Deck storedDeck = findDeckById(deck.getId());
        logger.info("Changed the deck name: from "
                + storedDeck.getName() + " to: "
                + deck.getName());
        storedDeck.setName(deck.getName());
        return deckRepository.save(storedDeck);
    }

    @Override
    public List<Flashcard> getAllFlashcardsOfDeck(long deckId) {
        Deck deck = findDeckById(deckId);
        logger.info("Getting all flashcards belonging to the deck with name " + deck.getName());
        return flashcardRepository.findByDeckId(deckId);
    }

    @Override
    public List<Flashcard> getFlashcardsForRevision(long deckId, int size, int version) {
        Deck deck = findDeckById(deckId);
        if (version == 2 && deck.getSize() < size) {
            throw new IllegalArgumentException("Deck size too large!");
        }
        deck.setLastTimeUsed(LocalDateTime.now());
        deckRepository.save(deck);
        if (version == 1) {
            logger.info("Getting all flashcards that are scheduled for revision now, belonging to the deck with name " + deck.getName());
            return flashcardRepository.findAllDueToday(deckId, LocalDateTime.now());
        } else {
            logger.info("Getting " + size + " flashcards belonging to the deck with name " + deck.getName());
            return flashcardRepository.findByDeckIdOrderByDueDateLimitSize(deckId, size);
        }
        /*List<Flashcard> all = getAllFlashcardsOfDeck(deckId);
        List<Flashcard> copy = new ArrayList<>(all);
        List<Flashcard> random = new ArrayList<>();
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < size; i++) {
            random.add(copy.remove(rand.nextInt(copy.size())));
        }
        logger.info("Getting " + size + " flashcards of the deck named " + deck.getName() + " for revision");
        return random;*/
    }

    @Override
    public Flashcard getOneFlashcard(long flashcardId) {
        Flashcard flashcard = flashcardRepository.findFlashcardById(flashcardId);
        if (flashcard == null) {
            throw new FlashcardDoesNotExist();
        }
        logger.info("Getting flashcard with question " + flashcard.getQuestion());
        return flashcard;
    }

    @Override
    public Flashcard createFlashcard(Flashcard flashcard) {
        flashcard.setEasiness(2.5);
        flashcard.setCorrectnessStreak(0);
        flashcard.setInterval(0);
        flashcard.setNextDueDate(LocalDateTime.now());
        List<Deck> decks = flashcard.getDecks();
        List<Document> documents = flashcard.getDocumentReferences();
        Flashcard created = flashcardRepository.save(flashcard);
        logger.info("Created new flashcard with question " + flashcard.getQuestion());
        for(Deck deck : decks) {
            this.assignFlashcard(deck.getId(), created.getId());
        }
        for(Document document : documents) {
            this.addReference(created.getId(), document.getId());
        }
        //created.setDecks(decks);
        //created.setDocumentReferences(documents);
        return created;
    }

    public void assignFlashcard(long deckId, long flashcardId) {
        flashcardRepository.assignFlashcard(deckId, flashcardId);
        logger.info("Assigning flashcard " + flashcardId + " to deck" + deckId);
        Deck deck = findDeckById(deckId);
        deck.setSize(deck.getSize() + 1);
        deckRepository.save(deck);
    }

    @Override
    public void assignFlashcard(long flashcardId, String decks) {
        String[] deckIds = decks.split("#deck#");
        for (int i = 0; i < deckIds.length; i++) {
            if (!deckIds[i].equals("")) {
                long id = Long.parseLong(deckIds[i]);
                flashcardRepository.assignFlashcard(id, flashcardId);
                logger.info("Assigning flashcard " + flashcardId + " to deck" + id);
                Deck deck = findDeckById(id);
                deck.setSize(deck.getSize() + 1);
                deckRepository.save(deck);
            }
        }
    }

    @Override
    public void removeAssignment(long deckId, long flashcardId) {
        flashcardRepository.removeAssignment(deckId, flashcardId);
        logger.info("Removing assignment for flashcard " + flashcardId + " from deck" + deckId);
        Deck deck = findDeckById(deckId);
        deck.setSize(deck.getSize() - 1);
        deckRepository.save(deck);
    }

    @Override
    public List<Long> getAssignments(long flashcardId) {
        logger.info("Getting all decks flashcard " + flashcardId + " is assigned to.");
        return flashcardRepository.getAllAssignments(flashcardId);
    }

    @Override
    public void deleteDeck(long deckId) {
        Deck deck = findDeckById(deckId);
        deckRepository.deleteById(deckId);
        logger.info("Delete deck with name " + deck.getName());
    }

    @Override
    public Flashcard editFlashcard(Flashcard flashcard) {
        Flashcard storedFlashcard = getOneFlashcard(flashcard.getId());
        storedFlashcard.setQuestion(flashcard.getQuestion());
        storedFlashcard.setAnswer(flashcard.getAnswer());
        for(Deck deck : storedFlashcard.getDecks()) {
            removeAssignment(deck.getId(), storedFlashcard.getId());
        }
        for(Deck deck  : flashcard.getDecks()) {
            assignFlashcard(deck.getId(), storedFlashcard.getId());
        }
        for(Document document : storedFlashcard.getDocumentReferences()) {
            removeReference(storedFlashcard.getId(), document.getId());
        }
        for(Document document : flashcard.getDocumentReferences()) {
            addReference(storedFlashcard.getId(), document.getId());
        }
        storedFlashcard.setDocumentReferences(flashcard.getDocumentReferences());
        logger.info("Edited the flashcard with question " + storedFlashcard.getQuestion());
        Flashcard editedFlashcard = flashcardRepository.save(storedFlashcard);
        System.out.println(flashcard.getDocumentReferences().size());
        System.out.println(editedFlashcard.getDocumentReferences().size());
        return editedFlashcard;
        //return flashcardRepository.save(storedFlashcard);
    }

    private void addReference(long flashcardId, long documentId) {
        logger.info("Adding a reference to document " + documentId + " for flashcard " + flashcardId);
        flashcardRepository.addReference(flashcardId, documentId);
    }

    private void removeReference(long flashcardId, long documentId) {
        logger.info("Removing a reference to document " + documentId + " from flashcard " + flashcardId);
        flashcardRepository.removeReference(flashcardId, documentId);
    }

    @Override
    public void rateFlashcard(Flashcard flashcard) throws FlashcardConstraintException {
        Flashcard storedFlashcard = getOneFlashcard(flashcard.getId());
        try {
            storedFlashcard.setConfidenceLevel(flashcard.getConfidenceLevel());
            //SM-2 Algorithm Calculations
            if (storedFlashcard.getConfidenceLevel() >= 3) {
                double easiness = storedFlashcard.getEasiness() - 0.8 + 0.28 * storedFlashcard.getConfidenceLevel() - 0.02 * Math.pow(storedFlashcard.getConfidenceLevel(), 2);
                if (easiness < 1.3) {
                    storedFlashcard.setEasiness(1.3);
                } else {
                    storedFlashcard.setEasiness(easiness);
                }
                if (storedFlashcard.getCorrectnessStreak() == 0) {
                    storedFlashcard.setInterval(1);
                } else if (storedFlashcard.getCorrectnessStreak() == 1) {
                    storedFlashcard.setInterval(6);
                } else {
                    storedFlashcard.setInterval((int) Math.ceil(storedFlashcard.getInterval() * storedFlashcard.getEasiness()));
                }
                storedFlashcard.setCorrectnessStreak(storedFlashcard.getCorrectnessStreak() + 1);
            } else {
                storedFlashcard.setInterval(1);
                storedFlashcard.setCorrectnessStreak(0);
            }
            storedFlashcard.setNextDueDate(LocalDateTime.now().plusDays(storedFlashcard.getInterval()));
            logger.info("Rated the flashcard with question " + storedFlashcard.getQuestion());
            flashcardRepository.save(storedFlashcard);
        } catch (ConstraintViolationException e) {
            throw new FlashcardConstraintException("Flashcard confidence level must be between 1 and 5!");
        }

    }

    @Override
    public Deck findDeckById(Long deckId) {
        Deck deck = deckRepository.findDeckById(deckId);
        if (deck == null) {
            logger.warn("Deck does not exist");
            throw new DeckDoesNotExist();
        }
        logger.info("Searching for the deck with the name " + deck.getName());
        return deck;
    }

    public Flashcard findFlashcardById(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findFlashcardById(flashcardId);
        if (flashcard == null) {
            logger.warn("Flashcard does not exist");
            //throw new DeckDoesNotExist();
        }
        return flashcard;
    }

    private User findUserById(long userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            logger.warn("User does not exist");
            throw new UserDoesNotExist();
        }
        return user;
    }
}



