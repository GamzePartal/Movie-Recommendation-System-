// NODE TABANLI MAX HEAP
// Dizi kullanılmıyor! Hoca bunu özellikle istedi.
// En yüksek similarity her zaman root'ta bulunur.
//
// Yeni düğümün nereye ekleneceğini bulmak için
// size'ın binary gösterimini kullanıyoruz:
//   size+1 = eklemenin yapılacağı konumun path'i
//   örnek: size=6 → binary(7)=111 → root→sağ→sağ

public class MaxHeap {

    private HeapNode root;
    private int size;

    public MaxHeap() {
        this.root = null;
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // ─────────────────────────────────────────
    // INSERT: yeni kullanıcıyı heap'e ekle
    // ─────────────────────────────────────────
    public void insert(User user, double similarity) {
        HeapNode newNode = new HeapNode(user, similarity);
        size++;

        if (root == null) {
            root = newNode;
            return;
        }

        // Eklenecek konumun ebeveynini bul
        HeapNode parent = findNode(size / 2);

        // Sol mu sağ mı çocuk?
        if (parent.left == null) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        newNode.parent = parent;

        // Heap özelliğini koru: yeni düğümü yukarı taşı
        siftUp(newNode);
    }

    // ─────────────────────────────────────────
    // EXTRACT MAX: en benzer kullanıcıyı çıkar
    // ─────────────────────────────────────────
    public HeapNode extractMax() {
        if (root == null) return null;

        HeapNode maxNode = root;

        if (size == 1) {
            root = null;
            size--;
            return maxNode;
        }

        // En sondaki düğümü bul, root ile verisini değiştir
        HeapNode lastNode = findNode(size);
        if (lastNode == null) { size--; return maxNode; } // güvenlik
        swapData(root, lastNode);

        // Son düğümü sil
        removeLastNode(lastNode);
        size--;

        // Root'u aşağı taşı
        siftDown(root);

        return maxNode;
    }

    // ─────────────────────────────────────────
    // PEEK: sadece root'u gör, çıkarma
    // ─────────────────────────────────────────
    public HeapNode peek() {
        return root;
    }

    // ─────────────────────────────────────────
    // YARDIMCI: n numaralı düğümü bul (1-indexed, root=1)
    //
    // Tam complete binary tree'de n'inci düğümün yolu:
    // n'in binary gösterimi MSB'den sonraki bitleri verir:
    //   0 → sol çocuğa git
    //   1 → sağ çocuğa git
    //
    // Örnek: n=6 → binary="110"
    //   MSB (1) = root, atla
    //   bit '1' → sağa git
    //   bit '0' → sola git   ← bu HEDEF düğüm
    //
    // ÖNCEKİ HATA: döngü "binary.length() - 1" ile bitiyordu,
    // yani son bit atlanıyordu → yanlış (bazen null) düğüm dönüyordu.
    // DÜZELTME: tüm bitleri (MSB hariç) dolaş.
    // ─────────────────────────────────────────
    private HeapNode findNode(int n) {
        if (n == 1) return root;

        String binary = Integer.toBinaryString(n);
        HeapNode current = root;

        // index 1'den başla: MSB (index 0) kökü temsil eder, atla
        // tüm kalan bitleri dolaş (son bit dahil)
        for (int i = 1; i < binary.length(); i++) {
            if (current == null) return null; // güvenlik kontrolü
            if (binary.charAt(i) == '0') {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return current;
    }

    // ─────────────────────────────────────────
    // SIFT UP: düğümü ebeveynlerle karşılaştırarak yukarı taşı
    // ─────────────────────────────────────────
    private void siftUp(HeapNode node) {
        while (node.parent != null && node.similarity > node.parent.similarity) {
            swapData(node, node.parent);
            node = node.parent;
        }
    }

    // ─────────────────────────────────────────
    // SIFT DOWN: düğümü çocuklarla karşılaştırarak aşağı taşı
    // ─────────────────────────────────────────
    private void siftDown(HeapNode node) {
        while (true) {
            HeapNode largest = node;

            if (node.left != null && node.left.similarity > largest.similarity) {
                largest = node.left;
            }
            if (node.right != null && node.right.similarity > largest.similarity) {
                largest = node.right;
            }

            if (largest == node) break; // heap özelliği sağlandı

            swapData(node, largest);
            node = largest;
        }
    }

    // ─────────────────────────────────────────
    // SWAP: iki düğümün verisini değiştir (pointer'ları değil!)
    // ─────────────────────────────────────────
    private void swapData(HeapNode a, HeapNode b) {
        User tempUser = a.user;
        double tempSim = a.similarity;
        a.user = b.user;
        a.similarity = b.similarity;
        b.user = tempUser;
        b.similarity = tempSim;
    }

    // ─────────────────────────────────────────
    // Son düğümü ağaçtan sil (ebeveyninden kopar)
    // ─────────────────────────────────────────
    private void removeLastNode(HeapNode lastNode) {
        HeapNode parent = lastNode.parent;
        if (parent == null) {
            root = null;
        } else if (parent.right == lastNode) {
            parent.right = null;
        } else {
            parent.left = null;
        }
        lastNode.parent = null;
    }
}