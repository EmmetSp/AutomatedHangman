// Project: AutomatedHangman
// Authors: Emmet Spencer & David Nessel
// Period: 2

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

   public static void main(String[] args){
       Scanner scanner = new Scanner(System.in);
       System.out.println("A word for computer to guess: ");
       String next = scanner.next();

       AutomatedHangman hangman = new AutomatedHangman(next, dictionaryPath);


       while(!hangman.foundWord){
           hangman.computerTurn();
       }
   }

   static String dictionaryPath = "C:\\Users\\ITPathways\\IdeaProjects\\Hangman\\src\\main\\resources\\dictionary.txt";


}

/**
* The abstract class Hangman.
*/
abstract class Hangman {


   private String[] wordSplit;
   String[] guessedWordSplit;
   String word;
   List<String> guessedLetters = new ArrayList<>();
   List<String> incorrectLetters = new ArrayList<>();
   int totalGuesses;
   boolean foundWord;
   double startTime;

   public Hangman(String word){
       String s = word.toLowerCase();
       this.startTime = System.currentTimeMillis();
       // creates all values for hangman game to be used later.
       this.word = s;
       this.wordSplit = s.split("");
       this.guessedWordSplit = s.split("");

       for (int i = 0; i < guessedWordSplit.length; i++){
           guessedWordSplit[i] = " ";
       }


   }

   public boolean isLetterInWord(String input){
       if (input.length() != 1) return false;
       if (guessedLetters.contains(input.toLowerCase())) return false;

       if (word.contains(input)){
           return true;
       } else {
           return false;
       }
   }

   public String guessLetter(String letter){

       if (isLetterInWord(letter)){
           for (int i = 0; i < wordSplit.length; i++){
               String s = wordSplit[i];
               if (s.equalsIgnoreCase(letter)){
                   guessedWordSplit[i] = letter.toLowerCase();
               }
           }

           System.out.println("Guessed letter '" + letter + "'!");
           System.out.println("Guessed Word Progress: " + Arrays.stream(guessedWordSplit).toList());
           this.guessedLetters.add(letter);
           this.totalGuesses += 1;
       }else{
           System.out.println("Guessed letter '" + letter + "' was not in the word.");

           this.guessedLetters.add(letter);
           this.incorrectLetters.add(letter);
           this.totalGuesses += 1;
       }
       return letter;
   }

   public void endGame(){
       this.foundWord = true;
   }

   public String[] getWordSplit(){
       return wordSplit;
   }

   public String getWord() {
       return this.word;
   }

   public int getTotalCharactersGuessed(){
       int total = 0;
       for (String s : this.guessedWordSplit) {
           if (!s.equals(" ")){
               total += 1;
           }
       }
       return total;
   }

}

/**
* The AutomatedHangman class. Extends Hangman.
*/
class AutomatedHangman extends Hangman {

   private Collection<String> wordDictionary = new ArrayList<>();
   private String nextLetter = "";

   public AutomatedHangman(String word, String dictionaryPath) {
       super(word);
       //<editor-fold desc="loadDictionary()">
       loadDictionary(dictionaryPath);
       //</editor-fold>

   }

   /**
    * loads dictionary from dictionary.txt
    *
    * @param filePath
    */
   private void loadDictionary(String filePath){
       File file = new File(filePath);

       try{
           BufferedReader reader = new BufferedReader(new FileReader(file)) ;
           wordDictionary = reader
                   .lines()
                   .filter(s -> s.length() == getWord().length())
                   .collect(Collectors.toList());
       }catch (FileNotFoundException e){
           System.out.println("File does not exist at filepath " + filePath);
       }catch (Exception e){
           e.printStackTrace();
       }

   }

   /**
    * @return next letter to guess.
    */
   public String calculateNextLetterToGuess(){
       Map<String, Integer> common = new HashMap<>();

       for (String word : wordDictionary) {
           String s = word.toLowerCase();
           List<String> countedLetters = new ArrayList<>();
           for (String s1 : s.split("")){
               if (countedLetters.contains(s1)){
                   continue;
               }
               if (!common.containsKey(s1)){
                   common.put(s1, 1);
                   continue;
               }
               common.put(s1, common.get(s1) + 1);
               countedLetters.add(s1);
           }
       }

       String highestPercentLetter = "";
       int currentHighestPercent = 0;

       for (Map.Entry<String, Integer> entry : common.entrySet()){
           if (guessedLetters.contains(entry.getKey())) continue;

           if (entry.getValue() > currentHighestPercent){
               highestPercentLetter = entry.getKey();
               currentHighestPercent = entry.getValue();
           }
       }

       return highestPercentLetter;
   }

   /**
    * Runs the computer's turn.
    */
   public void computerTurn() {

       if (foundWord == true){
           System.out.println("Game has ended! Total Guesses: " + totalGuesses);
           return;
       }

       if (totalGuesses == 10){
           this.foundWord = true;
       }

       double startTime = System.currentTimeMillis();

       if (getTotalCharactersGuessed() < 1) { // on the first turn, we will guess the most common letter in the alphabet.
           guessLetter(calculateNextLetterToGuess());
       }else{
           guessLetter(nextLetter);
       }

       wordDictionary = wordDictionary.stream()
               .filter(w -> similarLetters(w) >= getTotalCharactersGuessed())
               .filter(w -> correctlyPositioned(w)) // Filters based on letters being correctly placed in the dictionary, this will remove the most amount of words by far.
               .collect(Collectors.toList());

       if (wordDictionary.size() == 1){
           if (wordDictionary.contains(word.toLowerCase())) {
               this.foundWord = true;
               System.out.println("AutomatedHangman found the word in " + totalGuesses + " guesses!");
           }
       }

       System.out.println("Total turn took " + (System.currentTimeMillis() - startTime) + "ms!");

       nextLetter = calculateNextLetterToGuess();
   }

   /**
    * returns the amount of similar letters that a word holds.
    *
    * @param word
    * @return
    */
   private int similarLetters(String word){
       String[] splitWord = word.split("");
       int total = 0;
       for (int i = 0; i < splitWord.length; i++){
           String s = splitWord[i];
           if (guessedWordSplit[i].equals(s)){
               total += 1;
           }
       }
       return total;
   }

   /**
    * returns whether a word from dictionary holds same letter placements as word to guess.
    *
    * @param word
    * @return
    */
   private boolean correctlyPositioned(String word){
       String[] splitWord = word.split("");
       List<String> guessedWordList = Arrays.stream(this.guessedWordSplit).toList();
       for (int i = 0; i < splitWord.length; i++){
           String s = splitWord[i];

           boolean isGuessedLetter = guessedLetters.contains(s);

           if (!isGuessedLetter){ // if the letter is already guessed, continue with program.
               continue;
           }

           if (guessedWordList.get(i).equals(splitWord[i])){
               continue;
           }

           if (!guessedWordList.get(i).equals(splitWord[i])){
               return false;
           }
       }
       return true;
   }
}

