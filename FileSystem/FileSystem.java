package filesystem;


import java.util.*;
/* ───────────────────── Node (Abstract) ───────────────────── */
abstract class Node {
    String name;
    Node(String name) { this.name = name; }
}
/* ───────────────────── Directory ───────────────────── */
class Directory extends Node {
    Map<String, Node> children = new HashMap<>();
    Directory(String name) {
        super(name);
    }
}
/* ───────────────────── File ───────────────────── */
class File extends Node {
    StringBuilder content = new StringBuilder();
    File(String name) {
        super(name);
    }
    void addContent(String data) {
        content.append(data);
    }
    String readContent() {
        return content.toString();
    }
}
/* ───────────────────── FileSystem ───────────────────── */
public class FileSystem {
    private final Directory root;
    public FileSystem() {
        root = new Directory("");
    }
    private Node traverse(String path, boolean create) {
        String[] parts = path.split("/");
        Node cur = root;
        for (int i = 1; i < parts.length; i++) {
            if (!(cur instanceof Directory)) {
                throw new RuntimeException("Invalid path: " + parts[i-1]);
            }
            Directory dir = (Directory) cur;
            if (!dir.children.containsKey(parts[i])) {
                if (create) {
                    dir.children.put(parts[i], new Directory(parts[i]));
                } else {
                    throw new RuntimeException("Path not found: " + path);
                }
            }
            cur = dir.children.get(parts[i]);
        }
        return cur;
    }
    /* mkdir(path) */
    public void mkdir(String path) {
        traverse(path, true);
    }
    /* addContentToFile(path, content) */
    public void addContentToFile(String filePath, String content) {
        String[] parts = filePath.split("/");
        String fileName = parts[parts.length - 1];
        String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        Directory dir = (Directory) traverse(dirPath.isEmpty() ? "/" : dirPath, true);
        Node file = dir.children.get(fileName); //frist time file wont be there.
        //Secnd time with the same directory file path and different content to the same file then
        // mkdir /a/b/c → directories already exist, so just traverse.
        ///a/b/c/children already has file.txt!

        if (file == null) {
            File newFile = new File(fileName);
            newFile.addContent(content);
            dir.children.put(fileName, newFile);
        } else if (file instanceof File) {
            ((File) file).addContent(content);
        } else {
            throw new RuntimeException(fileName + " is a directory");
        }
    }
    /* readContentFromFile(filePath) */
    public String readContentFromFile(String filePath) {
        Node node = traverse(filePath, false);
        if (node instanceof File) {
            return ((File) node).readContent();
        } else {
            throw new RuntimeException(filePath + " is a directory");
        }
    }
    /* ls(path) */
    public List<String> ls(String path) {
        Node node = traverse(path, false);
        if (node instanceof File) {
            return List.of(node.name);
        } else {
            Directory dir = (Directory) node;
            List<String> names = new ArrayList<>(dir.children.keySet());
            Collections.sort(names);
            return names;
        }
    }
}
/* ───────────────────── DEMO / TEST ───────────────────── */
 class Main {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        fs.mkdir("/a/b/c");
        fs.addContentToFile("/a/b/c/file.txt", "Hello ");
        fs.addContentToFile("/a/b/c/file.txt", "World!");
        System.out.println("ls /a/b/c -> " + fs.ls("/a/b/c"));
        System.out.println("readContent /a/b/c/file.txt -> " + fs.readContentFromFile("/a/b/c/file.txt"));
        fs.mkdir("/x/y");
        System.out.println("ls / -> " + fs.ls("/"));
    }
}

