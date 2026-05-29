// MaxHeap içindeki her düğümü temsil eder
// Pointer tabanlı yapı — hoca dizi kullanmayı yasakladı
public class HeapNode {

    public User     user;           // bu düğümde saklanan kullanıcı
    public double   similarity;     // hedefe benzerlik skoru (büyük = daha benzer)

    public HeapNode leftChild;      // sol çocuk düğüm
    public HeapNode rightChild;     // sağ çocuk düğüm
    public HeapNode parent;         // ebeveyn düğüm (siftUp için gerekli)

    public HeapNode(User user, double similarity) {
        this.user       = user;
        this.similarity = similarity;
        this.leftChild  = null;
        this.rightChild = null;
        this.parent     = null;
    }
}