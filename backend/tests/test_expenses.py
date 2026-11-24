import unittest
from concurrent.futures import ThreadPoolExecutor

from fastapi import HTTPException

from app.models.transaction import Destination, ExpenseProposal, TransactionStatus as Status
from app.services.transaction_service import decide, expenses, reset_transactions, submit


class ExpensePolicyTests(unittest.TestCase):
    def setUp(self):
        reset_transactions()

    def test_crypto_is_declined_at_intake_and_cannot_be_approved(self):
        blocked = next(row for row in expenses() if row.id == 3)
        self.assertEqual(blocked.status, Status.REJECTED)
        self.assertIn("crypto wallets", blocked.decision_reason)
        with self.assertRaises(HTTPException) as error:
            decide(3, Status.APPROVED, 100)
        self.assertEqual(error.exception.status_code, 409)
        self.assertEqual([row.id for row in expenses(True)], [1, 2])

    def test_newly_submitted_wallet_is_rejected_before_review(self):
        row = submit(ExpenseProposal(recipient="Sample", amount=17, purpose="Team demo", destination=Destination.CRYPTO))
        self.assertEqual(row.status, Status.REJECTED)
        self.assertNotIn(row.id, [expense.id for expense in expenses(True)])
        invoice = submit(ExpenseProposal(recipient="Sample", amount=17, purpose="Team demo", destination=Destination.INVOICE))
        self.assertEqual(invoice.status, Status.PENDING)

    def test_repeat_and_conflicting_decisions(self):
        self.assertEqual(decide(1, Status.APPROVED), decide(1, Status.APPROVED))
        with self.assertRaises(HTTPException) as error:
            decide(1, Status.REJECTED)
        self.assertEqual(error.exception.status_code, 409)
        self.assertEqual(decide(2, Status.REJECTED).status, Status.REJECTED)

    def test_concurrent_review_has_one_winner(self):
        def attempt(status):
            try:
                return decide(1, status).status
            except HTTPException as error:
                return error.status_code

        with ThreadPoolExecutor(max_workers=2) as pool:
            results = list(pool.map(attempt, [Status.APPROVED, Status.REJECTED]))
        self.assertEqual(results.count(409), 1)

    def test_legacy_restricted_session_preserves_queue(self):
        with self.assertRaises(HTTPException) as error:
            decide(1, Status.APPROVED, 20)
        self.assertEqual(error.exception.status_code, 403)
        self.assertEqual(len(expenses(True)), 2)
