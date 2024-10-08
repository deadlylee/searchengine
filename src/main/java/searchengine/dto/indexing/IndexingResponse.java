package searchengine.dto.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class IndexingResponse {
    private boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public IndexingResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public IndexingResponse(boolean result) {
        this.result = result;
    }
}
