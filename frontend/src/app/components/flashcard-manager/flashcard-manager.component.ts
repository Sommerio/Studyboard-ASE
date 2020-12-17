import {Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {FlashcardService} from '../../services/flashcard.service';
import {UserService} from '../../services/user.service';
import {Deck} from '../../dtos/deck';
import {Flashcard} from '../../dtos/flashcard';
import {User} from '../../dtos/user';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-flashcard-manager',
  templateUrl: './flashcard-manager.component.html',
  styleUrls: ['./flashcard-manager.component.scss']
})

export class FlashcardManagerComponent implements OnInit {

  deckForm: FormGroup;
  deckEditForm: FormGroup;
  flashcardForm: FormGroup;
  flashcardEditForm: FormGroup;
  revisionSizeForm: FormGroup;
  flashcardRateForm: FormGroup;
  error: boolean = false;
  errorMessage: string = '';
  viewAll: boolean = true;
  showAnswer: boolean = false;
  selectedDeck: Deck;
  selectedDeckId: number;
  selectedFlashcard: Flashcard;
  showFlashcardId: number;
  chooseSize: boolean = true;
  revisionCounter: number = 0;
  currentlyRevisedCard: Flashcard;
  sizeError: boolean = false;
  private decks: Deck[];
  private flashcards: Flashcard[];
  revisionFlashcards: Flashcard[];
  deleteFlash: boolean = false;
  confidenceError: boolean = false;


  constructor(private formBuilder: FormBuilder, private flashcardService: FlashcardService, private userService: UserService, private snackBar: MatSnackBar) {
    this.deckForm = this.formBuilder.group({
      title: ['']
    });
    this.deckEditForm = this.formBuilder.group({
      title: ['']
    });
    this.flashcardForm = this.formBuilder.group({
          question: [''],
          answer: ['']
    });
    this.flashcardEditForm = this.formBuilder.group({
         question: [''],
         answer: ['']
    });
    this.revisionSizeForm = this.formBuilder.group({
            revisionSize: [0]
    });
    this.flashcardRateForm = this.formBuilder.group({
      confidenceLevel: [0]
    });
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
     this.deckForm.reset();
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
           this.flashcardService.createDeck(deck).subscribe(
                () => {
                       this.openSnackbar('You successfully created a deck with the title ' + deck.name + `!`, 'success-snackbar');
                       this.loadAllDecks();
                       },
                       error => {
                         this.error = true;
                         this.errorMessage = 'Could not create a deck!';
                         this.openSnackbar(this.errorMessage, 'warning-snackbar');
                       }
                     );
    });
   //this.deckForm.reset({'title':''});
  }

  saveEdits(deck: Deck) {
      //send edits to backend
      this.userService.getUserByUsername(localStorage.getItem('currentUser')).subscribe(res => {
             deck.name = this.deckEditForm.controls.title.value;
                 this.flashcardService.editDeck(deck).subscribe(
                      (editedDeck : Deck) => {
                             this.openSnackbar('You successfully edited a deck!', 'success-snackbar');
                             this.selectedDeck = editedDeck;
                             },
                             error => {
                               this.error = true;
                               this.errorMessage = 'Could not edit the deck!';
                               this.openSnackbar(this.errorMessage, 'warning-snackbar');
                             }
                           );
       });
       //this.deckForm.reset({'title':''});
  }


  /**
  * Get a list of all flashcards belonging to a deck from backend
  */
  loadFlashcards(deck: Deck) {
    this.selectedDeck = deck;
    this.flashcardService.getFlashcards(deck.id).subscribe(
        (flashcards : Flashcard[]) => {
                     this.flashcards = flashcards;
                     this.chooseSize = true;
                     },
                     error => {
                           this.defaultErrorHandling(error);
                     }
                 );
     //this.deckEditForm.reset();
     this.deckEditForm.patchValue({
             title: deck.name
     })
     this.flashcardForm.reset();
  }

  getFlashcards() {
    return this.flashcards;
  }

  createFlashcard() {
    this.flashcardService.getDeckById(this.selectedDeckId).subscribe(res => {
       console.log(res);
       const flashcard = new Flashcard(0, this.flashcardForm.controls.question.value, this.flashcardForm.controls.answer.value, 0, res);
       this.flashcardService.createFlashcard(flashcard, this.selectedDeckId).subscribe(
                       () => {
                              this.openSnackbar('You successfully created a flashcard with the question ' + flashcard.question + `!`, 'success-snackbar');
                              this.loadFlashcards(res);
                              this.loadAllDecks();
                              },
                              error => {
                                this.error = true;
                                this.errorMessage = 'Could not create a flashcard!';
                                this.openSnackbar(this.errorMessage, 'warning-snackbar');

                              }
                            );
      });
  }

  saveFlashcardEdits(flashcard: Flashcard) {
        //send edits to backend
        console.log(flashcard);
         this.flashcardService.getDeckById(this.selectedDeck.id).subscribe(res => {
                let question = this.flashcardEditForm.controls.question.value;
                let answer = this.flashcardEditForm.controls.answer.value;
               if(question != null && question != "") {
                flashcard.question = question;
               }
               if(answer != null && answer != "") {
                flashcard.answer = answer;
              }
              this.flashcardService.editFlashcard(flashcard, this.selectedDeck.id).subscribe(
                    () => {
                           this.openSnackbar('You successfully edited a flashcard!', 'success-snackbar');
                           this.loadFlashcards(this.selectedDeck);
                           },
                           error => {
                             this.error = true;
                             this.errorMessage = 'Could not edit the flashcard!';
                             this.openSnackbar(this.errorMessage, 'warning-snackbar');
                           }
                         );
              });
    }


    revise() {
      this.sizeError = false;
      let size = this.revisionSizeForm.controls.revisionSize.value;
      if(size < 0 || size > this.selectedDeck.size) {
          this.sizeError = true;
         //this.openSnackbar('Number of chosen cards not corresponding to the size of the deck', 'warning-snackbar');
      } else {
            this.chooseSize = false;
            this.revisionCounter = 0;
            this.flashcardService.revise(size, this.selectedDeck.id).subscribe(
                (flashcards : Flashcard[]) => {
                             this.revisionFlashcards = flashcards;
                             this.getRevisionFlashcard();
                             },
                             error => {
                                   this.defaultErrorHandling(error);
                             }
                         );
      }

    }

   getRevisionFlashcard() {
     this.showAnswer = false;
     this.currentlyRevisedCard = this.revisionFlashcards[this.revisionCounter];
     if(this.revisionCounter < this.revisionFlashcards.length) {
        this.revisionCounter=this.revisionCounter +1;
     }
   }


  /**
   * Sends a request to delete a specific deck.
   */
  deleteDeck(id: number) {
    this.flashcardService.deleteDeck(id).subscribe(
      () => {
        this.openSnackbar('You successfully deleted the deck!', 'success-snackbar');
        this.loadAllDecks();
        location.reload();
      },
      error => {
        this.defaultErrorHandling(error);
      });
  }

  /**
   * Sends a request to delete a specific flashcard.
   */
  deleteFlashcard(flashcardId: number, deckId: number) {
    this.flashcardService.deleteFlashcard(flashcardId, deckId).subscribe(
      () => {
        this.openSnackbar('You successfully deleted the flashcard!', 'success-snackbar');
        this.loadAllDecks();
        this.loadFlashcards(this.selectedDeck);
      },
      error => {
        this.defaultErrorHandling(error);
      });
  }

  rateFlashcard(flashcard: Flashcard) {
    console.log(flashcard);
    this.flashcardService.getDeckById(this.selectedDeck.id).subscribe(res => {
      let confidence = this.flashcardRateForm.controls.confidenceLevel.value;
      if (confidence != null) {
        flashcard.confidenceLevel = confidence;
      }
      this.flashcardService.editFlashcard(flashcard, this.selectedDeck.id).subscribe(
        () => {
          this.openSnackbar('You successfully rated a flashcard!', 'success-snackbar');
          this.loadFlashcards(this.selectedDeck);
        },
        error => {
          this.confidenceError = true;
          this.error = true;
          this.errorMessage = 'Could not rate the flashcard! Please choose the value between 1 and 5.';
          this.openSnackbar(this.errorMessage, 'warning-snackbar');
        }
      );
    });
  }


  deckClicked(select : number) {
    console.log(select);
    this.selectedDeckId = select;
  }

  flashcardClicked(select : Flashcard, del: boolean) {
     console.log(select);
     this.selectedFlashcard = select;
     this.showFlashcardId = select.id;
     this.deleteFlash = del;
     console.log(this.showFlashcardId);
     this.flashcardEditForm.patchValue({
        question: select.question,
        answer: select.answer
     })
   }

  openSnackbar(message: string, type: string) {
     this.snackBar.open(message, 'close', {
       duration: 4000,
       panelClass: [type]
     });
   }

  private defaultErrorHandling(error: any) {
      console.log(error);
      this.error = true;
      this.errorMessage = '';
      this.errorMessage = error.error.message;
    }

}
