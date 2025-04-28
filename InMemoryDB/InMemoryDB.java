package inmemorydb;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class Item {
    String field;
    String fieldValue;
    int timestamp;
    int ttl;

    //this is used when creating the item with field, fieldValue, timestamp and ttl.
    public Item(String field, String fieldValue, int timestamp, int ttl) {
        this.field = field;
        this.fieldValue = fieldValue;
        this.timestamp = timestamp;
        this.ttl = ttl;
    }

    //this is used when creating the item with field and fieldValue.
    public Item(String field, String fieldValue) {
        this.field = field;
        this.fieldValue = fieldValue;
    }

    public String getField() {
        return field;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    //isAlive method to find out the live/expired items
    public boolean isAlive(int ts) {
        return ttl == 0 || ts <= timestamp+ttl; //currentTime <= timestamp when item is created+ttl delay.

    }
}
public class InMemoryDB {
    private final Map<String, List<Item>> store = new ConcurrentHashMap<>();
    ReentrantLock lock = new ReentrantLock();

    //set(key, Item)
    public void set(String key, String field, String value) {
        lock.lock();
        try {
            store.computeIfAbsent(key, s -> new ArrayList<>()).add(new Item(field, value));
        } finally {
            lock.unlock();
        }
    }

    public String get(String key, String field){
        lock.lock();
        try {
            List<Item> items  = store.get(key);
            if(items == null) return null;

            for(Item item : items) {
                if(item.getField().equals(field))
                    return item.fieldValue;
            }
            return null;
        }finally {
            lock.unlock();
        }
    }


    public void delete(String key, String field) {
        lock.lock();
        try {
          List<Item> items = store.get(key);
          items.removeIf(item -> item.getField().equals(field));
        }finally {
            lock.unlock();
        }
    }

    //SetAt(key, item, ts, ttl=null)
    // items with key, item, ts, ttl=null
    public void setAt(String key, String field, String value, int ts){
        setWithTTL(key,field, value, ts, 0);
    }

    //SetAtWithTTL(key, item, ts, ttl)
    // items with key, item, ts, ttl
    public void setWithTTL(String key, String field, String value, int ts, int ttl){
        lock.lock();
        try {
          List<Item> items = store.computeIfAbsent(key,s -> new ArrayList<>());
          for(Item item: items){
              if(item.getField().equals(field)) {
                  item.fieldValue = value;
                  item.timestamp = ts;
                  item.ttl = ttl;
              }
          }
          items.add(new Item(field, value, ts, ttl));
        }finally {
            lock.unlock();
        }
    }


    //getAt(key, ts) - Always get the live item
        //If item is alive at this ts, then Get those items only
    public String getAt(String key, String field, int ts) {
        lock.lock();
        try {
            List<Item> items = store.get(key);
            if(items == null) return null;
            for(Item item : items) {
                if(item.getField().equalsIgnoreCase(field) && item.isAlive(ts)){
                    return item.getFieldValue();
                }
            }
            return null;
        }
        finally {
            lock.unlock();
        }
    }

    //deleteAt(key, ts) - Always delete the live item
    //If item is alive at this ts, then delete those items only
    public boolean deleteAt (String key, String field, int ts) {
        lock.lock();
        try {
            List<Item> items = store.get(key);
            if(items == null) return false;
           return  items.removeIf(item -> item.getField().equalsIgnoreCase(field) && item.isAlive(ts));
        }finally {
            lock.unlock();
        }
    }

    //ScanAt - scan for the live items at a specific timestamp
    //Get all the items from Store
    //Filter out the live items.
    //Create a list to hold live items with fieldName, fieldValue
    public List<String> scanAt(String key, int ts){
        lock.lock();
        try {
            List<String> out = new ArrayList<>();
            List<Item> items = store.getOrDefault(key, new ArrayList<>());
            //List<Item> items = store.getOrDefault(key,List.of());
            for(Item item : items){
                if(item.isAlive(ts)){
                    out.add(item.getField()+ "(" + item.getFieldValue()+ ")");
                    return out;
                }
            }
            return null;
        }finally {
            lock.unlock();
        }
    }

    //ScanByPrefixAt - scan for the prefixed items at a specific timestamp
        //Get all the items from store.
        //Filter out the live items. (isAlive method to be in Item class)
        //if items.fieldNames.startsWith(prefix) & liveItem
            // --> then Create a list to hold live items with fieldName, fieldValue

        public List<String> scanByPrefixAt(String key, String prefix, int ts){
            lock.lock();
            try {
                List<String> out = new ArrayList<>();
                List<Item> items = store.getOrDefault(key, new ArrayList<>());
                //List<Item> items = store.getOrDefault(key,List.of());
                for(Item item : items){
                    if(item.getField().startsWith(prefix) && item.isAlive(ts) ){
                        out.add(item.getField()+ "(" + item.getFieldValue()+ ")");
                        return out;
                    }
                }
                return null;
            }finally {
                lock.unlock();
            }
        }
}

class InMemoryDBDemo {
    public static void main(String[] args) {
        InMemoryDB db = new InMemoryDB();
        // basic ops
        db.set("user:1", "name", "Alice");
        System.out.println("name = " + db.get("user:1", "name"));   // Alice
        // timestamp / ttl
        db.setWithTTL("user:1", "session", "xyz", 100, 10); // expires at 110
        System.out.println("session@108 -> " + db.getAt("user:1", "session", 108)); // xyz
        System.out.println("session@120 -> " + db.getAt("user:1", "session", 120)); // null (expired)
        // scan
        System.out.println("Scan@200 -> " + db.scanAt("user:1", 200)); // [name(Alice)]
        System.out.println("Prefix 'na' -> " + db.scanByPrefixAt("user:1", "na", 200)); // same list
    }
}
