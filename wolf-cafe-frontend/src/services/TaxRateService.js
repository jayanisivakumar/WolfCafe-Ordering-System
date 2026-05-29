/**
 * Service for handling Tax Rate API calls.
 * Provides methods to retrieve and update the system tax rate.
 * 
 * @author Jayani Sivakumar
 */

import axios from "axios";
import { getAuthToken } from './AuthService'

const API_BASE_URL = "http://localhost:8080/api/taxrate";

/**
 * Retrieves authorization header with stored JWT token.
 * 
 * @returns Authorization header object
 */
const getAuthHeader = () => {
  const token = getAuthToken();

  if (!token) {
    return {};
  }

  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  };
};

/**
 * Fetches the current tax rate from backend.
 * 
 * @returns Axios GET request
 */
export const getTaxRate = () => {
  return axios.get(API_BASE_URL, getAuthHeader());
};

/**
 * Updates the tax rate in the backend.
 * 
 * @param {number} rate New tax rate value
 * @returns Axios PUT request
 */
export const updateTaxRate = (rate) => {
  return axios.put(API_BASE_URL, { rate }, getAuthHeader());
};
