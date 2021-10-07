import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Example HTTP SSE client. This opens a given url and starts listening for SSE communication.
 */
public class Client {
    public static void main(String[] args) throws InterruptedException {
        EventHandler eventHandler = new SimpleEventHandler();

        // Without parameters, the server simulates a 'real' environment. The timing is real-time, but shifted to the past.
//        String url = String.format("http://localhost:7000/sse");
        // You can pass 'start' as a query parameter to override the server's sense of time. The first event returned will be at the timestamp you specified.
         String url = String.format("http://localhost:7000/sse?start=1377299204");

        // Example that calls the url and starts listening to events sent by the server.
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url))
                .reconnectTime(Duration.ofMillis(3000));

        try (EventSource eventSource = builder.build()) {
            eventSource.start();
            // listen for 10 minutes, then quit
            TimeUnit.MINUTES.sleep(10);
        }
    }
}
