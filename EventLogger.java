import javax.swing.JTextArea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventLogger {
    private final JTextArea logArea;

    public EventLogger(JTextArea logArea) {
        this.logArea = logArea;
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // Legg til emojis for fornøyde/sinte kunder
        if (message.contains("happy")) {
            message = message.replace("happy", "happy 😊");
        } else if (message.contains("angry")) {
            message = message.replace("angry", "angry 😣");
        }
        String logMessage = "[" + timestamp + "] " + message + "\n";
        synchronized (logArea) {
            logArea.append(logMessage);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
}