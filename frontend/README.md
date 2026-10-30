# ADRIA Bank Frontend

Minimal React client for the account, transaction, transfer and chatbot APIs exposed by Gateway.

```bash
npm install
npm run dev
```

The application runs at `http://localhost:5173`. During development, Vite proxies `/api` to the
Gateway at `http://localhost:8080`, avoiding a separate browser CORS dependency. Set
`VITE_API_URL` only for a production deployment that uses a different Gateway URL.
