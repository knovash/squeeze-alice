package org.knovash.squeezealice;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AliceRequest {
    public Meta meta;
    public Session session;
    public Request request;
    public String version;

    // Геттеры и сеттеры
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public static AliceRequest fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, AliceRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        public String locale;
        public String timezone;
        @JsonProperty("client_id")
        public String clientId;
        public Interfaces interfaces;

        // Геттеры и сеттеры
        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public Interfaces getInterfaces() { return interfaces; }
        public void setInterfaces(Interfaces interfaces) { this.interfaces = interfaces; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interfaces {
        public Object screen = new Object();
        public Object payments = new Object();
        @JsonProperty("account_linking")
        public Object accountLinking = new Object();

        public Object getScreen() { return screen; }
        public void setScreen(Object screen) { this.screen = screen; }

        public Object getPayments() { return payments; }
        public void setPayments(Object payments) { this.payments = payments; }

        public Object getAccountLinking() { return accountLinking; }
        public void setAccountLinking(Object accountLinking) { this.accountLinking = accountLinking; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Session {
        @JsonProperty("message_id")
        public int messageId;
        @JsonProperty("session_id")
        public String sessionId;
        @JsonProperty("skill_id")
        public String skillId;
        public User user;
        public Application application;
        @JsonProperty("new")
        public boolean isNew;
        @JsonProperty("user_id")
        public String userId;

        // Геттеры и сеттеры
        public int getMessageId() { return messageId; }
        public void setMessageId(int messageId) { this.messageId = messageId; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }

        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }

        public Application getApplication() { return application; }
        public void setApplication(Application application) { this.application = application; }

        public boolean isNew() { return isNew; }
        public void setNew(boolean aNew) { isNew = aNew; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("user_id")
        public String userId;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Application {
        @JsonProperty("application_id")
        public String applicationId;

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        public String command;
        @JsonProperty("original_utterance")
        public String originalUtterance;
        public Nlu nlu;
        public Markup markup;
        public String type;

        // Геттеры и сеттеры
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }

        public String getOriginalUtterance() { return originalUtterance; }
        public void setOriginalUtterance(String originalUtterance) { this.originalUtterance = originalUtterance; }

        public Nlu getNlu() { return nlu; }
        public void setNlu(Nlu nlu) { this.nlu = nlu; }

        public Markup getMarkup() { return markup; }
        public void setMarkup(Markup markup) { this.markup = markup; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nlu {
        public List<String> tokens = List.of();
        public List<Object> entities = List.of();
        public Map<String, Object> intents = Map.of();

        public List<String> getTokens() { return tokens; }
        public void setTokens(List<String> tokens) { this.tokens = tokens; }

        public List<Object> getEntities() { return entities; }
        public void setEntities(List<Object> entities) { this.entities = entities; }

        public Map<String, Object> getIntents() { return intents; }
        public void setIntents(Map<String, Object> intents) { this.intents = intents; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Markup {
        @JsonProperty("dangerous_context")
        public boolean dangerousContext;

        public boolean isDangerousContext() { return dangerousContext; }
        public void setDangerousContext(boolean dangerousContext) { this.dangerousContext = dangerousContext; }
    }
}