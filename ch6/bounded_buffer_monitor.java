import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer<T> {
    private final T[] buffer;
    private int count;      // Number of items in buffer
    private int in;         // Index for next put
    private int out;        // Index for next get
    
    private final Lock lock;
    private final Condition notFull;   // Signaled when buffer is not full
    private final Condition notEmpty;  // Signaled when buffer is not empty
    
    @SuppressWarnings("unchecked")
    public BoundedBuffer(int capacity) {
        buffer = (T[]) new Object[capacity];
        count = 0;
        in = 0;
        out = 0;
        
        lock = new ReentrantLock();
        notFull = lock.newCondition();
        notEmpty = lock.newCondition();
    }
    
    /**
     * Puts an item into the buffer.
     * Blocks if buffer is full until space becomes available.
     */
    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            // Wait while buffer is full
            while (count == buffer.length) {
                notFull.await();  // Release lock and wait
            }
            
            // Add item to buffer
            buffer[in] = item;
            in = (in + 1) % buffer.length;
            count++;
            
            // Signal that buffer is not empty
            notEmpty.signal();
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Gets an item from the buffer.
     * Blocks if buffer is empty until an item becomes available.
     */
    public T get() throws InterruptedException {
        lock.lock();
        try {
            // Wait while buffer is empty
            while (count == 0) {
                notEmpty.await();  // Release lock and wait
            }
            
            // Remove item from buffer
            T item = buffer[out];
            buffer[out] = null;  // Help GC
            out = (out + 1) % buffer.length;
            count--;
            
            // Signal that buffer is not full
            notFull.signal();
            
            return item;
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Returns the current number of items in the buffer.
     */
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
    
    // Test/Demo code
    public static void main(String[] args) {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(5);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.put(i);
                    System.out.println("Produced: " + i + " (size: " + buffer.size() + ")");
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Thread.sleep(300);  // Consume slower than produce
                    int item = buffer.get();
                    System.out.println("Consumed: " + item + " (size: " + buffer.size() + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Done!");
    }
}