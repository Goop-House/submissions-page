from fastapi import FastAPI, HTTPException, UploadFile
from pydantic import BaseModel

app = FastAPI()

class Submission(BaseModel):
    artist_name1: str
    artist_name2: str | None = None
    artist_name3: str | None = None
    artist_name4: str | None = None
    song_name: str
    user_id: str
    art_path: str | None = None
    song_path: str 

submissions = {}   
counter_id = 0

#implement da crud

#create
@app.post("/submissions")
async def create_submission(submission: Submission, file: UploadFile):
    global counter_id
    if (file.content_type != "wav" & file.content_type != "mp3" & file.content_type != "mpeg"):
        raise HTTPException(status_code = 422, detail = "Invalid audio file type (.wav, .mp3, .mpeg accepted)")
    submissions.update({counter_id: submission})
    new_id = counter_id
    counter_id += 1
    return ("New submission of ID ", new_id, " created with file", file.filename)
    
    

#read
@app.get("/submissions")
async def get_submissions():
    return submissions

#get specific submission
@app.get("/submissions/{submission_id}")
async def get_submission(submission_id: int):
    if (submission_id not in submissions):
        raise HTTPException(status_code = 404, detail = "Submission ID not found")
    return submissions.get(submission_id)

#update
@app.patch("/submissions/{submission_id}")
async def update_submission(submission_id: int, updatedSubmission: Submission, file: UploadFile | None = None):
    if (file.content_type != "wav" & file.content_type != "mp3" & file.content_type != "mpeg"):
        raise HTTPException(status_code = 422, detail = "Invalid audio file type (.wav, .mp3, .mpeg accepted)")
    if (submission_id not in submissions):
        raise HTTPException(status_code = 404, detail = "Submission ID not found")
    submissions[submission_id] = updatedSubmission

#delete
@app.delete ("/submissions/{submission_id}")
async def delete_submission(submission_id: int):
    if (submission_id not in submissions):
        raise HTTPException(status_code = 404, detail = "Submission ID not found")
    submissions.pop(submission_id)