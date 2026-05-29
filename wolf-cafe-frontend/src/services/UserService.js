import axios from 'axios'

const REST_API_BASE_URL = 'http://localhost:8080/api/users'

export const getAllUsers = () => axios.get(REST_API_BASE_URL)

export const getUserById = (id) => axios.get(`${REST_API_BASE_URL}/${id}`)

export const createStaff = (user) => axios.post(`${REST_API_BASE_URL}/staff`, user)

export const updateUser = (id, user) => axios.put(`${REST_API_BASE_URL}/${id}`, user)

export const deleteUser = (id) => axios.delete(`${REST_API_BASE_URL}/${id}`)