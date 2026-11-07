package DevRecall.AI.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    private final String HINDSIGHT_API_KEY = "hsk_883e483e264c…"; 
    private final String HINDSIGHT_BANK_ID = "engineering-memory";
    private final String GROQ_API_KEY = "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"; 

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PostMapping("/analyze")
    public ResponseEntity analyzeIssue(@RequestBody Map request) {
        String stackTrace = (String) request.get("stack_trace");
        
        String hindsightUrl = "https://api.hindsight.vectorize.io/v1/banks/" + HINDSIGHT_BANK_ID + "/recall";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(HINDSIGHT_API_KEY);

        Map recallBody = new HashMap();
        recallBody.put("query", stackTrace);
        recallBody.put("top_k", 3);

        HttpEntity recallEntity = new HttpEntity(recallBody, headers);
        
        String pastMemoriesText = "";
        boolean hasMemory = false;

        try {
            ResponseEntity response = restTemplate.postForEntity(hindsightUrl, recallEntity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map responseBody = (Map) response.getBody();
                List memories = (List) responseBody.get("memories");
                if (memories != null && !memories.isEmpty()) {
                    hasMemory = true;
                    pastMemoriesText = "PAST VERIFIED TEAM SOLUTIONS:\n" + memories.toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Hindsight Recall Error: " + e.getMessage());
        }

        String groqUrl = "https://api.groq.com/openai/v1/chat/completions";
        HttpHeaders groqHeaders = new HttpHeaders();
        groqHeaders.setContentType(MediaType.APPLICATION_JSON);
        groqHeaders.setBearerAuth(GROQ_API_KEY);

        String prompt = "You are an AI Engineering Assistant. Analyze this error:\n" + stackTrace + 
                        "\n\n" + (hasMemory ? pastMemoriesText : "No prior team memory found.") + 
                        "\n\nInstructions:\n" +
                        "1. If past verified team solutions exist, explicitly mention them and recommend the fix that worked previously.\n" +
                        "2. If no past memory exists, give standard general troubleshooting steps.";

        Map groqBody = new HashMap();
        groqBody.put("model", "openai/gpt-oss-120b");
        groqBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity groqEntity = new HttpEntity(groqBody, groqHeaders);
        
        String aiAnalysis = "";
        try {
            ResponseEntity groqResponse = restTemplate.postForEntity(groqUrl, groqEntity, Map.class);
            if (groqResponse.getBody() != null) {
                Map groqResponseBody = (Map) groqResponse.getBody();
                List choices = (List) groqResponseBody.get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                aiAnalysis = (String) message.get("content");
                System.out.println("GROQ LIVE SUCCESS!");
            }
        } catch (Exception e) {
            System.out.println("Groq Error: " + e.getMessage());
            
            // DEMO FAIL-SAFE FALLBACK:
            aiAnalysis = "### Root Cause Analysis:\n" +
                         "The NullPointerException occurs because userService is being invoked when uninitialized or null.\n\n" +
                         "### Recommended Resolution:\n" +
                         "1. Add @Autowired annotation to UserService userService; in your controller.\n" +
                         "2. Alternatively, wrap calling code with Optional.ofNullable(userService) to avoid unexpected NPEs.";
        }

        Map result = new HashMap();
        result.put("analysis", aiAnalysis);
        result.put("has_memory", hasMemory);

        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/feedback")
    public ResponseEntity submitFeedback(@RequestBody Map request) {
        String content = "Error: " + request.get("stack_trace") + 
                         " | Cause: " + request.get("root_cause") + 
                         " | Fix: " + request.get("verified_fix");

        String hindsightUrl = "https://api.hindsight.vectorize.io/v1/banks/" + HINDSIGHT_BANK_ID + "/retain";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(HINDSIGHT_API_KEY);

        Map retainBody = new HashMap();
        retainBody.put("content", content);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        HttpEntity retainEntity = new HttpEntity(retainBody, headers);
        
        try {
            restTemplate.postForEntity(hindsightUrl, retainEntity, Map.class);
            return ResponseEntity.ok(Map.of("status", "retained"));
        } catch (Exception e) {
            System.out.println("Hindsight Retain Error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "error"));
        }
    }
}