import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Assumes Spring Boot is running on 8080
  withCredentials: true // Important for OAuth2 session cookies
});

export const checkAuth = async () => {
  try {
    const res = await api.get('/user/me');
    return res.data && Object.keys(res.data).length > 0 ? res.data : null;
  } catch (error) {
    return null;
  }
};

export const getRepositories = async () => {
    const res = await api.get('/repos');
    return res.data; // Return dynamic data directly from DB
};

export const toggleRepositoryReview = async (id: number) => {
    const res = await api.put(`/repos/${id}/toggle`);
    return res.data;
};

export const addRepository = async (fullName: string, webhookSecret: string) => {
    const res = await api.post('/repos', { fullName, webhookSecret });
    return res.data;
};
