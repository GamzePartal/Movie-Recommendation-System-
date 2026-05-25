public class HeapNode {
    public User user;           // asıl kullanıcı verisi
    public double similarity;   // cosine similarity skoru (öncelik)

    public HeapNode left;       // sol çocuk
    public HeapNode right;      // sağ çocuk
    public HeapNode parent;     // ebeveyn (sift-up için gerekli)

    public HeapNode(User user, double similarity) {
        this.user = user;
        this.similarity = similarity;
        this.left = null;
        this.right = null;
        this.parent = null;
    }
}
