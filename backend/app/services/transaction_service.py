"""Single-process, in-memory workspace containing invented expenses only."""
from threading import Lock

from fastapi import HTTPException

from app.models.transaction import Destination, ExpenseProposal, Transaction, TransactionStatus


def apply_intake_policy(expense_id: int, proposal: ExpenseProposal) -> Transaction:
    """Apply policy to the typed destination before a request enters the queue."""
    row = Transaction(id=expense_id, **proposal.model_dump())
    if row.destination == Destination.CRYPTO:
        row.status = TransactionStatus.REJECTED
        row.decision_reason = "Automatically declined: team expenses cannot be paid to crypto wallets."
    return row


def build_demo_transactions() -> dict[int, Transaction]:
    proposals = [
        ExpenseProposal(recipient="Paper & Ink", amount=84.50, purpose="Print workshop handouts", destination=Destination.INVOICE),
        ExpenseProposal(recipient="Northline Rail", amount=126, purpose="Team travel to a workshop", destination=Destination.INVOICE),
        ExpenseProposal(recipient="Demo wallet", amount=240, purpose="Proposed digital asset payment", destination=Destination.CRYPTO),
    ]
    return {i: apply_intake_policy(i, proposal) for i, proposal in enumerate(proposals, start=1)}


_expenses = build_demo_transactions()
_lock = Lock()


def submit(proposal: ExpenseProposal) -> Transaction:
    with _lock:
        expense_id = max(_expenses, default=0) + 1
        row = apply_intake_policy(expense_id, proposal)
        _expenses[expense_id] = row
        return row.model_copy()


def expenses(pending_only: bool = False) -> list[Transaction]:
    with _lock:
        return [row.model_copy() for row in _expenses.values()
                if not pending_only or row.status == TransactionStatus.PENDING]


def decide(expense_id: int, target: TransactionStatus, trust_score: int | None = None) -> Transaction:
    """Apply the decision once. Repeats are safe; conflicting decisions get a conflict."""
    with _lock:
        row = _expenses.get(expense_id)
        if row is None:
            raise HTTPException(404, "Expense not found.")
        if target == TransactionStatus.APPROVED and row.destination == Destination.CRYPTO:
            raise HTTPException(409, row.decision_reason)
        if row.status == target:
            return row.model_copy()
        if row.status != TransactionStatus.PENDING:
            raise HTTPException(409, "This expense already has a final decision.")
        # Older clients send a local demo-session score. It can only restrict approval.
        if target == TransactionStatus.APPROVED and trust_score is not None and trust_score < 50:
            raise HTTPException(403, "This review session is restricted.")
        row.status = target
        row.decision_reason = "Approved by the reviewer." if target == TransactionStatus.APPROVED else "Declined by the reviewer."
        return row.model_copy()


def reset_transactions() -> None:
    with _lock:
        _expenses.clear()
        _expenses.update(build_demo_transactions())
