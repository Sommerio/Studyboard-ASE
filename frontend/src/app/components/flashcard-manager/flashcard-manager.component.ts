import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {FlashcardService} from '../../services/flashcard.service';
import {UserService} from '../../services/user.service';
import {SpaceService} from '../../services/space.service';
import {FileUploadService} from '../../services/file-upload.service';
import {Deck} from '../../dtos/deck';
import {Flashcard} from '../../dtos/flashcard';
import {Space} from '../../dtos/space';
import {Document} from '../../dtos/document';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DocumentDialogComponent} from '../document-dialog/document-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {DomSanitizer} from '@angular/platform-browser';


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
  error: boolean = false;
  errorMessage: string = '';
  viewAll: boolean = true;
  showAnswer: boolean = false;
  selectedDeck: Deck;
  selectedDecksIds: number[];
  selectedFlashcard: Flashcard;
  showFlashcardId: number;
  chooseSize: boolean = true;
  revisionCounter: number = 0;
  currentlyRevisedCard: Flashcard;
  sizeError: boolean = false;
  chosenOption: number;
  private decks: Deck[];
  private filteredDecks: Deck[];
  private flashcards: Flashcard[];
  selectedDecks: number[];
  private unassignedFilteredDecks: Deck[] = [];
  private spaces: Space[] = [];
  documents: Map<number, Document[]> = new Map<number, Document[]>();
  filteredDocuments: Map<number, Document[]> = new Map<number, Document[]>();
  selectedDocuments: number[];
  existingRefs: number[];
  revisionFlashcards: Flashcard[];
  dueDateFlashcards: Flashcard[];
  deleteFlash: boolean = false;
  editRef: boolean = false;
  confidenceError: boolean = false;
  optionError: boolean = false;
  currentRate = 0;
  deckNameSearch: string = '';
  deckNameSearchModal: string;
  documentNameSearch: string;
  documentNameSearchEdit: string;
  deckNameMoveModal: string;
  deckFavorite = new FormControl(false);
  deckSelectControl = new FormControl();
  documentSelectControl = new FormControl();
  documentSelectControlEdit = new FormControl();
  deckMoveControl = new FormControl();
  selectable = true;
  removable = true;
  showInfo = false;

  @ViewChild('sortOption') sortOption;

  constructor(private formBuilder: FormBuilder, private flashcardService: FlashcardService,
              private userService: UserService, private spaceService: SpaceService,
              private fileUploadService: FileUploadService, private snackBar: MatSnackBar,
              private sanitizer: DomSanitizer, private dialog: MatDialog) {
    this.deckForm = this.formBuilder.group({
      title: ['', [
        Validators.required,
        Validators.minLength(1)
      ]],
      favorite: this.deckFavorite
    });
    this.deckEditForm = this.formBuilder.group({
      title: ['', [
        Validators.required,
        Validators.minLength(1)
      ]],
      favorite: this.deckFavorite
    });
    this.flashcardForm = this.formBuilder.group({
      question: ['', [
        Validators.required,
        Validators.minLength(1)
      ]],
      answer: ['', [
        Validators.required,
        Validators.minLength(1)
      ]],
      selectRefs: [[]]
    });
    this.flashcardEditForm = this.formBuilder.group({
      question: ['', [
        Validators.required,
        Validators.minLength(1)
      ]],
      answer: ['', [
        Validators.required,
        Validators.minLength(1)
      ]]
    });
    this.revisionSizeForm = this.formBuilder.group({
      revisionSize: [1]
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
      (decksList: Deck[]) => {
        this.decks = decksList;
        this.filteredDecks = decksList;
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
    this.resetDeckForm();
    this.resetFlashcardForm();
  }

  /**
   * @return all decks belonging to the logged-in user
   */
  getDecks() {

    if (this.sortOption != null) {
      switch (this.sortOption.value) {
        case 'name-asc' : {
          return this.decks.sort((d1, d2) => d1.name.localeCompare(d2.name));
        }
        case 'name-desc' : {
          return this.decks.sort((d1, d2) => d2.name.localeCompare(d1.name));
        }
        case 'date-asc': {
          return this.decks.sort((d1, d2) => Date.parse(d1.creationDate) - Date.parse(d2.creationDate));
        }
        case 'date-desc': {
          return this.decks.sort((d2, d1) => Date.parse(d1.creationDate) - Date.parse(d2.creationDate));
        }
        case 'default': {
          return this.decks;
        }
        default : {
          return this.decks;
        }
      }
    }
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
      const deck = new Deck(0, this.deckForm.controls.title.value, 0, dateString, dateString, this.deckForm.controls.favorite.value, res);
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
  }

  /**
   * Save changes to deck dto and sends an edition request.
   */
  saveEdits(deck: Deck) {
    this.userService.getUserByUsername(localStorage.getItem('currentUser')).subscribe(res => {
      deck.name = this.deckEditForm.controls.title.value;
      deck.favorite = this.deckEditForm.controls.favorite.value;
      this.flashcardService.editDeck(deck).subscribe(
        () => {
          this.openSnackbar('You successfully edited a deck!', 'success-snackbar');
        },
        error => {
          this.error = true;
          this.errorMessage = 'Could not edit the deck!';
          this.openSnackbar(this.errorMessage, 'warning-snackbar');
        }
      );
    });
  }


  /**
   * Get a list of all flashcards belonging to a deck from backend
   */
  loadDeckDetails(deck: Deck) {
    this.selectedDeck = deck;
    this.flashcardService.getDeckById(deck.id).subscribe(
      (deckRefreshed: Deck) => {
        this.selectedDeck = deckRefreshed;
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
    this.selectedDecksIds = [deck.id];
    this.flashcardService.getFlashcards(deck.id).subscribe(
      (flashcards: Flashcard[]) => {
        this.flashcards = flashcards;
        this.chooseSize = true;
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
    // get cards for revision in advance to show how many are scheduled for the day
    this.flashcardService.revise(1, deck.id, 1, false).subscribe(
      (flashcards: Flashcard[]) => {
        this.dueDateFlashcards = flashcards;
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
    this.deckEditForm.patchValue({
      title: deck.name,
      favorite: deck.favorite
    });
    // this.loadAllSpaces();
  }

  /**
   * @return all flashcards belonging to the logged-in user
   */
  getFlashcards() {
    return this.flashcards;
  }

  /**
   * Reset all values when creating a new flashcard
   * Load values which are needed
   */
  prepareFlashcardCreation() {
    this.loadAllSpaces();
    if (this.selectedDeck === undefined) {
      this.selectedDecksIds = undefined;
      this.selectedDecks = [];
    } else {
      this.selectedDecks = [this.selectedDeck.id];
    }
    this.selectedDocuments = undefined;
    this.searchDecksInModal('');
    this.resetFlashcardForm();
  }

  /**
   * Get all spaces documents can be referenced from
   */
  loadAllSpaces() {
    this.spaceService.getSpaces(localStorage.getItem('currentUser')).subscribe(
      (spaceList: Space[]) => {
        this.spaces = spaceList;
        for (let i = 0; i < this.spaces.length; i++) {
          this.loadDocuments(this.spaces[i]);
        }
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
  }

  getSpaces() {
    return this.spaces;
  }

  /**
   * Load all documents from a space as possible references
   */
  loadDocuments(space: Space) {
    this.spaceService.getAllDocuments(localStorage.getItem('currentUser'), space.id).subscribe(
      (documentList: Document[]) => {
        this.documents.set(space.id, documentList);
        this.filteredDocuments.set(space.id, documentList);
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
  }

  /**
   * Builds a flashcard dto and sends a creation request.
   */
  createFlashcard() {
    console.log(this.selectedDecks);
    const documentReferences = this.getReferences();
    const deckDTOs = this.getChosenDecks();
    const flashcard = new Flashcard(0, this.flashcardForm.controls.question.value, this.flashcardForm.controls.answer.value,
      0, deckDTOs, documentReferences);
    this.flashcardService.createFlashcard(flashcard).subscribe(
      (created: Flashcard) => {
        console.log(created);
        this.openSnackbar('You successfully created a flashcard with the question ' + flashcard.question + `!`, 'success-snackbar');
        if (this.selectedDeck !== undefined) {
          this.loadDeckDetails(this.selectedDeck);
        }
        this.loadAllDecks();
      },
      error => {
        this.error = true;
        this.errorMessage = 'Could not create a flashcard!';
        this.openSnackbar(this.errorMessage, 'warning-snackbar');

      });
  }

  /**
   * Build a Deck object array, from options chosen in the dropdown menu
   */
  getChosenDecks() {
    if (this.selectedDecks !== undefined) {
      return this.decks.filter((el) => this.selectedDecks.find(sel => (sel === el.id)));
    } else {
      return [];
    }
  }

  /**
   * Build a Document object array, from options chosen in the dropdown menu
   */
  getReferences() {
    if (this.selectedDocuments !== undefined) {
      let values = [];
      for (let i = 0; i < this.spaces.length; i++) {
        values = values.concat(this.documents.get(this.spaces[i].id).filter((el) => this.selectedDocuments.find(sel => (sel === el.id))));
      }
      return values;
    } else {
      return [];
    }
  }

  /**
   * Save changes to flashcard dto and sends an edition request.
   */
  saveFlashcardEdits(flashcard: Flashcard) {
    const question = this.flashcardEditForm.controls.question.value;
    const answer = this.flashcardEditForm.controls.answer.value;
    if (question != null && question !== '') {
      flashcard.question = question;
    }
    if (answer != null && answer !== '') {
      flashcard.answer = answer;
    }
    this.flashcardService.editFlashcard(flashcard).subscribe(
      () => {
        this.openSnackbar('You successfully edited a flashcard!', 'success-snackbar');
        this.loadDeckDetails(this.selectedDeck);
      },
      error => {
        this.error = true;
        this.errorMessage = 'Could not edit the flashcard!';
        this.openSnackbar(this.errorMessage, 'warning-snackbar');
      });
  }


  /**
   * Sends a revision request based on chosen revision size.
   */
  revise() {
    console.log('Option: ' + this.chosenOption);
    this.sizeError = false;
    this.optionError = false;
    const size = this.revisionSizeForm.controls.revisionSize.value;
    if (size <= 0 || size > this.selectedDeck.size) {
      this.sizeError = true;
    } else if (this.chosenOption === undefined) {
      this.optionError = true;
    } else {
      this.chooseSize = false;
      this.revisionCounter = 0;
      this.flashcardService.revise(size, this.selectedDeck.id, this.chosenOption, true).subscribe(
        (flashcards: Flashcard[]) => {
          this.revisionFlashcards = flashcards;
          this.getRevisionFlashcard();
        },
        error => {
          this.defaultErrorHandling(error);
        }
      );

    }
  }

  /**
   * @return next flashcard for the revision
   * In case the confidence level was unchanged - sends a request to rate it again with the same confidence level
   */
  getRevisionFlashcard() {
    this.showAnswer = false;
    this.currentlyRevisedCard = this.revisionFlashcards[this.revisionCounter];
    this.currentRate = this.currentlyRevisedCard.confidenceLevel;
    if (this.revisionCounter < this.revisionFlashcards.length) {
      this.revisionCounter = this.revisionCounter + 1;
    }
    if (this.currentlyRevisedCard.confidenceLevel > 0) {
      this.flashcardService.rateFlashcard(this.currentlyRevisedCard).subscribe(
        () => {
          console.log('Rating with the old confidence level again.');
        },
        error => {
          this.confidenceError = true;
          this.error = true;
          this.errorMessage = 'Could not rate the flashcard! Please choose the value between 1 and 5.';
          this.openSnackbar(this.errorMessage, 'warning-snackbar');
        });
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
        this.viewAll = true;
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
        this.openSnackbar('You successfully removed the flashcard!', 'success-snackbar');
        this.loadAllDecks();
        this.loadDeckDetails(this.selectedDeck);
      },
      error => {
        this.defaultErrorHandling(error);
      });
  }

  /**
   * Sends a request to rate a specific flashcard.
   */
  rateFlashcard(flashcard: Flashcard, rate: number) {
    if (rate != null) {
      flashcard.confidenceLevel = rate;
    }
    if (this.currentRate < 1 || this.currentRate > 5) {
      this.error = true;
      this.errorMessage = 'Could not rate the flashcard! Please choose the value between 1 and 5.';
      this.openSnackbar(this.errorMessage, 'warning-snackbar');
    } else {
      this.flashcardService.rateFlashcard(flashcard).subscribe(
        () => {
          this.openSnackbar('You successfully rated a flashcard!', 'success-snackbar');
          this.loadDeckDetails(this.selectedDeck);
        },
        error => {
          this.confidenceError = true;
          this.error = true;
          this.errorMessage = 'Could not rate the flashcard! Please choose the value between 1 and 5.';
          this.openSnackbar(this.errorMessage, 'warning-snackbar');
        });
    }
  }

  /**
   * Assigns a flashcard to new decks
   */
  copyFlashcard(flashcard: Flashcard) {
    const updatedDecks = this.getChosenDecks();
    updatedDecks.push(this.selectedDeck);
    flashcard.deckDTOs = updatedDecks;
    this.flashcardService.editFlashcard(flashcard).subscribe(
      () => {
        this.openSnackbar('Flashcard successfully copied', 'success-snackbar');
        this.loadDeckDetails(this.selectedDeck);
      },
      error => {
        this.error = true;
        this.errorMessage = 'Could not copy the flashcard!';
        this.openSnackbar(this.errorMessage, 'warning-snackbar');
      });
  }

  /**
   * Assigns a flashcard to new decks and removes the assignment from the current deck
   */
  moveFlashcard(flashcard: Flashcard) {
    flashcard.deckDTOs = this.getChosenDecks();
    this.flashcardService.editFlashcard(flashcard).subscribe(
      () => {
        this.openSnackbar('Flashcard successfully copied', 'success-snackbar');
        this.loadDeckDetails(this.selectedDeck);
      },
      error => {
        this.error = true;
        this.errorMessage = 'Could not copy the flashcard!';
        this.openSnackbar(this.errorMessage, 'warning-snackbar');
      });
  }

  /**
   * Sends a request to rate a specific flashcard while in revision mode.
   */
  rateFlashcardInRevision(flashcard: Flashcard, rate: number) {
    if (rate != null) {
      flashcard.confidenceLevel = rate;
    }
    if (rate < 1 || rate > 5) {
      this.error = true;
      this.errorMessage = 'Could not rate the flashcard! Please choose the value between 1 and 5.';
      this.openSnackbar(this.errorMessage, 'warning-snackbar');
    } else {
      this.flashcardService.rateFlashcard(flashcard).subscribe(
        () => {
          this.openSnackbar('You successfully rated a flashcard!', 'success-snackbar');
        },
        error => {
          this.confidenceError = true;
          this.error = true;
          this.errorMessage = 'Could not rate the flashcard! Please choose the value between 1 and 5.';
          this.openSnackbar(this.errorMessage, 'warning-snackbar');
        });
    }
    if (this.revisionCounter < this.revisionFlashcards.length) {
       this.getRevisionFlashcard();
     }
  }

  /**
   * Loads and opens the document that is referenced
   */
  loadFile(document: Document) {
    this.fileUploadService.getFile(document.spaceDTO, document.name).subscribe(
      (res) => {
        let fileObject: Blob;
        let blobUrl: any;
        if (document.name.includes('.mp3') || document.name.includes('.mp4')) {
          fileObject = res;
          blobUrl = URL.createObjectURL(fileObject);
        } else if (document.name.includes('.pdf')) {
          fileObject = new Blob([res], {type: 'application/pdf'});
          blobUrl = URL.createObjectURL(fileObject);
        } else {
          fileObject = res;
          blobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(URL.createObjectURL(fileObject));
        }
        this.dialog.open(DocumentDialogComponent, {
          data: {
            currentDocument: document,
            blobUrl: blobUrl
          }
        });
      },
      error => {
        this.defaultErrorHandling(error);
      }
    );
  }

  /**
   * Creates an array from items chosen in the dropdown (it's ids)
   */
  updateDeckList(select: number) {
    console.log('deck: ' + select);
    if (this.selectedDecks !== undefined) {
      const index = this.selectedDecks.indexOf(select);
      if (index > -1) {
        this.selectedDecks.splice(index, 1);
      } else {
        this.selectedDecks.push(select);
      }
    } else {
      this.selectedDecks = [select];
    }
    this.searchDecksInModal('');
  }

  /**
   * Creates an array from items chosen in the dropdown (it's ids)
   */
  updateReferenceList(select: number) {
    console.log('document: ' + select);
    if (this.selectedDocuments !== undefined) {
      const index = this.selectedDocuments.indexOf(select);
      if (index > -1) {
        this.selectedDocuments.splice(index, 1);
      } else {
        this.selectedDocuments.push(select);
      }
    } else {
      this.selectedDocuments = [select];
    }
    this.searchDocumentsInModal('');
  }

  /**
   * Prepare values need for editing references
   */
  prepareEditRef() {
    this.editRef = true;
    this.existingRefs = this.selectedFlashcard.documentReferences.map(({id}) => id);
    this.selectedDocuments = this.existingRefs;
    this.searchDocumentsInModal('');
  }

  /**
   * Sends new references of a flashcard to backend
   */
  editReferences(flashcard: Flashcard) {
    flashcard.documentReferences = this.getReferences();
    this.flashcardService.editFlashcard(flashcard).subscribe(
      (updatedFlashcard: Flashcard) => {
        console.log(updatedFlashcard);
        this.openSnackbar('You successfully edited flashcard references!', 'success-snackbar');
        this.selectedFlashcard = updatedFlashcard;
        this.selectedDocuments = this.selectedFlashcard.documentReferences.map(({id}) => id);
      },
      error => {
        this.error = true;
        this.errorMessage = 'Could not edit the flashcard references!';
        this.openSnackbar(this.errorMessage, 'warning-snackbar');
      });
    this.editRef = false;
  }

  /**
   * When a flashcard-related function was chosen
   */
  flashcardClicked(select: Flashcard, del: boolean) {
    console.log(this.getSpaces());
    console.log(select);
    this.selectedFlashcard = select;
    this.showFlashcardId = select.id;
    this.deleteFlash = del;
    this.editRef = false;
    this.currentRate = this.selectedFlashcard.confidenceLevel;
    this.flashcardEditForm.patchValue({
      question: select.question,
      answer: select.answer
    });
    this.selectedDecks = undefined;
    this.loadAllSpaces();
  }

  /**
   * Get all decks a flashcard doesn't belong to
   */

  /**
   * Sends a request to filter the deck results based on a sign/sign group they contain
   */
  searchDecksByName() {
    this.decks = this.flashcardService.getDecksByName(localStorage.getItem('currentUser'), this.deckNameSearch);
  }

  filterSearchContent(searchContent: string) {
    return searchContent.replace('/', '').replace(';', '');
  }

  /**
   * Sends a request to filter the deck results based on a sign/sign group they contain
   * Filters out already selected options
   */
  searchDecksInModal(inputVal: string) {
    console.log(inputVal);
    let decksList = this.flashcardService.getDecksByName(localStorage.getItem('currentUser'), inputVal);

    const selected = this.getChosenDecks();
    this.filteredDecks = decksList.filter((el) => !selected.find(rm => (rm.id === el.id)));
    if(this.selectedFlashcard != undefined) {
      this.flashcardService.getFlashcardAssignments(this.selectedFlashcard.id).subscribe(
          (assignedDecks: number[]) => {
              console.log(assignedDecks);
              this.unassignedFilteredDecks =  decksList.filter((el) => !assignedDecks.find(rm => (rm === el.id)));
              this.unassignedFilteredDecks =  this.unassignedFilteredDecks.filter((el) => !selected.find(rm => (rm.id === el.id)));
          },
          error => {
            this.defaultErrorHandling(error);
          }
        );
      }

  }

  getFilteredDecks() {
    return this.filteredDecks;
  }

  /**
   * Gets all decks a flashcard doesn't belongs to
   */
  getUnassignedFilteredDecks() {
    return this.unassignedFilteredDecks;
  }

  /**
   * Calls the filter/search function for documents of all spaces
   */
  searchDocumentsInModal(inputVal: string) {
    const allSpaces = this.getSpaces();
    for (let i = 0; i < allSpaces.length; i++) {
      this.searchDocumentsOfSpace(inputVal, allSpaces[i].id);
    }
  }

  /**
   * Sends a request to filter the document results based on a sign/sign group they contain
   * Filters out already selected options
   */
  searchDocumentsOfSpace(inputVal: string, spaceId: number) {
    let documentList = this.spaceService.getDocumentsByName(spaceId, inputVal);
    const selected = this.getReferences();
    documentList = documentList.filter((el) => !selected.find(rm => (rm.id === el.id)));
    this.filteredDocuments.set(spaceId, documentList);
  }

  isEmptyDecks() {
    return this.decks?.length === 0 && this.deckNameSearch.length === 0;
  }

  isEmptySpecificDecks() {
        return this.decks?.length === 0 && this.deckNameSearch.length !== 0;
  }

  isEmptyFlashcards() {
    return this.flashcards?.length === 0;
  }

  isSpaceNotEmpty(spaceId: number) {
    if (this.filteredDocuments.get(spaceId) !== undefined) {
      return this.filteredDocuments.get(spaceId).length !== 0;
    }
  }

  resetDeckForm() {
    this.deckForm.reset();
  }

  resetFlashcardForm() {
    this.flashcardForm.reset();
    if (this.selectedDeck !== undefined) {
      this.selectedDecksIds = [this.selectedDeck.id];
    }
  }

  resetRevisionSizeForm() {
    this.revisionSizeForm.patchValue({
      revisionSize: 1
    });
    this.chosenOption = undefined;
    this.optionError = false;
  }

  checkSelection(compareId: number) {
    if (this.selectedDeck === undefined) {
      return false;
    } else if (this.selectedDeck.id === compareId) {
      console.log(compareId);
      return true;
    }
    return false;
  }

  /**
   * Set the values to go back to seeing all decks
   */
  backToAll() {
    this.viewAll = true;
    this.resetDeckForm();
    this.loadAllDecks();
    this.selectedDeck = undefined;
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
