import com.solacesystems.jms.SolConnectionFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;
import javax.jms.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class SolaceExclusiveQueueListener implements SessionAwareMessageListener<Message> {

    private final Executor executor;

    public SolaceExclusiveQueueListener() {
        this.executor = Executors.newFixedThreadPool(5); // Adjust the number of threads as needed
    }

    @JmsListener(destination = "your-queue-name", containerFactory = "jmsListenerContainerFactory")
    public void processMessage(Message message) {
        // Message processing logic goes here
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                System.out.println("Received message: " + text);

                // Process the received message asynchronously
                executor.execute(() -> processMessageAsync(message));
            } else {
                System.out.println("Received non-text message: " + message);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void processMessageAsync(Message message) {
        try {
            // Simulate some processing time
            Thread.sleep(1000); // 1 second (adjust as needed)

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                System.out.println("Processing message asynchronously: " + text);

                // Process the received message here asynchronously
            } else {
                System.out.println("Processing non-text message asynchronously: " + message);
            }

            // Acknowledge the message after processing
            message.acknowledge();
        } catch (InterruptedException | JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        // This method is required by SessionAwareMessageListener
        processMessage(message);
    }
}

@Configuration
class SolaceConfig {

    @Bean(name = "jmsListenerContainerFactory")
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                      DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setConcurrency("5"); // Set the concurrency to limit the number of threads
        return factory;
    }
}
