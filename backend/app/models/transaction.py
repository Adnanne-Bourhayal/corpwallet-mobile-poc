from pydantic import BaseModel
from enum import Enum

class TransactionStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"

class Transaction(BaseModel):
    id: int
    amount: float
    recipient: str
    risk_score: int
    status: TransactionStatus
