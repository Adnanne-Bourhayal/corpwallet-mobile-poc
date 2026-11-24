"""One team expense and the decision that appears in its review log."""
from enum import Enum

from pydantic import BaseModel, ConfigDict, Field


class TransactionStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"


class Destination(str, Enum):
    INVOICE = "supplier_invoice"
    CRYPTO = "crypto_wallet"


class ExpenseProposal(BaseModel):
    """A fixture supplied by a reviewer; decision fields belong to the server."""
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True, allow_inf_nan=False)

    recipient: str = Field(min_length=1, max_length=80)
    purpose: str = Field(min_length=1, max_length=160)
    amount: float = Field(gt=0, le=1_000_000, multiple_of=0.01)
    destination: Destination


class Transaction(ExpenseProposal):
    id: int
    currency: str = "EUR"
    status: TransactionStatus = TransactionStatus.PENDING
    decision_reason: str = ""
    risk_score: int = 0  # Kept for the original Android client's queue view.
