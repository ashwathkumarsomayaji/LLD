package LRU;

import java.util.HashMap;

public class LRUCache {
    HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
    int capacity = 0;
    Node head;
    Node tail;

    static class Node {
        Integer key;
        Integer value;
        Node prev;
        Node next;

        public Node(Integer key, Integer value) {
            this.key = key;
            this.value = value;
            this.prev = null;
            this.next = null;
        }
    }

   public void lrucache_intialise(int capacity) {
         head = new Node(0,0);
         tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;

        this.capacity = capacity;
    }

    public void printLL() {
        Node n = head;
        while (n != null) {
            System.out.print("key:" + n.key);
            System.out.print(" value:" + n.value);
            if (n.prev != null)
                System.out.print(" prev:" + n.prev.key);
            else
                System.out.print(" prev:" + n.prev);
            if (n.next != null)
                System.out.print(" next:" + n.next.key);
            else
                System.out.print(" next:" + n.next);
            System.out.println();
            n = n.next;

        }
    }

    public boolean insert(Node llNode) {
        hm.put(llNode.key, llNode);

        llNode.next = head.next;
        llNode.prev = head;
        head.next.prev = llNode;
        head.next = llNode;
        return true;
    }

     public boolean remove(Node llNode) {
            hm.remove(llNode.key);
            llNode.prev.next = llNode.next;
            llNode.next.prev = llNode.prev;
            return true;
     }

    public boolean put(int key, int value){
    if(hm.containsKey(key)){
        remove(hm.get(key));
    }
    if(hm.size() == capacity){
        remove(tail.prev);
    }
        insert(new Node (key, value));
        return true;
    }



    public int get(int key){
        if(hm.containsKey(key)) {
            Node llNode = hm.get(key);
            remove(llNode);
            insert(llNode);
            return llNode.value;
        }
        else return -1;
    }

    public static void main(String[] args) {
        LRUCache cache =  new LRUCache();
        cache.lrucache_intialise(2);

        cache.put(1, 1);
        cache.put(2,2);
        System.out.println(cache.get(1));
        cache.put(3,3);
        System.out.println(cache.get(2));
        cache.put(4, 4);
        System.out.println(cache.get(1));
        System.out.println(cache.get(3));
        System.out.println(cache.get(4));
        cache.printLL();
    }
}
