from fastapi import APIRouter, HTTPException

from app.models.transaction import ExpenseProposal, Transaction, TransactionStatus
from app.schemas.transaction import ApproveRequest
from app.services.transaction_service import decide, expenses, reset_transactions, submit

router = APIRouter()


@router.get("/health")
def health():
    return {"status": "ok", "service": "corpwallet"}


@router.post("/auth/login", deprecated=True)
def retired_login():
    raise HTTPException(410, "This local sample opens without an account.")


@router.post("/transactions", response_model=Transaction, status_code=201)
def create_expense(proposal: ExpenseProposal):
    return submit(proposal)


@router.get("/transactions", response_model=list[Transaction])
def all_expenses():
    return expenses()


@router.get("/transactions/pending", response_model=list[Transaction])
def pending_expenses():
    return expenses(pending_only=True)


@router.post("/transactions/{transaction_id}/approve", response_model=Transaction)
def approve(transaction_id: int, req: ApproveRequest | None = None):
    return decide(transaction_id, TransactionStatus.APPROVED, req.device_trust_score if req else None)


@router.post("/transactions/{transaction_id}/reject", response_model=Transaction)
def reject(transaction_id: int):
    return decide(transaction_id, TransactionStatus.REJECTED)


@router.post("/demo/reset")
def reset_demo():
    reset_transactions()
    return {"status": "reset_ok"}
