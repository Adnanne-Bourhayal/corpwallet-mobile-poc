from fastapi import FastAPI

app = FastAPI(title="CorpWallet Mobile PoC API")

@app.get("/health")
def health():
    return {"status": "ok", "service": "corpwallet-api"}

@app.get("/")
def root():
    return {"message": "CorpWallet Mobile PoC backend is running"}
