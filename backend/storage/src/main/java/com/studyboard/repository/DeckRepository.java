package com.studyboard.repository;

import com.studyboard.model.Deck;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends CrudRepository<Deck, Long> {

    List<Deck> findAll();

    Optional<Deck> findById(Long id);

    Deck save(Deck deck);
}
