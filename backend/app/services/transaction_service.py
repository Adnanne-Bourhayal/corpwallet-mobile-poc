from app.models.transaction import Transaction, TransactionStatus

def build_demo_transactions():
    return [
        Transaction(id=1, amount=1200, recipient="Supplier A", risk_score=20, status=TransactionStatus.PENDING),
        Transaction(id=2, amount=8900, recipient="External Partner", risk_score=80, status=TransactionStatus.PENDING),
    ]

TRANSACTIONS = build_demo_transactions()

def get_pending_transactions():
    return [t for t in TRANSACTIONS if t.status == TransactionStatus.PENDING]

def approve_transaction(transaction_id: int, trust_score: int):
    for t in TRANSACTIONS:
        if t.id == transaction_id:
            if trust_score < 50:
                return {"error": "Device not trusted. Approval blocked."}
            t.status = TransactionStatus.APPROVED
            return t
    return {"error": "Transaction not found"}

def reject_transaction(transaction_id: int):
    for t in TRANSACTIONS:
        if t.id == transaction_id:
            t.status = TransactionStatus.REJECTED
            return t
    return {"error": "Transaction not found"}

def reset_transactions():
    global TRANSACTIONS
    TRANSACTIONS = build_demo_transactions()
