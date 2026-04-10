from fastapi import APIRouter
from app.services.transaction_service import *
from app.services.auth_service import login
from app.schemas.transaction import ApproveRequest

router = APIRouter()

@router.post("/auth/login")
def auth_login(username: str, password: str):
    user = login(username, password)
    if not user:
        return {"error": "Invalid credentials"}
    return user

@router.get("/transactions/pending")
def pending_transactions():
    return get_pending_transactions()

@router.post("/transactions/{transaction_id}/approve")
def approve(transaction_id: int, req: ApproveRequest):
    return approve_transaction(transaction_id, req.device_trust_score)

@router.post("/transactions/{transaction_id}/reject")
def reject(transaction_id: int):
    return reject_transaction(transaction_id)

@router.post("/demo/reset")
def reset_demo():
    reset_transactions()
    return {"status": "reset_ok"}
