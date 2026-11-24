# From proposal to decision

The Android desk requests all expenses from FastAPI. A new proposal contains a supplier, a purpose, an amount and the reviewer's chosen destination type. Validation accepts a positive euro amount with up to two decimal places. Clients cannot supply a status or a decision reason.

The service applies the destination policy as soon as a request enters. Supplier invoices go into the review queue. A crypto-wallet destination receives a final decline and the reason is shown in the log. Approval checks the destination a second time. A reviewer can choose between the permitted decisions only while the expense is pending.

The store holds all sample expenses in the memory of a **single** API worker. A lock makes the check and update one operation. Repeating the same final decision is safe. An opposing decision returns HTTP 409. This sample does not survive a server restart or coordinate multiple workers.

The Android document picker exports a separate JSON containing the app name, a format version, the source and build flags. No expense amounts, supplier names, server addresses, account details or device IDs are written. The companion interprets two known settings and leaves the remaining controls unknown.
