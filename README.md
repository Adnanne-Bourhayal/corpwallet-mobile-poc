# CorpWallet Mobile PoC

A corporate mobile wallet proof of concept focused on secure approval flows, mobile hardening, and trusted backend behavior.

## Core Idea
CorpWallet Mobile PoC is a small Android treasury approval application built to demonstrate how a sensitive enterprise mobile app can evolve from a functional baseline into a hardened, security-aware product workflow.

## Project Structure
- `android-app/` → Kotlin Android client
- `backend/` → FastAPI backend
- `security-toolkit/` → Python validation/reporting toolkit
- `docs/` → PoC, architecture, checklists, troubleshooting
- `landing/` → Mini landing page for showcase/demo

## Planned MVP
- Corporate login
- Treasury dashboard
- Pending approvals
- Approve / reject flow
- Integrity-aware backend behavior
- Security validation report

## Guardsquare Alignment
This project is intentionally designed around:
- Android/Kotlin mobile protection mindset
- code hardening and obfuscation story
- runtime risk awareness
- security validation workflow
- trusted app / backend interaction model
