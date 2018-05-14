import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TernarySearchTree<T extends Comparable<T>> {
    private TSTNode<T> root;

    /**
     * Insert an item into the TST.
     *
     * Instead of accepting a String, we accept an {@link Iterable} of generic T-- so
     * you may make the TST of any comparable type T.
     *
     * @param items An iterable list of generic T.
     *              We use {@link StringWrapper} as our wrapper for T=Character.
     */
    public void insert(Iterable<T> items){
        TSTNode<T> current = this.root;
        // in our loop, current is generally the last node we inserted
        // into the TST. However, we have to handle the first element separately
        // because we can't do it with the parent of the root (and if we did
        // it would look weirder).
        boolean first = true;
        for(T item : items) {
            if (this.root == null) { // current == root == null; we should make the root our first letter.
                this.root = new TSTNode<T>(item);
                current = this.root;
            } else if(first){ // if it's the first element, instead of dealing with the child, deal with the current node
                current = insertHelper(item, current);
            } else if(current.getCenterNode() == null){ // in this case we just placed "current"-- the child may go directly below
                current.setCenterNode(new TSTNode<T>(item));
                current = current.getCenterNode();
                continue;
            } else { // if something else if in the place of where the center node would be, we use our insert helper to traverse to
                // where we should actually put the node. insertHelper is called recursively.
                current = insertHelper(item, current.getCenterNode());
            }
            first = false; // flip our boolean flag after one iteration.
        }
        current.setEndPoint(true); // current is the last node we inserted-- flag it as an endpoint.
    }

    /**
     * A recursive function which is called to traverse a tree and insert where the node belongs.
     * @param item Item to insert.
     * @param current Node which we are comparing against
     * @return
     */
    private TSTNode<T> insertHelper(T item, TSTNode<T> current){
        int comparator = item.compareTo(current.getElement());
        if(comparator == 0){
            return current; // if the comparator is 0, this node was already what we wanted. no need to insert.
        }
        if(comparator < 0){ // if no left child exists, we belong there. else, continue our traversal on the left subtree.
            if(current.getLeftNode() == null){
                current.setLeftNode(new TSTNode<T>(item));
                return current.getLeftNode();
            } else {
                return insertHelper(item, current.getLeftNode());
            }
        }
        //if(comparator > 0) // same thing for right side if the comparator is above 0.
        if(current.getRightNode() == null){
            current.setRightNode(new TSTNode<T>(item));
            return current.getRightNode();
        } else {
            return insertHelper(item, current.getRightNode());
        }
    }


    /**
     * Check if an element exists within the TST.
     * @param element Iterable of generic T-- element to check.
     * @return boolean, if the item exists in the TST.
     */
    public boolean contains(Iterable<T> element) {
        TSTNode<T> node = this.getNode(element);
        return node != null && node.isEndPoint(); // short-circuit evaluation prevents NullPointerException
    }

    /**
     * Helper function for traversing and finding an element in a tree.
     * @param element Iterable of generic T
     * @return Node representing the last element in {@param element}.
     */
    private TSTNode<T> getNode(Iterable<T> element) {
        TSTNode<T> curr_node = root;
        Iterator<T> iterator = element.iterator();
        T item = iterator.next();
        while (curr_node != null) {
            int comparator = item.compareTo(curr_node.getElement());
            //System.out.println(comparator + ", " + item + ". 0" + curr_node.getElement());
            if (comparator == 0) {
                if (!iterator.hasNext()) break; // we're out of letters -- we found the end.
                item = iterator.next(); // only advance the iterator when we found a match
                //System.out.println("proceeding with " + curr_node.getElement());
                curr_node = curr_node.getCenterNode();
            } else if (comparator < 0) { // continue the traversal on one side of the tree.
                curr_node = curr_node.getLeftNode();
            } else {// if(comparator > 0)
                curr_node = curr_node.getRightNode();
            }
        }
        return curr_node; // curr_node will be null if we failed to find it.
    }

    /**
     * Given a prefix of Iterable of generic T, we find the node which starts with this prefix
     * and then traverse to find all children of that node.
     * @param elements where to begin our traversal
     * @return an array of iterable results.
     */
    public ArrayList<ArrayList<T>> getChildren(Iterable<T> elements) {
        TSTNode<T> node = getNode(elements);
        if (node == null) return null;
        ArrayList<ArrayList<T>> words = new ArrayList<ArrayList<T>>(); // shallow copies on traversal
        ArrayList<T> running = new ArrayList<T>();
        Iterator<T> i = elements.iterator();
        while (i.hasNext()) {
            T element = i.next();
            running.add(element);
        }
        if(node.getCenterNode() == null) return words; // empty
        node.getCenterNode().traverse(words, running); // begin from center because "node" must've been a match as well.
        return words;
    }
}

/**
 * A node in our TST.
 *
 * @param <T> the element this node contains.
 *
 * @author Daniel Barnes '21
 */
class TSTNode<T extends Comparable<T>> {

    /**
     * Three nodes in a TST: left, center, right.
     *
     * Left is less than, center means a match, right means more than
     */
    private TSTNode<T> leftNode;
    private TSTNode<T> centerNode;
    private TSTNode<T> rightNode;
    /**
     * Element we hold
     */
    private final T element;
    /**
     * If we are an endpoint
     */
    private boolean endPoint;

    /**
     * Create new node. By default, we are not an endpoint.
     * @param element element we hold
     */
    public TSTNode(T element){
        this(element, false);
    }

    /**
     * Create new node and set if we're an endpoint or not.
     * @param element element we hold
     * @param endPoint if we are an endpoint.
     */
    public TSTNode(T element, boolean endPoint){
        this.element = element;
        this.endPoint = endPoint;
    }

    /**
     * Traverse down to find all children, and add them to elementList.
     * @param elementList running list of elements
     * @param running the elements we passed to get to this point. if we're an endpoint or center, we append this node's element.
     */
    protected void traverse(ArrayList<ArrayList<T>> elementList, ArrayList<T> running){
        ArrayList<T> curr = new ArrayList<T>(running); // shallow copy
        curr.add(this.element);
        if(this.isEndPoint()) elementList.add(curr);
        if(this.leftNode != null) this.leftNode.traverse(elementList, running);
        if(this.centerNode != null) this.centerNode.traverse(elementList, curr);
        if(this.rightNode != null) this.rightNode.traverse(elementList, running);
    }

    /**
     * Set a new left, center, or right node
     * @param node Node to set.
     */
    public void setLeftNode(TSTNode<T> node){
        this.leftNode = node;
    }
    public void setCenterNode(TSTNode<T> node){
        this.centerNode = node;
    }
    public void setRightNode(TSTNode<T> node) {
        this.rightNode = node;
    }

    /**
     * Get our current element.
     * @return T element.
     */
    public T getElement(){
        return this.element;
    }

    /**
     * Check if we're an endpoint.
     * @return if we are an endpoint.
     */
    public boolean isEndPoint(){
        return this.endPoint;
    }

    /**
     * Set if we are an endpoint.
     * @param endPoint boolean, if we are an endpoint.
     */
    public void setEndPoint(boolean endPoint){
        this.endPoint = endPoint;
    }

    /**
     * Get our left, right, or center node.
     * @return our respective child node.
     */
    public TSTNode<T> getLeftNode(){
        return this.leftNode;
    }
    public TSTNode<T> getCenterNode(){
        return this.centerNode;
    }
    public TSTNode<T> getRightNode(){
        return this.rightNode;
    }

    /**
     * For debug purposes: print our own characer, and if we're an endpoint.
     * @return String
     */
    public String toString(){
        return this.element.toString() + " " + this.isEndPoint();
    }
}

/**
 * A StringWrapper, which is an iterable of Character objects.
 *
 * We pass this to our TST when we want to use strings, so we can add each character into the TST
 * while still allowing the TST to be generic, and accept any Iterable of generic T.
 *
 * This feels very Python-ic.
 *
 * @author Daniel Barnes '21
 */
class StringWrapper implements Iterable<Character> {
    /**
     * String we wrap
     */
    private String str;

    /**
     * Create a new string wrapper for a given string
     * @param str String to wrap
     */
    public StringWrapper(String str){
        this.str = str;
    }

    /**
     * Create a new StringWrapper, wrapping the Iterable we are passed. Turn it into a string and wrap it.
     * @param chars Iterable of object Character to wrap.
     */
    public StringWrapper(Iterable<Character> chars){
        this.str = "";
        for(Character i : chars){
            str += i;
        }
    }

    /**
     * Return the string we wrap.
     * @return String
     */
    public String getString(){
        return this.str;
    }

    /**
     * Return the same string-- this is included for debug purposes.
     * @return
     */
    public String toString(){
        return this.str;
    }

    /**
     * Override the Iterable method iterator() -- return an iterator which iterates
     * through each character, as Character objects, of the String we wrap.
     * @return An iterable object of Characters, representing this string.
     */
    @Override
    public Iterator<Character> iterator(){
        Character[] charArray = new Character[str.length()];
        for(int i = 0; i < charArray.length; i++){
            charArray[i] = str.charAt(i);
        }
        return Arrays.asList(charArray).iterator();
    }

    /**
     * Append a letter to the string.
     * @param c char
     */
    public void addLetter(char c){
        this.str += c;
    }

    /**
     * Remove the last letter of the string, or do nothing if the string is empty.
     */
    public void removeLast(){
        if(this.str.length() == 0) return;
        this.str = this.str.substring(0, this.str.length() - 1);
    }
}