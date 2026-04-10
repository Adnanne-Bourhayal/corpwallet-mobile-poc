from fastapi import FastAPI
from app.api.routes import router

app = FastAPI(title="CorpWallet Mobile PoC API")

app.include_router(router)

@app.get("/")
def root():
    return {"message": "CorpWallet API running"}
