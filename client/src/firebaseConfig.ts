import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
    apiKey: "AIzaSyCGA9XRj4FUKTdAvoFyb4gwya0ai0I8ttQ",
    authDomain: "goop-house-submissions.firebaseapp.com",
    projectId: "goop-house-submissions",
    storageBucket: "goop-house-submissions.appspot.com",
    messagingSenderId: "488042674664",
    appId: "1:488042674664:web:9c0eae59e7b0acadc3cc5d"
  };

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);