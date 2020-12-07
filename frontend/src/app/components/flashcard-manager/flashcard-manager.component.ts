import {Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {FlashcardService} from '../../services/flashcard.service';
import {UserService} from '../../services/user.service';
import {Deck} from "../../dtos/deck"
import {User} from '../../dtos/user';


@Component({
  selector: 'app-flashcard-manager',
  templateUrl: './flashcard-manager.component.html',
  styleUrls: ['./flashcard-manager.component.scss']
})

export class FlashcardManagerComponent implements OnInit {

  deckForm: FormGroup;
  deckEditForm: FormGroup;
  error: boolean = false;
  errorMessage: string = '';
  load: boolean = true;
  private decks: Deck[];


  constructor(private formBuilder: FormBuilder, private flashcardService: FlashcardService, private userService: UserService) {
    this.deckForm = this.formBuilder.group({
      title: ['']
    })
    this.deckEditForm = this.formBuilder.group({
      title: ['']
    })
   }

  ngOnInit(): void {
    this.loadAllDecks();
  }

  /**
  * Get a list of all decks belonging to the logged-in user from backend
  */
  loadAllDecks() {
    this.flashcardService.getDecks(localStorage.getItem('currentUser')).subscribe(
        (decksList : Deck[]) => {
                     this.decks = decksList;
                     },
                     error => {
                           this.defaultErrorHandling(error);
                     }
                 );
  }

  /**
  * @return all decks belonging to the logged-in user
  */
  getDecks() {
    return this.decks;
  }

  /**
   * Builds a deck dto and sends a creation request.
   */
  createDeck() {
    const date = new Date();
    date.setHours(date.getHours() - date.getTimezoneOffset() / 60);
    const dateString = date.toISOString();
    this.userService.getUserByUsername(localStorage.getItem('currentUser')).subscribe(res => {
       const deck = new Deck(0, this.deckForm.controls.title.value, 0, dateString, dateString, res);
           this.flashcardService.createDeck(deck, localStorage.getItem('currentUser')).subscribe(
                () => {
                       this.loadAllDecks();
                       },
                       error => {
                         this.defaultErrorHandling(error);
                       }
                     );
    });
   //this.deckForm.reset({'title':''});
  }

  saveEdits(deck: Deck) {
      //send edits to backend
      this.userService.getUserByUsername(localStorage.getItem('currentUser')).subscribe(res => {
             deck.name = this.deckEditForm.controls.title.value;
                 this.flashcardService.editDeck(deck, localStorage.getItem('currentUser')).subscribe(
                      () => {
                             this.loadAllDecks();
                             location.reload();
                             },
                             error => {
                               this.defaultErrorHandling(error);
                             }
                           );
       });
       //this.deckForm.reset({'title':''});
  }

  private defaultErrorHandling(error: any) {
      console.log(error);
      this.error = true;
      this.errorMessage = '';
      this.errorMessage = error.error.message;
    }

}