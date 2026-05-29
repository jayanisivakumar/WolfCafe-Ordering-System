import axios from "axios"
import { getAuthToken } from "./AuthService"

/** Base URL for the Ingredient API - Correspond to methods in Backend's Ingredient Controller. */
const REST_API_BASE_URL = "http://localhost:8080/api/ingredients"

const getAuthHeader = () => {
  const token = getAuthToken()

  return token
    ? { headers: { Authorization: `Bearer ${token}` } }
    : {}
}

/** GET Ingredients - lists all ingredients */
export const listIngredients = () => axios.get(REST_API_BASE_URL, getAuthHeader())

/** POST Ingredient - creates a new ingredient */
export const createIngredient = (ingredient) => axios.post(REST_API_BASE_URL, ingredient, getAuthHeader())

/** GET Ingredient - gets a single ingredient by id */
export const getIngredient = (id) => axios.get(REST_API_BASE_URL + "/" + id, getAuthHeader())

/** DELETE Ingredient - deletes the ingredient with the given id */
export const deleteIngredient = (id) => axios.delete(REST_API_BASE_URL + "/" + id, getAuthHeader())
