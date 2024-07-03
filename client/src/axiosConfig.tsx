import axios from 'axios';

const instance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'https://submit.goop.house',
  withCredentials: true
});

export default instance;