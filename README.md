# CorpWallet

**Secure Mobile Transaction Approval System with Runtime Integrity Verification**

CorpWallet is a mobile-first proof of concept designed to demonstrate how sensitive financial approvals can be protected through **runtime-aware security controls**.

Instead of relying only on username/password authentication, CorpWallet introduces a **trust-based approval model**. Before allowing high-value operations, the system evaluates the integrity of the execution environment and applies backend approval policies accordingly.

---

## 1. Overview

CorpWallet simulates a secure enterprise treasury workflow where users can:

- access a protected mobile workspace
- review pending financial transactions
- inspect transaction risk
- approve or reject operations
- validate device trust before critical actions are allowed

The core idea is simple:

> **A valid user should not be enough if the runtime environment is not trusted.**

This project combines a mobile client, a backend policy engine, and runtime integrity checks into one approval flow.

---

## 2. Problem Statement

Traditional enterprise approval systems usually focus on:

- user identity
- credentials
- session tokens
- sometimes MFA

That model is not always enough.

If an attacker gains access to a session in an untrusted environment, the system may still approve sensitive actions unless additional runtime protections are enforced.

Common risks include:

- debugger-attached sessions
- emulator-based analysis
- rooted devices
- runtime manipulation
- approval attempts from low-trust environments

For financial and enterprise workflows, this creates a major gap between **authentication** and **execution trust**.

---

## 3. Solution

CorpWallet applies a **runtime trust score** before sensitive approvals are executed.

The system evaluates runtime conditions such as:

- debugger detection
- emulator detection
- root detection

These signals are converted into a trust score.  
That score is then checked against backend policy rules before a transaction is approved.

This means the solution is not based only on:

- who the user is

but also on:

- **where**
- **how**
- **under what runtime conditions**
the action is being executed

---

## 4. Architecture

CorpWallet uses a simple client-server model:

- **Android mobile app**
  - Kotlin
  - XML layouts
  - transaction review UI
  - trust-aware approval flow

- **Backend API**
  - FastAPI
  - policy enforcement
  - pending transactions
  - approve/reject endpoints
  - demo reset endpoint

- **Security layer**
  - runtime integrity checks
  - trust score calculation
  - approval threshold enforcement

### Diagram 
<img width="1750" height="797" alt="diagram" src="https://github.com/user-attachments/assets/ef76005a-a601-475c-a3ef-3647d5c4284d" />

## 5. Secure Approval Flow

The core approval workflow follows these security stages:

1.  **User Authentication**: User signs into the application.
2.  **Data Loading**: The app fetches and loads pending transactions.
3.  **Transaction Inspection**: User opens specific transaction details.
4.  **Integrity Check**: The app evaluates **runtime integrity** to ensure the environment is secure.
5.  **Risk Analysis**: The app computes a **trust score** based on device and session signals.
6.  **Backend Request**: An approval request is sent to the backend.
7.  **Threshold Validation**: The backend validates the trust score against the required **trust threshold**.
8.  **Final Resolution**: The transaction is either **approved** or **blocked**.

### Diagram
<img width="901" height="1071" alt="diagram_2" src="https://github.com/user-attachments/assets/75bc131a-14a6-41a9-a53a-09d8fdbd65b0" />

## 6. Security Model

CorpWallet demonstrates a simple but effective security posture model based on real-time threat detection.

### Runtime Signals
The mobile layer continuously inspects runtime conditions, specifically searching for:
*   **Debugger attached**: Detects if a debugger is actively probing the application.
*   **Emulator environment**: Identifies if the app is running on virtualized hardware.
*   **Rooted device indicators**: Checks for unauthorized administrative access (root/jailbreak).

### Trust Score
Security signals directly impact the transaction's trust score through a penalty system:
*   **Debugger**: Major penalty (highest risk).
*   **Emulator**: Medium penalty.
*   **Root**: Medium penalty.

### Policy Enforcement
The backend acts as the final gatekeeper, authorizing sensitive actions **only** if the calculated trust score remains above the defined security threshold.

### Diagram Placement
<img width="562" height="1081" alt="diagram_3" src="https://github.com/user-attachments/assets/dc24bc98-01f3-48d5-9ab0-c2fa3d579698" />

## 7. Why This Matters

This project demonstrates a critical principle in modern security-focused systems:

> **Authentication alone is not enough for sensitive operations.**

A truly secure system must evaluate the complete context before granting authorization:
*   **Execution context**: Where and how the action is being performed.
*   **Device integrity**: The underlying health of the hardware.
*   **Runtime posture**: Real-time detection of active threats (debuggers, hooks).
*   **Moment-of-action conditions**: Validating security status at the exact time of approval.

This approach is essential for **enterprise finance**, **treasury workflows**, and any **mobile-first enterprise tool** where protecting the runtime environment is as vital as the user's password.

## 8. Key Features

### 🔐 Secure Access Flow
*   Branded welcome and secure login screens.
*   Demo access support for quick presentations.

### 📊 Enterprise Dashboard
*   Budget overview and allocation visibility.
*   Security status preview and **trust posture** visibility.

### 💼 Transactions Workspace
*   Pending approvals queue.
*   Risk-aware review model within the transaction detail flow.

### 🛡️ Security Workspace
*   Real-time **Trust Score** presentation.
*   Live status of debugger, emulator, and root detection.
*   Clear explanations of active runtime policies.

### ⚙️ Settings Workspace
*   Profile and account structure.
*   Session state visibility and realistic enterprise layout.

### 🛡️ Backend Protection
*   **Runtime-aware approval gate**: Only allows actions from healthy environments.
*   Automatic blocking of approvals under restricted conditions.
*   Demo reset flow for consistent testing and presentation.

## 9. Screens

1.  **Welcome & Login**: Initial user experience.
2.  **Dashboard**: The central hub with security indicators.
3.  **Transactions**: The queue of pending tasks.
4.  **Transaction Detail**: The flow before approval.
5.  **Security**: The breakdown of the Trust Score.
6.  **Settings**: Profile and session management.
7.  **Approval States**: Comparison between "Approval Blocked" (unsecure) and "Approval Success" (secure).



| Screenshot 1 | Screenshot 2 | Screenshot 3 | Screenshot 4 |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/6452c7c2-9a18-4b28-9a81-10fcbafc483c" width="200"> | <img src="https://github.com/user-attachments/assets/8a535b12-8568-4abd-8f9b-739b09d7b8f9" width="200"> | <img src="https://github.com/user-attachments/assets/3b1182c5-7638-4e54-a610-a3c4a7ba1b71" width="200"> | <img src="https://github.com/user-attachments/assets/78f928dc-d6f0-48a1-b1ad-be69512c9932" width="200"> |



## 10. Tech Stack

### Mobile
*   **Kotlin**: Primary language for robust Android development.
*   **Android XML Layouts**: Native UI implementation for an enterprise feel.
*   **OkHttp**: Reliable networking for backend communication.

### Backend
*   **Python**: Logic and data processing.
*   **FastAPI**: Modern, high-performance web framework for the API layer.

### Security Logic
*   **Threat Detection**: Built-in debugger, emulator, and root detection.
*   **Risk Engine**: Real-time trust score computation.
*   **Policy Engine**: Enforcement of approvals based on environment integrity.

## 11. Project Positioning

CorpWallet is a **security-oriented enterprise approval PoC**. 

It is not just a UI demo; it is a **solution concept** that demonstrates how runtime security signals can be seamlessly integrated into critical business approvals. This makes it highly relevant for:

*   **Solution & Security Engineering**: Architecture of trusted systems.
*   **Mobile Security**: Real-world application of RASP principles.
*   **FinTech Platforms**: Protecting high-value transactions.
*   **Enterprise Design**: Building tools that balance UX with hard security.

## 12. Roadmap

Future improvements planned for the ecosystem:

*   [ ] **Biometric Integration**: Real FaceID/Fingerprint flow.
*   [ ] **Hardware Security**: Keystore-backed secrets and device binding.
*   [ ] **Compliance & Audit**: Full backend audit trail for every action.
*   [ ] **Access Control**: Granular Role-Based Access Control (RBAC).
*   [ ] **Advanced Protection**: Deeper RASP integration and anomaly scoring.
*   [ ] **Policy Engine**: Richer, customizable security rules.

## 13. Author Perspective

This project follows a **solution-oriented approach**:
1.  **Identify** a real business problem (insecure mobile approvals).
2.  **Define** the security gap (lack of runtime context).
3.  **Implement** a realistic model (trust-based authorization).
4.  **Demonstrate** value through a functional, high-fidelity workflow.

The goal is to bridge the gap between technical security design and practical enterprise product presentation.
