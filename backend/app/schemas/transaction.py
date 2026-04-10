from pydantic import BaseModel

class ApproveRequest(BaseModel):
    device_trust_score: int
