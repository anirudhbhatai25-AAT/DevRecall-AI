# DevRecall-AI
​*DevRecall AI* is an intelligent engineering assistant built to stop dev teams from solving the exact same bugs repeatedly[span_0](start_span)[span_0](end_span). By combining real-time LLM stack trace analysis with persistent team vector memory, DevRecall AI ensures that once a engineer fixes a bug, the entire team gains that solution forever[span_1](start_span)[span_1](end_span).

---

## 🚀 System Architecture

- *Frontend:* Angular (Standalone Architecture, TypeScript)[span_2](start_span)[span_2](end_span)
- *Backend API:* Java 17, Spring Boot REST Controller[span_3](start_span)[span_3](end_span)
- *Vector Memory Engine:* Hindsight Vector DB API (engineering-memory bank)[span_4](start_span)[span_4](end_span)
- *LLM Engine:* Groq Cloud API running openai/gpt-oss-120b[span_5](start_span)[span_5](end_span)

---

## ✨ Key Features

1. *Automated Error Analysis:* Paste Java stack traces or exceptions to get structured root-cause analysis and actionable fixes instantly.
2. *Hindsight Team Memory Indexing:* Engineers can store root causes and verified fixes directly to Hindsight's vector database with a single click.
3. *Contextual Memory Recall:* When any team member encounters a recurring issue, the system queries Hindsight vector memory, retrieves the past solution, and incorporates it into the final recommendation.

---

## 🛠️ Local Setup & Execution

### 1. Backend (Spring Boot)

cd devrecall-ui
npm install
ng serve
```bash
cd backend
./mvnw spring-boot:run
