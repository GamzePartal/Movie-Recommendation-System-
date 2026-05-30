//MaxHeap içindeki bir node u temsil eder
public class HeapNode {

    public User     user;
    public Movie    movie;          // film heapi tutatr
    public double   similarity; //similarity,rating
    public HeapNode leftChild;
    public HeapNode rightChild;
    public HeapNode parent;

    public HeapNode(User user, double similarity) {
        this.user       = user;
        this.movie      = null;
        this.similarity = similarity;  //kullanıcının hedef kullanıcıya benzerlik skorudur
        this.leftChild  = null;
        this.rightChild = null;
        this.parent     = null;
    }

    // Film heap'i için constructor
    public HeapNode(Movie movie, double rating) {
        this.user       = null;
        this.movie      = movie;
        this.similarity = rating;  //similarity alanına rating puanı yazılır
        this.leftChild  = null;
        this.rightChild = null;
        this.parent     = null;
    }
}