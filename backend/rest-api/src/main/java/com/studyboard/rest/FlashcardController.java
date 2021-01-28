package com.studyboard.rest;

import com.studyboard.dto.DeckDTO;
import com.studyboard.dto.FlashcardDTO;
import com.studyboard.exception.FlashcardConstraintException;
import com.studyboard.model.Flashcard;
import com.studyboard.service.FlashcardService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/flashcards")
public class FlashcardController {

    @Autowired
    private FlashcardService flashcardService;

    @RequestMapping(value = "/{username}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(
            value = "Get all decks associated with specific user id.",
            authorizations = {@Authorization(value = "apiKey")})
    public List<DeckDTO> getAllDecks(@PathVariable(name = "username") String username) {
        return flashcardService.getAllDecks(username).stream()
                .map(DeckDTO::of)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{username}/{searchParam}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(
            value = "Get all decks containing the search parameter in the name.",
            authorizations = {@Authorization(value = "apiKey")})
    public List<DeckDTO> findDecksByName(
            @PathVariable(name = "username") String username,
            @PathVariable(name = "searchParam") String searchParam) {
        return flashcardService.findDecksByName(username, searchParam).stream()
                .map(DeckDTO::of)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/deck{deckId}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(
            value = "Get a deck with a specific id.",
            authorizations = {@Authorization(value = "apiKey")})
    public DeckDTO getOneDeck(@PathVariable(name = "deckId") Long deckId) {
        return DeckDTO.of(flashcardService.findDeckById(deckId));
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiOperation(
            value = "Create deck associated to a user specified in the DTO",
            authorizations = {@Authorization(value = "apiKey")})
    public DeckDTO createDeck(
            @RequestBody DeckDTO deckDTO) {
        return DeckDTO.of(flashcardService.createDeck(deckDTO.toDeck()));
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            produces = "application/json")
    @ApiOperation(
            value = "Edit a specific deck",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity editDeck(
            @RequestBody DeckDTO deckDTO) {
        flashcardService.editDeck(deckDTO.toDeck());
        return ResponseEntity.ok().build();
    }

    @RequestMapping(
            value = "/{deckId}/flashcards",
            method = RequestMethod.GET,
            produces = "application/json")
    @ApiOperation(
            value = "Get all flashcards associated with specific user and deck id.",
            authorizations = {@Authorization(value = "apiKey")})
    public List<FlashcardDTO> getAllFlashcards(
            @PathVariable(name = "deckId") long deckId) {
        return flashcardService.getAllFlashcardsOfDeck(deckId).stream()
                .map(FlashcardDTO::FlashcardDTOFromFlashcard)
                .collect(Collectors.toList());
    }

    @RequestMapping(
            value = "/{deckId}/size{size}/version{version}/update{updateLastTimeUsed}",
            method = RequestMethod.GET,
            produces = "application/json")
    public List<FlashcardDTO> getFlashcardsForRevision(
            @PathVariable(name = "deckId") long deckId,
            @PathVariable(name = "size") int size,
            @PathVariable(name = "version") int version,
            @PathVariable(name = "updateLastTimeUsed") boolean updateLastTimeUsed) {
        return flashcardService.getFlashcardsForRevision(deckId, size, version, updateLastTimeUsed).stream()
                .map(FlashcardDTO::FlashcardDTOFromFlashcard)
                .collect(Collectors.toList());
    }

    @RequestMapping(
            value = "/{deckId}/flashcard{flashcardId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ApiOperation(
            value = "Get flashcard with specific flashcard, user and deck id.",
            authorizations = {@Authorization(value = "apiKey")})
    public FlashcardDTO getOneFlashcard(
            @PathVariable(name = "flashcardId") long flashcardId) {
        return FlashcardDTO.FlashcardDTOFromFlashcard(
                flashcardService.getOneFlashcard(flashcardId));
    }

    @RequestMapping(
            value = "/flashcard",
            method = RequestMethod.POST,
            produces = "application/json")
    @ApiOperation(
            value = "Create a flashcard.",
            authorizations = {@Authorization(value = "apiKey")})
    public FlashcardDTO createFlashcard(
            @RequestBody FlashcardDTO flashcardDTO) {
        System.out.println(flashcardDTO.toString());
        return FlashcardDTO.FlashcardDTOFromFlashcard(flashcardService.createFlashcard(flashcardDTO.FlashcardFromFlashcardDTO()));
    }

    @RequestMapping(
            value = "/flashcard{flashcardId}/decks",
            method = RequestMethod.GET,
            produces = "application/json")
    @ApiOperation(
            value = "Get all decks a flashcard belongs to.",
            authorizations = {@Authorization(value = "apiKey")})
    public List<Long> getAssignments(
            @PathVariable(name = "flashcardId") long flashcardId) {
        System.out.println("Getting decks flashcard " + flashcardId + " is assigned to");
        return flashcardService.getAssignments(flashcardId);
    }

    @RequestMapping(
            value = "/{deckId}",
            method = RequestMethod.DELETE,
            produces = "application/json")
    @ApiOperation(
            value = "Delete deck.",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity deleteDeck(
            @PathVariable(name = "deckId") long deckId) {
        flashcardService.deleteDeck(deckId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(
            value = "/{deckId}/{flashcardId}",
            method = RequestMethod.DELETE,
            produces = "application/json")
    @ApiOperation(
            value = "Delete flashcard.",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity deleteFlashcard(
            @PathVariable(name = "deckId") long deckId,
            @PathVariable(name = "flashcardId") long flashcardId) {
        flashcardService.removeAssignment(deckId, flashcardId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(
            value = "/flashcard{flashcardId}",
            method = RequestMethod.PUT,
            produces = "application/json")
    @ApiOperation(
            value =
                    "Edit a flashcard's question or answer.",
            authorizations = {@Authorization(value = "apiKey")})
    public FlashcardDTO editFlashcard(
            @RequestBody FlashcardDTO flashcardDTO) {
        return FlashcardDTO.FlashcardDTOFromFlashcard(flashcardService.editFlashcard(flashcardDTO.FlashcardFromFlashcardDTO()));
    }

    @RequestMapping(
            value = "/rate{flashcardId}",
            method = RequestMethod.PUT,
            produces = "application/json")
    @ApiOperation(
            value =
                    "Rate the flashcard based on personal confidence level with the value between 0 and 5.",
            authorizations = {@Authorization(value = "apiKey")})
    public void rateFlashcard(
            @RequestBody FlashcardDTO flashcardDTO)
            throws FlashcardConstraintException {
        flashcardService.rateFlashcard(flashcardDTO.FlashcardFromFlashcardDTO());
    }
}
