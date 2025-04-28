package tweetservice;

import java.time.Instant;
import java.util.*;

/* ──────────────────────  DOMAIN  ───────────────────────── */

class Tweet {
    final long   id;
    final String userId;
    final String text;
    final long   ts = Instant.now().toEpochMilli();
    Tweet(long id, String u, String t){ this.id=id; userId=u; text=t; }
    @Override public String toString(){
        return "%s: %s (%d)".formatted(userId, text, ts);
    }
}

class User {
    final String id;
    final Set<String> following = new HashSet<>();
    /** newest tweet at head → singly-linked via next pointer */
    TweetNode head;                       // null if no tweets

    static final class TweetNode {
        final Tweet tweet;
        TweetNode next;
        TweetNode(Tweet t){ tweet=t; }
    }
    User(String id){ this.id=id; following.add(id); }   // follow yourself by default
}

/* ─────────────────────  SERVICE  ───────────────────────── */

public class FeedService {
    private long nextTweetId = 1;
    private final Map<String, User> users = new HashMap<>();

    /* ---------- helpers ------------ */
    private User user(String id){
        return users.computeIfAbsent(id, User::new);
    }

    /* ---------- API ---------------- */
    public Tweet postTweet(String userId, String text){
        User u  = user(userId);
        Tweet t = new Tweet(nextTweetId++, userId, text);
        /* prepend to user’s list */
        User.TweetNode n = new User.TweetNode(t);
        n.next = u.head;  u.head = n;
        return t;
    }
    public void follow(String follower, String followee){
        user(follower).following.add(followee);
    }
    public void unfollow(String follower, String followee){
        if(!follower.equals(followee))
            user(follower).following.remove(followee);
    }

    /** newest 10 tweets across the user + followees */
    public List<Tweet> getNewsFeed(String userId){
        /* min-heap ordered by tweet timestamp descending (newest first) */

        //Comparator.comparingLong orders ascending (1,2,3…).
        //.reversed() flips it to descending (3,2,1…).
        //PriorityQueue in Java is a min-heap by default,
        // so reversing the comparator yields a max-heap where peek() and poll() return the largest timestamp (= newest tweet).

        PriorityQueue<User.TweetNode> pq = new PriorityQueue<>(
                Comparator.comparingLong((User.TweetNode n)->n.tweet.ts) // natural = oldest first
                        .reversed());  // reverse  = newest first


        //PriorityQueue<User.TweetNode> pq = new PriorityQueue<>((a,b) -> Long.compare(b.tweet.ts, a.tweet.ts));

        // seed with head of each followed user's list
        user(userId).following.forEach(fol -> {
            User.TweetNode h = user(fol).head;
            if(h!=null) pq.add(h);
        });

        List<Tweet> res = new ArrayList<>(10);
        while(!pq.isEmpty() && res.size()<10){
            User.TweetNode node = pq.poll();
            res.add(node.tweet);
            if(node.next!=null) pq.add(node.next);      // push the rest of that user's list
        }
        return res;
    }
}

/* ─────────────────────  DEMO / TEST  ───────────────────── */
 class Main {
    public static void main(String[] args) throws InterruptedException {
        FeedService svc = new FeedService();

        svc.postTweet("alice", "Hello Twitter!");
        Thread.sleep(5);                                  // ensure different timestamps
        svc.postTweet("bob",   "Good morning");
        Thread.sleep(5);
        svc.postTweet("alice", "My second tweet");
        svc.postTweet("carol", "Hi all, I'm Carol");

        svc.follow("alice","bob");   // Alice follows Bob
        svc.follow("alice","carol"); // and Carol

        System.out.println("-- Alice's feed --");
        svc.getNewsFeed("alice").forEach(System.out::println);

        svc.unfollow("alice","bob");                       // Alice unfollows Bob
        System.out.println("-- Alice's feed (after unfollow Bob) --");
        svc.getNewsFeed("alice").forEach(System.out::println);
    }
}
//class Tweet - id, userId, ts,
//class User - id, tweerNode head, List<String> following;
//    Every user maintans the time sorted singly linked list tweetNode.
//
//   class  TweetNode {
//       Tweet tweet;
//        TweetNode next;
//       TweetNode(Tweet t){ tweet =t; } // constructor to construct tweet object by tweet text
//   }
//
//   class FeedService {
//     Tweet postTweet(userId, tweetText) {
//         User u = by userId;
//
//         Tweet t = new Tweet(userId, tweetText)
//         User.TweetNode n = new User.TweetNode(t);
//         n.next = u.head;
//         u.head = n;
//         return  t;
//     }
//     void follow(follower, followee) {
////         get the user details from follower.
////         get the list of following from followers
////                 Add the followee to the list of followers
//     }
//
//     void unfollow() {
//        //get the user details from follower.
//        //get the list of following from followers
//        //Remove the followee to the list of followers
//     }
//     List<Tweet> getNewsFeed(String userId) {
//      PriorityQueue<user.TweetNode> pq  = new PriorityQueue<user.TweetNode>()
//              .comparator().thenComparingLong((User.TweetNode n) -> n.tweet.ts)
//         .reverse());
//     }
//
//     //seeding the followee's tweet heads into PQ.
//       user(userId).following.forEach(fol -> {
//           User.TweetNode h = user(fol).head;
//           if(h!=null) pq.add(h);
//       });
//
//     List<Tweet> res= new ArrayList<>(10);
//     while (!pq.isEmpty) && res.size() <10) {
//        User.TweetNode node = pq.poll();
//        res.add(node.tweet);
//        if(node.next != null) {
//            pq.add(node.next) //adding the next latest tweer to pq
//        }
//       }
//
//
//   }
