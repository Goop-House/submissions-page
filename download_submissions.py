import os
import json
import requests
import random

with open('submissions.json', 'r') as file:
    submissions = json.load(file)

os.makedirs('submissions', exist_ok=True)
os.makedirs('submissions/art', exist_ok=True)

def download_file(url, path):
    try:
        response = requests.get(url, stream=True)
        if response.status_code == 200:
            with open(path, 'wb') as file:
                for chunk in response.iter_content(chunk_size=8192):
                    file.write(chunk)
            print(f"Downloaded: {path}")
        else:
            print(f"Failed to download {url} - Status code: {response.status_code}")
            return False
    except Exception as e:
        print(f"Error downloading {url}: {e}")
        return False
    return True

for submission in submissions:
    try:
        artists = [submission['artist_name1'], submission['artist_name2'], submission['artist_name3'], submission['artist_name4']]
        artists = [artist for artist in artists if artist]
        random.shuffle(artists)
        artists_str = ', '.join(artists)

        audio_filename = f"{artists_str} - {submission['song_name']}.mp3"
        audio_path = os.path.join('submissions', audio_filename)
        if not download_file(submission['audioUrl'], audio_path):
            continue

        if submission.get('artworkUrl'):
            artwork_filename = f"{artists_str} - {submission['song_name']}_artwork.jpg"
            artwork_path = os.path.join('submissions/art', artwork_filename)
            if not download_file(submission['artworkUrl'], artwork_path):
                continue 

    except Exception as e:
        print(f"Error processing submission {submission['song_name']}: {e}")
        continue  