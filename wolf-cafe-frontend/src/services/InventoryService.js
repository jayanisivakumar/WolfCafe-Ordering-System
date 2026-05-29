import axios from "axios"
import { getAuthToken } from "./AuthService"

/** Base URL for the Inventory API - Correspond to methods in Backend's InventoryController. */
const REST_API_BASE_URL = "http://localhost:8080/api/inventory"

const getAuthHeader = () => {
  const token = getAuthToken()

  return token
    ? { headers: { Authorization: `Bearer ${token}` } }
    : {}
}

/** GET Inventory - returns all inventory */
export const getInventory = () => axios.get(REST_API_BASE_URL, getAuthHeader())

/** PUT Inventory - updates the inventory */
export const updateInventory = (inventory) => axios.put(REST_API_BASE_URL, inventory, getAuthHeader())
