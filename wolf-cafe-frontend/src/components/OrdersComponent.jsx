/**
 * Displays and manages customer orders for UC6: Customer Order Pickup.
 *
 * Features:
 * - Displays active and past orders
 * - Polls backend for order status updates
 * - Shows notification when an order becomes ready
 * - Allows pickup confirmation through a modal
 * - Shows status badges and highlights ready orders
 * - Supports active/history toggle
 * - Displays last updated time
 *
 * @author Jayani Sivakumar
 */

import React, { useEffect, useMemo, useState, useCallback } from 'react'
import { getMyOrders, pickupOrder } from '../services/OrderService'
import { getLoggedInUser } from '../services/AuthService'

const POLL_INTERVAL_MS = 5000
const DISMISSED_CANCELLED_PREFIX = 'dismissedCancelledOrders'

const OrdersComponent = () => {
    const [orders, setOrders] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [toastMessage, setToastMessage] = useState('')
    const [showModal, setShowModal] = useState(false)
    const [selectedOrder, setSelectedOrder] = useState(null)
    const [prevStatuses, setPrevStatuses] = useState({})
    const [view, setView] = useState('ACTIVE')
    const [lastUpdated, setLastUpdated] = useState('')
	const toastTimeoutRef = React.useRef(null)

    const role = sessionStorage.getItem('role') || localStorage.getItem('role') || ''
    const currentUsername = getLoggedInUser()
    const dismissedCancelledStorageKey = `${DISMISSED_CANCELLED_PREFIX}:${currentUsername || 'guest'}`
    const [dismissedCancelledOrders, setDismissedCancelledOrders] = useState(
        JSON.parse(localStorage.getItem(dismissedCancelledStorageKey) || '[]')
    )

    useEffect(() => {
        setDismissedCancelledOrders(
            JSON.parse(localStorage.getItem(dismissedCancelledStorageKey) || '[]')
        )
    }, [dismissedCancelledStorageKey])

    useEffect(() => {
        localStorage.setItem(
            dismissedCancelledStorageKey,
            JSON.stringify(dismissedCancelledOrders)
        )
    }, [dismissedCancelledOrders, dismissedCancelledStorageKey])

    /**
     * Shows a temporary toast-style notification.
     *
     * @param {string} message notification text
     */
	function showToast(message) {
	    setToastMessage(message)

	    if (toastTimeoutRef.current) {
	        clearTimeout(toastTimeoutRef.current)
	    }

	    toastTimeoutRef.current = setTimeout(() => {
	        setToastMessage('')
	    }, 10000)
	}
	
    /**
     * Fetches orders from backend and detects state transitions.
     */
	const fetchOrders = useCallback(() => {
	    getMyOrders()
	        .then((res) => {
	            const newOrders = res.data

	            newOrders.forEach((order) => {
	                if (
	                    prevStatuses[order.id] === 'PENDING' &&
	                    order.status === 'READY_FOR_PICKUP'
	                ) {
	                    showToast(`Your order #${order.id} is ready for pickup!`)
	                }

                    if (
                        prevStatuses[order.id] &&
                        prevStatuses[order.id] !== 'CANCELLED' &&
                        order.status === 'CANCELLED'
                    ) {
                        showToast(`Your order #${order.id} was cancelled by staff.`)
                    }
	            })

	            const nextStatuses = {}
	            newOrders.forEach((order) => {
	                nextStatuses[order.id] = order.status
	            })

	            setPrevStatuses(nextStatuses)
	            setOrders(newOrders)
	            setError('')
	            setLastUpdated(new Date().toLocaleTimeString())
	        })
	        .catch(() => {
	            setError('Failed to load orders.')
	        })
	        .finally(() => setLoading(false))
	}, [prevStatuses])

	useEffect(() => {
	    fetchOrders()

	    const intervalId = setInterval(fetchOrders, POLL_INTERVAL_MS)

	    return () => clearInterval(intervalId)
	}, [fetchOrders])

    /**
     * Opens pickup confirmation modal for an order.
     *
     * @param {Object} order selected order
     */
    function openModal(order) {
        setSelectedOrder(order)
        setShowModal(true)
        setError('')
    }

    /**
     * Closes pickup confirmation modal.
     */
    function closeModal() {
        setShowModal(false)
        setSelectedOrder(null)
    }

    /**
     * Handles pickup confirmation.
     */
    function handlePickup() {
        if (!selectedOrder) {
            return
        }

        pickupOrder(selectedOrder.id)
            .then((res) => {
                const pickedUpId = res.data.id
                setOrders((prev) => prev.filter((order) => order.id !== pickedUpId))
                showToast(`Thank you! Your order #${pickedUpId} has been picked up.`)
                closeModal()
            })
            .catch((err) => {
                console.error(err)
                setError(err?.response?.data?.error || 'Pickup failed.')
                closeModal()
            })
    }

    /**
     * Dismisses a cancelled order from the active customer view.
     *
     * @param {number} orderId cancelled order ID
     */
    function handleDismissCancelledOrder(orderId) {
        setDismissedCancelledOrders((prev) => (
            prev.includes(orderId) ? prev : [...prev, orderId]
        ))
    }

    /**
     * Returns a readable label for order status.
     *
     * @param {string} status backend status
     * @returns {string} display label
     */
    function getStatusLabel(status) {
        if (status === 'READY_FOR_PICKUP') return 'Ready for Pickup'
        if (status === 'PENDING') return 'Pending'
        if (status === 'CANCELLED') return 'Cancelled'
        if (status === 'PICKED_UP') return 'Picked Up'
        return status
    }

    /**
     * Returns a badge element for a given order status.
     *
     * @param {string} status backend status
     * @returns {JSX.Element|string} badge or plain status
     */
    function getStatusBadge(status) {
        if (status === 'PENDING') {
            return <span className="badge bg-secondary">{getStatusLabel(status)}</span>
        }
        if (status === 'READY_FOR_PICKUP') {
            return <span className="badge bg-success">{getStatusLabel(status)}</span>
        }
        if (status === 'CANCELLED') {
            return <span className="badge bg-danger">{getStatusLabel(status)}</span>
        }
        if (status === 'PICKED_UP') {
            return <span className="badge bg-dark">{getStatusLabel(status)}</span>
        }
        return status
    }

    /**
     * Formats order items into a readable comma-separated string.
     *
     * @param {Array} items list of order items
     * @returns {string} formatted description
     */
    function formatItems(items) {
        if (!Array.isArray(items) || items.length === 0) {
            return 'No items'
        }

        return items.map((item) => `${item.name} x${item.quantity}`).join(', ')
    }

    const activeOrders = useMemo(
        () => orders.filter((order) => (
            order.status !== 'PICKED_UP' &&
            !(order.status === 'CANCELLED' && dismissedCancelledOrders.includes(order.id))
        )),
        [orders, dismissedCancelledOrders]
    )

    const pickedUpOrders = useMemo(
        () => orders.filter((order) => order.status === 'PICKED_UP'),
        [orders]
    )

    const displayedOrders = view === 'ACTIVE' ? activeOrders : pickedUpOrders

    if (!role.includes('CUSTOMER')) {
        return <h2 className="text-center mt-5">Unauthorized</h2>
    }

    return (
        <div className="container">
            <br />
            <h2 className="text-center">Orders</h2>

            {toastMessage && (
                <div className="alert alert-info text-center" role="alert">
                    {toastMessage}
                </div>
            )}

            {error && (
                <div className="alert alert-danger text-center" role="alert">
                    {error}
                </div>
            )}

            <div className="d-flex justify-content-center gap-2 mb-3 flex-wrap">
                <button
                    className={`btn ${view === 'ACTIVE' ? 'btn-primary' : 'btn-outline-primary'}`}
                    onClick={() => setView('ACTIVE')}
                >
                    Active Orders
                </button>
                <button
                    className={`btn ${view === 'HISTORY' ? 'btn-secondary' : 'btn-outline-secondary'}`}
                    onClick={() => setView('HISTORY')}
                >
                    Order History
                </button>
                <button
                    className="btn btn-outline-dark"
                    onClick={fetchOrders}
                >
                    Refresh Orders
                </button>
            </div>

            {lastUpdated && (
                <div className="text-center text-muted mb-3">
                    Last updated: {lastUpdated}
                </div>
            )}

            {loading ? (
                <div className="text-center">Loading orders...</div>
            ) : displayedOrders.length === 0 ? (
                <div className="alert alert-light text-center">
                    {view === 'ACTIVE'
                        ? 'You have no active orders.'
                        : 'No picked up orders in your history.'}
                </div>
            ) : (
                <div className="table-responsive">
                    <table className="table table-bordered text-center align-middle">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Description</th>
                                <th>Price</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {displayedOrders.map((order) => (
                                <tr
                                    key={order.id}
                                    id={`order-${order.id}`}
                                    className={order.status === 'READY_FOR_PICKUP' ? 'table-success' : ''}
                                >
                                    <td>#{order.id}</td>
                                    <td>{formatItems(order.items)}</td>
                                    <td>
                                        {typeof order.total === 'number'
                                            ? order.total.toFixed(2)
                                            : Number(order.total || 0).toFixed(2)}
                                    </td>
                                    <td>{getStatusBadge(order.status)}</td>
                                    <td>
                                        {order.status === 'READY_FOR_PICKUP' ? (
                                            <button
                                                className="btn btn-warning"
                                                onClick={() => openModal(order)}
                                            >
                                                Pick Up Order
                                            </button>
                                        ) : order.status === 'CANCELLED' ? (
                                            <div className="d-flex justify-content-center align-items-center gap-2 flex-wrap">
                                                <span className="badge bg-danger p-2">ORDER CANCELLED</span>
                                                <button
                                                    className="btn btn-primary btn-sm"
                                                    onClick={() => handleDismissCancelledOrder(order.id)}
                                                >
                                                    Confirm
                                                </button>
                                            </div>
                                        ) : (
                                            <span />
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {showModal && selectedOrder && (
                <div
                    className="modal show"
                    style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }}
                    aria-modal="true"
                    role="dialog"
                >
                    <div className="modal-dialog">
                        <div className="modal-content text-center">
                            <div className="modal-header">
                                <h4 className="modal-title">Pick Up Order</h4>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to pick up your order?</p>
                                <p className="mb-1"><strong>Order ID:</strong> #{selectedOrder.id}</p>
                                <p className="mb-1"><strong>Items:</strong> {formatItems(selectedOrder.items)}</p>
                                <p className="mb-0">
                                    <strong>Total:</strong>{' '}
                                    {typeof selectedOrder.total === 'number'
                                        ? selectedOrder.total.toFixed(2)
                                        : Number(selectedOrder.total || 0).toFixed(2)}
                                </p>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-secondary" onClick={closeModal}>
                                    Cancel
                                </button>
                                <button className="btn btn-primary" onClick={handlePickup}>
                                    Confirm
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default OrdersComponent
