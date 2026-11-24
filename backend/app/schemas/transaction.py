from pydantic import BaseModel, Field


class ApproveRequest(BaseModel):
    """Optional legacy session signal. The server always applies its expense policy."""
    device_trust_score: int | None = Field(default=None, ge=0, le=100)
