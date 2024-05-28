import java.util.*;
import java.util.concurrent.*;

// Przykładowy "Internet"
class Internet 
{ 
    private static final Map<String, String> urlContentMap = new HashMap<>();
    static 
    {
        urlContentMap.put("http://example.com", "http://example.com/page1\nhttp://example.com/page2");
        urlContentMap.put("http://example.com/page1", "http://example.com/page3\nhttp://example.com/page4");
        urlContentMap.put("http://example.com/page2", "http://example.com/page5");
        urlContentMap.put("http://example.com/page3", "");
        urlContentMap.put("http://example.com/page4", "http://example.com/page6\nhttp://example.org/page1");
        urlContentMap.put("http://example.com/page5", "http://example.com/page6\nhttp://example.com/page7\nhttp://example.net/page1");
        urlContentMap.put("http://example.com/page6", "");
        urlContentMap.put("http://example.com/page7", "");
        urlContentMap.put("http://example.org/page1", "http://example.org/page2\nhttp://example.com/page3");
        urlContentMap.put("http://example.org/page2", "");
        urlContentMap.put("http://example.net/page1", "http://example.net/page2");
        urlContentMap.put("http://example.net/page2", "");
    }
    
    public static String get(String url) 
    {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000));
        } 
        catch(InterruptedException _) {}
        
        return urlContentMap.getOrDefault(url, null);
    }
    public static Set<String> getAllUrls() { return urlContentMap.keySet(); }
}

class Task 
{
    private final String url;
    private final int depth;
    public Task(String url, int depth) 
    {
        this.url = url;
        this.depth = depth;
    }
    public String getUrl() { return url; }
    public int getDepth() { return depth; }
}

class WebCrawlerThread extends Thread 
{
    private final BlockingQueue<Task> queue;
    private final Set<String> visited;
    private final int maxDepth;
    
    public WebCrawlerThread(BlockingQueue<Task> queue, Set<String> visited, int maxDepth) 
    {
        this.queue = queue;
        this.visited = visited;
        this.maxDepth = maxDepth;
    }
    
    @Override
    public void run() 
    {
        while(true)
        {
            Task task;
            
            try {
             task = queue.poll(2, TimeUnit.SECONDS);
            }
            catch(InterruptedException e) { return; }
            
            if(task == null) return; 
            
            if(task.getDepth() > maxDepth)  return; 
            
            String url = task.getUrl();
            if(url == null || url.isEmpty())  continue; 
            
            if(!visited.contains(url))
            {
                visited.add(url);
                System.out.println("Crawled URL: " + url + " at depth: " + task.getDepth());
                String content = Internet.get(url);
                if(content != null)
                {
                    String[] urlsArray = content.split("\n");
                    for(String s : urlsArray)
                    {
                        if(!queue.offer(new Task(s, task.getDepth() + 1)))
                        {
                            System.out.println("The Queue is full");
                        }
                    }
                }
            }
            
        }
    }
}

public class WebCrawler {
    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    private final Set<String> visited = new HashSet<>();
    private final int maxDepth;
    private final int numThreads;
   
    public WebCrawler(int maxDepth, int numThreads) 
    {
        this.maxDepth = maxDepth;
        this.numThreads = numThreads;
    }
    
    public void startCrawling(String startUrl) throws InterruptedException 
    {
        queue.put(new Task(startUrl, 0));
        Thread [] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new WebCrawlerThread(queue, visited, maxDepth);
            threads[i].start();
        }
        for (Thread thread : threads) { thread.join(); }
    }
    
    public Set<String> getVisitedUrls() { return visited; }
    
    public static void main(String[] args) throws InterruptedException 
    {
        WebCrawler webCrawler = new WebCrawler(3, 4);
        webCrawler.startCrawling("http://example.com");
        
        // Weryfikacja
        Set<String> visitedUrls = webCrawler.getVisitedUrls();
        Set<String> allUrls = Internet.getAllUrls();
        
        if (visitedUrls.containsAll(allUrls)) 
        {
            System.out.println("All pages have been successfully crawled.");
        } 
        else 
        {
            Set<String> missedUrls = new HashSet<>(allUrls);
            missedUrls.removeAll(visitedUrls);
            System.out.println("The following pages were not crawled: " + missedUrls);
        }
    }
}
