import axios from 'axios';

const instance = axios.create({
  baseURL: '/',
  withCredentials: true
});

export default instance;