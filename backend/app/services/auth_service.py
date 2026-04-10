def login(username: str, password: str):
    if username == "admin" and password == "admin":
        return {
            "token": "fake-jwt-token",
            "device_trust_score": 75
        }
    return None
