package search;

import java.io.*;
import java.util.*;

class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
			throws FileNotFoundException {
		// COMPLETE THIS METHOD

		HashMap<String,Occurrence> retWords = new HashMap<String,Occurrence>();
		String currWord = null, word = null;
		Occurrence foundWord = null;
						
		Scanner sc = new Scanner(new File(docFile));
		
		while ( sc.hasNext() ) {
			currWord = sc.next();
			word = getKeyWord(currWord);

			if( word != null ) {	

				if( retWords.containsKey(word) ){
					foundWord = retWords.get(word);
					foundWord.frequency++;
				} else {
					Occurrence newOcc = new Occurrence(docFile, 1);
					retWords.put(word, newOcc);
				}
			}
		}

		sc.close();

		return retWords;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		// COMPLETE THIS METHOD
		
		ArrayList<Occurrence> tempList = null;
		
		for( String key: kws.keySet() ) {
			
			Occurrence currOcc = kws.get(key);
			
//			System.out.println(currOcc);
			
			if( keywordsIndex.containsKey(key) ) {
				tempList = keywordsIndex.get(key);
				tempList.add(currOcc);

				insertLastOccurrence(tempList);		
				keywordsIndex.put(key, tempList);

			} else {
				tempList = new ArrayList<Occurrence>();				

				tempList.add(currOcc);
				keywordsIndex.put(key, tempList);
			}	
		}

	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		String specialChars = ".,?:;!";

		// trim the leading and trailing spaces
		String retWord = word.trim();	
		char lastChar = retWord.charAt(retWord.length()-1);

		// trim trailing special chars
		while( retWord.length() > 1 && specialChars.indexOf(lastChar+"") != -1 ) {
			retWord = retWord.substring(0, retWord.length()-1);
			
			if (retWord.length() > 1) {
				lastChar = retWord.charAt(retWord.length()-1);				
			}
		}

		// is alphabetic
		if (!isAlphabetic(retWord)) {
			return null;
		}

		// is a noise word
		for( String nw : noiseWords.keySet() ) {
			if(retWord.equalsIgnoreCase(nw)){
				return null;
			}
		}

		// return LOWERCASE word
		return retWord.toLowerCase();
	}

	// is alphabetic letter
	private boolean isAlphabetic(String word) {

		for(int i = 0; i < word.length(); i++){
			if(!Character.isLetter(word.charAt(i))){
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE

		// if the size of the input list is 1
        if (occs.size()==1) return null;

        ArrayList<Integer> midPointIndexes = new ArrayList<Integer>();

        Occurrence lasOccs = occs.get(occs.size() - 1);
        int lo = 0, hi = occs.size()-2, mid = 0;
        
        //binary search
        while (lo <= hi) {
        	
            mid = (lo + hi) / 2;
        
            midPointIndexes.add(mid);
            
            if (lasOccs.frequency > occs.get(mid).frequency) {
                hi = mid - 1;
            } if (lasOccs.frequency < occs.get(mid).frequency) {
                lo = mid + 1;

                if (hi <= mid)
                    mid = mid + 1;
            } else {
                break;
            }
        }
        
        occs.add(mid, lasOccs);
        occs.remove(occs.size() - 1);

        return midPointIndexes;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		ArrayList<String> top5search = new ArrayList<String>();
		ArrayList<Occurrence> kw1List = null, kw2List = null;
		int top5 = 5;
		
		kw1List = keywordsIndex.get(kw1.toLowerCase());
		kw2List = keywordsIndex.get(kw2.toLowerCase());
		
		if (kw1List != null && kw2List == null) {

			for(Occurrence kw1Occ : kw1List) {

				if(top5search.size() >= top5) break;

				if(!top5search.contains(kw1Occ.document)) {
					top5search.add(kw1Occ.document);
				}
			}
			
		} else if (kw1List == null && kw2List != null) {
			
			for(Occurrence kw2Occ : kw2List) {

				if(top5search.size() >= top5) break;

				if(!top5search.contains(kw2Occ.document)) {
					top5search.add(kw2Occ.document);
				}
			}

		} else if (kw1List != null && kw2List != null) {

			for(Occurrence kw1Occ : kw1List) {

				if(top5search.size() >= top5) break;

				for(Occurrence kw2Occ : kw2List) {

					if(top5search.size() >= top5) break;
					
					if(kw2Occ.frequency <= kw1Occ.frequency 
							&& !top5search.contains(kw1Occ.document)) {
					
						top5search.add(kw1Occ.document);
					
					} else if (kw2Occ.frequency > kw1Occ.frequency 
							&& !top5search.contains(kw2Occ.document)) {

						top5search.add(kw2Occ.document);

					}
				}
			}

		}

		for(String doc: top5search){
		System.out.println(doc);
		}
		
		return (top5search.size() == 0 ? null : top5search);
	}
}
