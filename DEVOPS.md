# Part I — DevOps

The stack contains Kafka, Config Server, Eureka, Account, Transaction, Chatbot, Gateway and the React frontend.

Keep the OpenAI key only in the ignored root `.env` file:

```properties
AI_API_KEY=your-key
```

Start or stop the complete stack:

```bash
docker compose up --build
docker compose down
```

The frontend is available at `http://localhost:5173`, Gateway at `http://localhost:8080`, and Eureka at `http://localhost:8761`.

`.github/workflows/ci-cd.yml` packages every application without tests, validates Compose, builds all images, and publishes them to GitHub Container Registry for pushes to `main` and `v*` tags.
