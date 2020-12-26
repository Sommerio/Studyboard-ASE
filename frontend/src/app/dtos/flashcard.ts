import {Deck} from './deck';

export class Flashcard {
  constructor(
    public id: number,
    public question: string,
    public answer: string) {
  }
}
