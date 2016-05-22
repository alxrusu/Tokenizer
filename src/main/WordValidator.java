package main;

/**
 * Created by tanasa.paul on 5/23/2016.
 */
public class WordValidator{

        private String word;

        public WordValidator(String word){
            this.word=word;
        }

        public boolean validate(){
            if(word.length()<3) return false;
            String modifiedWord=this.removeRubbish();
            if(hasDigits(modifiedWord)) return false;
            return true;
        }

        public String removeRubbish(){
            return word.toLowerCase().trim().replaceAll("\\p{P}", "");
        }

        public boolean hasDigits(String word){
            return word.matches(".*\\d+.*");
        }
}
