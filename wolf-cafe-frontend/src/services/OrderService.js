/**
 * Service layer for Order-related API calls.
 * Provides methods for retrieving customer orders and
 * acknowledging order pickup.
 *
 * @author Jayani Sivakumar
 */

import axios from 'axios'

const BASE_REST_API_URL = 'http://localhost:8080/api/orders'

/**
 * Retrieves all orders for the currently authenticated customer.
 *
 * @returns {Promise} promise resolving to the customer's orders
 */
export const getMyOrders = () => axios.get(BASE_REST_API_URL + '/my')

/**
 * Retrieves all pending orders for staff fulfillment.
 *
 * @returns {Promise} promise resolving to pending orders
 */
export const getPendingOrders = () => axios.get(BASE_REST_API_URL + '/pending')

/**
 * Retrieves a single order by ID.
 *
 * @param {number} id order ID
 * @returns {Promise} promise resolving to order details
 */
export const getOrder = (id) => axios.get(BASE_REST_API_URL + '/' + id)

/**
 * Places a new order for a customer or anonymous guest.
 *
 * @param {Object} orderPayload order submission payload
 * @returns {Promise} promise resolving to created order response
 */
export const placeOrder = (orderPayload) => axios.post(BASE_REST_API_URL, orderPayload)

/**
 * Marks a pending order as fulfilled.
 *
 * @param {number} id order ID
 * @returns {Promise} promise resolving to updated order response
 */
export const fulfillOrder = (id) =>
    axios.put(BASE_REST_API_URL + '/' + id + '/fulfill')

/**
 * Marks an order as cancelled.
 *
 * @param {number} id order ID
 * @returns {Promise} promise resolving to updated order response
 */
export const cancelOrder = (id) =>
    axios.put(BASE_REST_API_URL + '/' + id + '/cancel')

/**
 * Marks a ready order as picked up.
 *
 * @param {number} id order ID
 * @returns {Promise} promise resolving to updated order response
 */
export const pickupOrder = (id) =>
    axios.put(BASE_REST_API_URL + '/' + id + '/pickup')
