
// MaxHeap'teki her düğümü temsil eder
// left/right/parent pointerlarıyla gerçek bir bağlı ağaç düğümü temsil eder
public class HeapNode {
    public User user;           // kullanıcı verisi
    public double similarity;   // cosine similarity skoru (buyuk deger = yuksek oncelik)

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
