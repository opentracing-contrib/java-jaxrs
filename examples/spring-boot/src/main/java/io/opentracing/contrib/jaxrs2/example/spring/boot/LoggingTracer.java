package io.opentracing.contrib.jaxrs2.example.spring.boot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import java.util.List;
import java.util.Map;

/**
 * Prints reported spans to stdout.
 *
 * @author Pavol Loffay
 */
public class LoggingTracer extends MockTracer {

    private ObjectMapper objectMapper;

    public LoggingTracer() {
        this.objectMapper = createObjectMapper();
    }

    @Override
    protected void onSpanFinished(MockSpan mockSpan) {
        try {
            String json = objectMapper.writeValueAsString(mockSpan);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        objectMapper.addMixIn(MockSpan.class, MockSpanMixin.class);
        objectMapper.addMixIn(MockSpan.MockContext.class, MockSpanContextMixin.class);
        objectMapper.addMixIn(MockSpan.LogEntry.class, LogEntryMixin.class);

        return objectMapper;
    }

    private abstract class MockSpanMixin {
        @JsonProperty("operationName") abstract int operationName();
        @JsonProperty("startMicros") abstract long startMicros();
        @JsonProperty("finishMicros") abstract long finishMicros();
        @JsonProperty("parentId") abstract int parentId();
        @JsonProperty("context") abstract MockSpan.MockContext context();
        @JsonProperty("logs") abstract List<MockSpan.LogEntry> logEntries();
        @JsonProperty("tags") abstract Map<String, Object> tags();
    }

    private abstract class MockSpanContextMixin {
        @JsonProperty("spanId") abstract long spanId();
        @JsonProperty("traceId") abstract long traceId();
    }

    private abstract class LogEntryMixin {
        @JsonProperty("fields") abstract Map<String, ?> fields();
        @JsonProperty("timestampMicros") abstract long timestampMicros();
    }
}
