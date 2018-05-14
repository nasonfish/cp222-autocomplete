import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Day 15 assignment-- autocomplete.
 *
 * We create a TST to store our words, read in from a file and insert each word into the TST,
 * and then show an autocomplete interface where the user can type words while we suggest potential completions.
 *
 * We use ANSI codes and a non-blocking terminak so this is recommended to be run in a console which supports them.
 *
 * However, if this is not a possibility, we attempt to fall-back on a simple scanner in/out method, and this can
 * also be triggered manually with invocation using -t.
 *
 * jline.jar must be in our classpath.
 */
public class Day15 {

    /**
     * Tree for storing words.
     */
    private final TernarySearchTree<Character> tree;

    /**
     * if we should use our advanced terminal or just a scanner.
     */
    private static boolean terminal = true;


    /**
     * Main method: interpret arguments and then run program.
     * @param args -t if you want to use the terminal. provide an optional argument if you want to use a different word list.
     * @throws IOException
     */
	public static void main(String[] args) {
	    Day15.terminal = true;
	    String url = "http://cs.coloradocollege.edu/~mwhitehead/courses/2017_2018/CP222/Assignments/11/words.txt";
	    if(args.length > 0){
	        for(String arg : args) {
                if (arg.startsWith("-")){
                    if(arg.equals("-t")){
                        Day15.terminal = false;
                    }
                } else {
                    url = arg;
                }
            }
        }
        Day15 instance = new Day15(url);
	    instance.predict();
	}

	public Day15(String url){
	    this.tree = new TernarySearchTree<Character>();
	    this.load(url);
    }

    /**
     * Load in data from given URL, or the wordlist.txt by default. Use the Utils class and a scanner.
     * @param url URL to read words from and insert into trie.
     */
	public void load(String url){
        Scanner scanner;
        try {
            scanner = Utils.pullText(new URL(url));
        } catch (MalformedURLException e) {
            System.out.println("Could not grab data from the Internet.");
            e.printStackTrace();
            System.exit(1);
            return;
        }
        while(scanner.hasNextLine()){
            tree.insert(new StringWrapper(scanner.nextLine()));
        }
        scanner.close();
    }

    /**
     * Here are several variables for my convenience. I use ANSI codes to print out the words
     * the way that I do.
     */
    public final String LINE_FEED = "\r"; // return to beginning of line
    public final String ANSI_CLEAR_SCREEN = "\u001B[2J"; // clear screen completely
	public final String ANSI_RESET_CURSOR = "\u001B[H"; // move cursor to 1,1
    public final String ANSI_CLEAR_LINE = "\u001B[2K"; // clear line
    public final String ANSI_COLOR_RED = "\u001B[31m"; // color red (not a word)
    public final String ANSI_COLOR_GREEN = "\u001B[32m"; // color green (correct word, and not a prefix for another word)
    public final String ANSI_COLOR_RESET = "\u001B[0m"; // reset color to default
    public final String ANSI_COLOR_MAGENTA = "\u001B[35m"; // color magenta-- for the autocomplete they haven't typed yet


    /**
     * Clear the screen and reset the cursor to 1,1, in the top left, using ANSI codes.
     */
    public void clearScreen(){
        System.out.print(ANSI_CLEAR_SCREEN + ANSI_RESET_CURSOR); // clear screen and reset cursor to 1,1
    }


    /**
     * Method which we loop through, reading user input and doing completions.
     *
     * Fall back on the Scanner if they don't support the non-blocking reader.
     */
    public void predict() {
        if(!Day15.terminal){ // -t flag
            this.doScannerFallback(); // lame
            return;
        }
        try { // I know it's bad practice to have this huge try block
            // however, the only methods which can trigger it is
            // creating a terminal and reading from the terminal.
            // in either case, if there's an error creating a terminal with the library
            // or we can't read, we should fall back on the scanner.
            Terminal terminal = TerminalBuilder.terminal();
            terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();
            // a non-blocking reader means we read character-by-character regardless of if the user
            // has pressed enter yet or not.
            StringWrapper search = new StringWrapper(""); // concatenate and delete from as necessary.
            this.clearScreen();
            int character;
            while((character = reader.read()) > 0) { // loop every time a key is pressed.
                this.clearScreen();
                if (character == 27) { // ESC key.
                    System.out.println("Exiting!");
                    System.exit(0);
                    return;
                }
                if (character < 32) { // characters under 32 are things like newline, NUL, other weird things which don't exist in the trie.
                    continue; // unprintable character. ignore.
                }
                if (character == 127) { // backspace
                    search.removeLast();
                } else { // a normal letter that we can parse.
                    search.addLetter((char) character); // add to search.
                }
                if (search.getString().length() == 0) { // empty search
                    System.out.print(ANSI_CLEAR_LINE); // return to beginning of line, and clear line.
                    continue;
                }
                ArrayList<ArrayList<Character>> children = tree.getChildren(search); // do search and get results.
                if (children == null) { // no results
                    System.out.print(LINE_FEED + ANSI_COLOR_RED + search + ANSI_COLOR_RESET); // turn our text red
                } else if (children.size() == 0) { // one result, and it's the word we entered.
                    System.out.print(LINE_FEED + ANSI_COLOR_GREEN + search + ANSI_COLOR_RESET); // turn our text green
                } else { // multiple results
                    StringWrapper result = new StringWrapper(children.get(0)); // a suggested autocomplete
                    for (int i = 1; i < children.size() && i < terminal.getHeight(); i++) { // for other results which may fit on the screen
                        System.out.print("\n" + new StringWrapper(children.get(i))); // print them too, below our result
                    }
                    System.out.print(ANSI_RESET_CURSOR); // finally go back uo
                    // print our autocomplete in magenta, go back, and print what they typed (so they may continue typing
                    // where they left off).
                    System.out.print(ANSI_COLOR_MAGENTA + result + LINE_FEED + ANSI_COLOR_RESET + search);
                }
            }
        } catch(IOException ex) { // a problem with the non-blocking terminal
            this.doScannerFallback();
        }
    }

    /**
     * If there's a problem and we can't do our ~fancy~ autocomplete, this is a Scanner alternative for
     * portability.
     *
     * This may be invoked manually using the flag -t.
     */
    public void doScannerFallback(){
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()) {
            System.out.print("Enter word: ");
            String word = scanner.nextLine();  // read lines one at a time.
            ArrayList<ArrayList<Character>> words = tree.getChildren(new StringWrapper(word)); // get results
            if (words == null) {
                System.out.println("No completions found.");
                continue;
            }
            // note that if there's no completions but the word they typed was a word, it will output nothing.
            for (ArrayList<Character> w : words) { // for every word,
                System.out.println(new StringWrapper(w).getString()); // print it.
            }
        }
        System.out.println("Thank you!"); // EOF, exit.
    }
}
