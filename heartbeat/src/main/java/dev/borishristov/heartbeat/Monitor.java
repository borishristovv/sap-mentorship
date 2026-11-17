
package dev.borishristov.heartbeat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "monitors")
public class Monitor {

    @Id
    private String id;

    private String url;
    private boolean enabled;

    public Monitor(String url, boolean enabled) {
        this.url = url;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
