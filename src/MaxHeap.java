//en büyük değeri her zaman rootta tutan MaxHeap yapısını kurar
//1. En benzer kullanıcıyı bulmak
//2. En yüksek puanlı filmi bulmak
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

    //kullanıcıyı heape ekle
    public void insert(User user, double similarity) {
        HeapNode newNode = new HeapNode(user, similarity);
        size++;

        if (root == null) {
            root = newNode;
            return;
        }

        HeapNode parentNode = findNodeAtIndex(size / 2);

        if (parentNode.leftChild == null) {
            parentNode.leftChild = newNode;
        } else {
            parentNode.rightChild = newNode;
        }

        newNode.parent = parentNode;
        siftUp(newNode);
    }

    public void insertMovie(Movie movie, double rating) {
        HeapNode newNode = new HeapNode(movie, rating);
        size++;

        if (root == null) {
            root = newNode;
            return;
        }

        HeapNode parentNode = findNodeAtIndex(size / 2);

        if (parentNode.leftChild == null) {
            parentNode.leftChild = newNode;
        } else {
            parentNode.rightChild = newNode;
        }

        newNode.parent = parentNode;
        siftUp(newNode);
    }

    //root döndürürüz ama root değişeceği için bilgileri kopyala
    public HeapNode extractMax() {
        if (root == null) return null;

        HeapNode maxNode = new HeapNode(root.user, root.similarity);
        maxNode.movie = root.movie;

        if (size == 1) {
            root = null;
            size--;
            return maxNode;
        }

        HeapNode lastNode = findNodeAtIndex(size);

        root.user       = lastNode.user;
        root.movie      = lastNode.movie;
        root.similarity = lastNode.similarity;

        removeLastNode(lastNode);
        size--;

        siftDown(root);

        return maxNode;
    }

    //Yeni eklenen node parentten büyükse yukarı çıkar
    private void siftUp(HeapNode currentNode) {
        while (currentNode.parent != null &&
                currentNode.similarity > currentNode.parent.similarity) {

            swapNodeData(currentNode, currentNode.parent);
            currentNode = currentNode.parent;
        }
    }

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

            if (largest == currentNode) break;

            swapNodeData(currentNode, largest);
            currentNode = largest;
        }
    }

    //belirli indexteki node u bulur array kullanmıyoruz binary yol kullanıyoruz 0 sola git 1 sağa git
    private HeapNode findNodeAtIndex(int index) {
        if (index == 1) return root;

        String binaryPath = Integer.toBinaryString(index);
        HeapNode currentNode = root;

        for (int i = 1; i < binaryPath.length(); i++) {
            if (currentNode == null) return null;

            if (binaryPath.charAt(i) == '0') {
                currentNode = currentNode.leftChild;
            } else {
                currentNode = currentNode.rightChild;
            }
        }

        return currentNode;
    }

    //iki node un bağlantılarını değil sadece içindeki verileri değiştirir
    private void swapNodeData(HeapNode firstNode, HeapNode secondNode) {
        User   tempUser       = firstNode.user;
        Movie  tempMovie      = firstNode.movie;
        double tempSimilarity = firstNode.similarity;

        firstNode.user       = secondNode.user;
        firstNode.movie      = secondNode.movie;
        firstNode.similarity = secondNode.similarity;

        secondNode.user       = tempUser;
        secondNode.movie      = tempMovie;
        secondNode.similarity = tempSimilarity;
    }

    //Son node u parentından koparır
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