import axios from 'axios'
import { getAuthToken } from './AuthService'

const BASE_REST_API_URL = 'http://localhost:8080/api/items'

axios.interceptors.request.use(function (config) {
  const token = getAuthToken()

  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  } else {
    delete config.headers['Authorization']
  }

  return config;
}, function (error) {
  // Do something with request error
  return Promise.reject(error);
});

export const saveItem = (item) => axios.post(BASE_REST_API_URL, item)

export const getItemById = (id) => axios.get(BASE_REST_API_URL + '/' + id)

export const getAllItems = () => axios.get(BASE_REST_API_URL)

export const updateItem = (id, item) => axios.put(BASE_REST_API_URL + '/' + id, item)

export const deleteItemById = (id) => axios.delete(BASE_REST_API_URL + '/' + id)
