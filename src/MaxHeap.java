// Pointer tabanlı Max-Heap
// Kural: her düğüm çocuklarından büyük olmalı → kök her zaman en büyük similarity değerine sahiptir
// Dizi kullanılmadı, leftChild / rightChild / parent pointer yapısı kullanıldı
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

    public HeapNode peek() {
        return root;
    }

    // Yeni kullanıcıyı heap'e ekler
    public void insert(User user, double similarity) {
        HeapNode newNode = new HeapNode(user, similarity);
        size++;

        if (root == null) {
            root = newNode;
            return;
        }

        // Complete binary tree mantığı:
        // Yeni node'un parent'ı size / 2 indeksindeki node'dur
        HeapNode parentNode = findNodeAtIndex(size / 2);

        if (parentNode.leftChild == null) {
            parentNode.leftChild = newNode;
        } else {
            parentNode.rightChild = newNode;
        }

        newNode.parent = parentNode;

        siftUp(newNode);
    }

    // Heap'teki en büyük similarity değerine sahip kullanıcıyı çıkarır
    public HeapNode extractMax() {
        if (root == null) {
            return null;
        }

        // ÖNEMLİ:
        // Root referansını değil, root'un verisinin kopyasını saklıyoruz.
        // Aksi halde root değişince döndürülen maxNode da değişmiş olur.
        HeapNode maxNode = new HeapNode(root.user, root.similarity);

        if (size == 1) {
            root = null;
            size--;
            return maxNode;
        }

        HeapNode lastNode = findNodeAtIndex(size);

        // Son node'un verisini root'a taşı
        root.user = lastNode.user;
        root.similarity = lastNode.similarity;

        // Son node'u ağaçtan kopar
        removeLastNode(lastNode);
        size--;

        // Root'a taşınan değer küçük olabilir, aşağı indir
        siftDown(root);

        return maxNode;
    }

    // Yeni eklenen node parent'ından büyükse yukarı çıkar
    private void siftUp(HeapNode currentNode) {
        while (currentNode.parent != null &&
                currentNode.similarity > currentNode.parent.similarity) {

            swapNodeData(currentNode, currentNode.parent);
            currentNode = currentNode.parent;
        }
    }

    // Root veya ara node çocuklarından küçükse aşağı iner
    private void siftDown(HeapNode currentNode) {
        while (true) {
            HeapNode largest = currentNode;

            if (currentNode.leftChild != null &&
                    currentNode.leftChild.similarity > largest.similarity) {
                largest = currentNode.leftChild;
            }

            if (currentNode.rightChild != null &&
                    currentNode.rightChild.similarity > largest.similarity) {
                largest = currentNode.rightChild;
            }

            if (largest == currentNode) {
                break;
            }

            swapNodeData(currentNode, largest);
            currentNode = largest;
        }
    }

    // Heap'in complete tree index mantığına göre node bulur
    // index = 1 root
    // index'in binary karşılığı yol verir:
    // 0 = sol, 1 = sağ
    private HeapNode findNodeAtIndex(int index) {
        if (index == 1) {
            return root;
        }

        String binaryPath = Integer.toBinaryString(index);
        HeapNode currentNode = root;

        // İlk bit root'u temsil eder, o yüzden 1'den başlıyoruz
        for (int i = 1; i < binaryPath.length(); i++) {
            if (currentNode == null) {
                return null;
            }

            if (binaryPath.charAt(i) == '0') {
                currentNode = currentNode.leftChild;
            } else {
                currentNode = currentNode.rightChild;
            }
        }

        return currentNode;
    }

    // Sadece node verilerini değiştirir, pointer bağlantılarına dokunmaz
    private void swapNodeData(HeapNode firstNode, HeapNode secondNode) {
        User tempUser = firstNode.user;
        double tempSimilarity = firstNode.similarity;

        firstNode.user = secondNode.user;
        firstNode.similarity = secondNode.similarity;

        secondNode.user = tempUser;
        secondNode.similarity = tempSimilarity;
    }

    // Son node'u parent'ından koparır
    private void removeLastNode(HeapNode lastNode) {
        HeapNode parentNode = lastNode.parent;

        if (parentNode == null) {
            root = null;
        } else if (parentNode.rightChild == lastNode) {
            parentNode.rightChild = null;
        } else {
            parentNode.leftChild = null;
        }

        lastNode.parent = null;
    }
}