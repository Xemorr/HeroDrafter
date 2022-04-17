package me.xemor.herodrafter.cache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.xemor.herodrafter.Player;
import net.dv8tion.jda.api.JDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

// threadsafe LRU cache
public class PlayerNameCache implements AutoCloseable {

    private final boolean persistent;
    private final File cacheFile = new File("names.json");
    private final int maxSize;
    private final Map<Long, PlayerName> fastAccess;
    private final Deque<PlayerName> deque;

    // wtf why cant i have a method to get rid of copy pasta code
    public PlayerNameCache(int maxSize) {
        if (maxSize < 1) {
            maxSize = Integer.MAX_VALUE;
        }
        deque = new ArrayDeque<>(maxSize);
        fastAccess = new HashMap<>(maxSize);
        this.maxSize = maxSize;
        this.persistent = false;
    }

    public PlayerNameCache(int maxSize, boolean persistent, JDA jda) {
        if (maxSize < 1) {
            maxSize = Integer.MAX_VALUE;
        }
        deque = new ArrayDeque<>(maxSize);
        fastAccess = new HashMap<>(maxSize);
        this.maxSize = maxSize;
        this.persistent = persistent;
        if (persistent) {
            initPersistent(jda);
        }
    }
    // I fucking hate checked exceptions
    private void initPersistent(JDA jda) {
        try {
            if (cacheFile.createNewFile()) {
                System.out.println("Created persistent cache file");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //hashmap unordered so no need to remove at the beginning
    public synchronized void cache(PlayerName name) {
        deque.remove(name);
        if (deque.size() >= maxSize) {
            fastAccess.remove(deque.removeFirst().getId());
        }
        deque.addLast(name);
        fastAccess.put(name.getId(), name);
    }
    // Optional rather than null seems to be the trend in this codebase
    public synchronized Optional<PlayerName> getUser(long id) {
        var user = fastAccess.get(id);
        if (user == null) {
           return Optional.empty();
        }
        return Optional.of(user);
    }

    public synchronized Optional<PlayerName> getUser(Predicate<PlayerName> predicate) {
        return deque.stream().filter(predicate).findAny();
    }

    @Override
    public void close() {
        try {
            if (persistent) {
                if (cacheFile.createNewFile()) { //  in case somehow, its just magically deleted??????
                    System.out.println("Created persistent cache file");
                }
                try (final FileWriter writer = new FileWriter(cacheFile)) {
                    final List<PlayerName> namesSer = new ArrayList<>();
                    deque.iterator().forEachRemaining(namesSer::add);
                    writer.write(new Gson().toJson(namesSer));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
