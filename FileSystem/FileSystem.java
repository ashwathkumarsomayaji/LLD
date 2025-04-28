package filehost;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class FileEntity {
    String fileName;
    Integer size;
    Long timestamp;
    Long ttl;

    public FileEntity(String fileName, Integer size, Long timestamp, Long ttl) {
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.size = size;
        this.ttl = ttl;
    }
    public FileEntity copyWithFile(String newFileName, Long timestamp) {
        return new FileEntity(newFileName, size, timestamp, null);
    }

    public boolean isAlive(long currentTimeStamp) {
        return ttl == null ||  currentTimeStamp-timestamp < ttl;
    }
}
class Operations {
    long timestamp;
    String type;

    FileEntity fileSnapshot;
    FileEntity targetName;

    public Operations(long timestamp, String type,FileEntity fileSnapshot, FileEntity targetName) {
        this.type = type;
        this.timestamp = timestamp;
        this.fileSnapshot = fileSnapshot;
        this.targetName = targetName;
    }



}
public class FileSystem {
    static Map<String, FileEntity> fileMap = new HashMap<>();
    private List<Operations> history = new ArrayList<>();
    private int currentSize = 0;


    public void FILE_UPLOAD_AT(String fileName, Integer fileSize, long timestamp) {
        if (fileMap.containsKey(fileName)) {
            throw new RuntimeException("File Already Exists");
        }

        FileEntity file = new FileEntity(fileName, fileSize, timestamp, null);
        fileMap.put(fileName, file);
        history.add(new Operations(timestamp, "UPLOAD", file, null));

    }

    public Integer FILE_GET_AT(String fileName, long timestamp) {
        FileEntity file = fileMap.get(fileName);
        if (file != null && file.isAlive(timestamp)) return file.size;
        return null;
    }

    public void FILE_COPY_AT(String source, String destination, long timestamp) {
        FileEntity sourceFile = fileMap.get(source);

        if (sourceFile == null || !sourceFile.isAlive(timestamp)) {
            throw new RuntimeException("Source file doesn't exist");
        }
        FileEntity des = sourceFile.copyWithFile(destination, timestamp);
        fileMap.put(destination, des);
        history.add(new Operations(timestamp, "COPY", des, sourceFile));

    }

    public List<FileEntity> FILE_SEARCH_AT(long timestamp, String prefix) {
        return fileMap.values().stream()
                .filter(f -> f.fileName.startsWith(prefix))
                .sorted((f1, f2) -> {
                    int cmp = Integer.compare(f2.size, f1.size);
                    return cmp != 0 ? cmp : f1.fileName.compareTo(f2.fileName);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    public void ROLLBACK(long toTimeStamp) {
        fileMap.clear();
        currentSize = 0;
        for(Operations op: history){
            if(op.timestamp <= toTimeStamp){
                if(op.type == "UPLOAD" || op.type == "COPY") {
                    fileMap.put(op.fileSnapshot.fileName, op.fileSnapshot);
                    currentSize += op.fileSnapshot.size;
                }
            }
        }
    }

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        fs.FILE_UPLOAD_AT("file-1.zip", 1420, 1);
        fs.FILE_UPLOAD_AT("file-2.zip", 2480, 2);
        fs.FILE_UPLOAD_AT("file-3.zip", 5040, 3);
        fs.FILE_UPLOAD_AT("file-4.zip", 1200, 4);

        System.out.println("GET file-1.zip: " + fs.FILE_GET_AT("file-1.zip", 4));

        fs.FILE_COPY_AT("file-1.zip", "copy-file-1.zip", 5);
        System.out.println("fileMap" + fileMap.toString());


        System.out.println("Search prefix 'file':");
        fs.FILE_SEARCH_AT(7, "file").forEach(f ->
                System.out.println(f.fileName + " - " + f.size));

        fs.ROLLBACK(3);
        System.out.println("After rollback to t=3, file-3.zip: " + fs.FILE_GET_AT("file-3.zip", 3));

    }
}
