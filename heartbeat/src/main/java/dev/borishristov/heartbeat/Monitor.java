
package dev.borishristov.heartbeat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "monitors")
public class Monitor {

    @Id
    private String id;

    private String url;

    public Monitor(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

}
