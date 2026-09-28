from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class Submission(BaseModel):
    artist_name1: str
    artist_name2: str | None = None
    artist_name3: str | None = None
    artist_name4: str | None = None
    song_name: str
    user_id: str


submissions = {}   

#implement da crud

#create
@app.post("/submissions")
async def create_submission(submission: Submission):
    submission_id = len(submissions)
    submissions.update({submission_id: submission})

#read
@app.get("/submissions")
async def get_submissions():
    return submissions

#get specific submission
@app.get("/submissions/{submission_id}")
async def get_submission(submission_id: int):
    return submissions.get(submission_id)

#update
@app.patch("/submissions")
async def update_submission(submission_id: int, updatedSubmission: Submission):
    submissions[submission_id] = updatedSubmission

#delete
@app.delete ("/submissions")
async def delete_submission(submission_id: int):
    submissions.pop(submission_id)