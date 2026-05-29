import React, { useEffect, useState } from 'react'
import { cancelOrder, getOrder, getPendingOrders, fulfillOrder } from '../services/OrderService'

/**
 * Staff-facing UC4 order queue for viewing and fulfilling pending orders.
 */
const OrderQueueComponent = () => {
    const [orders, setOrders] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [successMessage, setSuccessMessage] = useState('')
    const [selectedOrder, setSelectedOrder] = useState(null)
    const [detailsLoading, setDetailsLoading] = useState(false)
    const [showCancelModal, setShowCancelModal] = useState(false)
    const [orderToCancel, setOrderToCancel] = useState(null)

    const role = sessionStorage.getItem('role') || localStorage.getItem('role') || ''

    useEffect(() => {
        fetchPendingOrders()
    }, [])

    /**
     * Loads the pending order queue for staff.
     */
    function fetchPendingOrders() {
        setLoading(true)
        getPendingOrders()
            .then((response) => {
                setOrders(response.data)
                setError('')
            })
            .catch((err) => {
                console.error(err)
                setError('Failed to load pending orders.')
            })
            .finally(() => setLoading(false))
    }

    /**
     * Loads full details for a selected order.
     *
     * @param {number} id order ID
     */
    function handleViewOrder(id) {
        setDetailsLoading(true)
        getOrder(id)
            .then((response) => {
                setSelectedOrder(response.data)
                setError('')
            })
            .catch((err) => {
                console.error(err)
                setError(err?.response?.data?.error || 'Failed to load order details.')
            })
            .finally(() => setDetailsLoading(false))
    }

    /**
     * Marks an order as fulfilled and removes it from the pending queue.
     *
     * @param {number} id order ID
     */
    function handleFulfillOrder(id) {
        fulfillOrder(id)
            .then(() => {
                setOrders((prevOrders) => prevOrders.filter((order) => order.id !== id))
                setSuccessMessage(`Order #${id} has been fulfilled!`)
                setError('')

                if (selectedOrder?.id === id) {
                    setSelectedOrder(null)
                }
            })
            .catch((err) => {
                console.error(err)
                setError(err?.response?.data?.error || 'Insufficient inventory to fulfill this order. Please cancel this order.')
            })
    }

    /**
     * Opens the cancel confirmation modal for a selected order.
     *
     * @param {number} id order ID
     */
    function openCancelModal(id) {
        setOrderToCancel(id)
        setShowCancelModal(true)
        setError('')
    }

    /**
     * Closes the cancel confirmation modal.
     */
    function closeCancelModal() {
        setShowCancelModal(false)
        setOrderToCancel(null)
    }

    /**
     * Confirms order cancellation and removes it from the pending queue.
     */
    function confirmCancelOrder() {
        if (!orderToCancel) {
            return
        }

        cancelOrder(orderToCancel)
            .then(() => {
                setOrders((prevOrders) => prevOrders.filter((order) => order.id !== orderToCancel))
                setSuccessMessage(`Order #${orderToCancel} has been cancelled!`)
                setError('')

                if (selectedOrder?.id === orderToCancel) {
                    setSelectedOrder(null)
                }

                closeCancelModal()
            })
            .catch((err) => {
                console.error(err)
                setError(err?.response?.data?.error || 'Failed to cancel order.')
                closeCancelModal()
            })
    }

    /**
     * Formats ordered items for the detail view.
     *
     * @param {Array} items ordered items
     * @returns {string} item summary
     */
    function formatItems(items) {
        if (!Array.isArray(items) || items.length === 0) {
            return 'No items'
        }

        return items.map((item) => `${item.quantity}x ${item.name}`).join(', ')
    }

    if (!role.includes('STAFF')) {
        return <h2 className="text-center mt-5">Unauthorized</h2>
    }

    if (selectedOrder) {
        return (
            <div className="container">
                <br />
                <button
                    type="button"
                    className="btn btn-secondary btn-sm mb-4"
                    onClick={() => setSelectedOrder(null)}
                >
                    Back To Orders
                </button>

                <h2 className="text-center mb-4">Order #{selectedOrder.id}</h2>

                <div className="mx-auto" style={{ maxWidth: '420px' }}>
                    <p><strong>Customer:</strong> {selectedOrder.customerName}</p>
                    <p><strong>Items Ordered:</strong> {formatItems(selectedOrder.items)}</p>
                    <p><strong>Subtotal:</strong> ${Number(selectedOrder.subtotal || 0).toFixed(2)}</p>
                    <p><strong>Tax:</strong> ${Number(selectedOrder.tax || 0).toFixed(2)}</p>
                    <p><strong>Tip:</strong> ${Number(selectedOrder.tip || 0).toFixed(2)}</p>
                    <p><strong>Total:</strong> ${Number(selectedOrder.total || 0).toFixed(2)}</p>
                </div>
            </div>
        )
    }

    return (
        <div className="container">
            <br />
            <h2 className="text-center mb-0">Orders</h2>
            <p className="text-center mb-3" style={{ fontSize: '0.85rem' }}>
                Click on Order ID to see more details.
            </p>

            {successMessage && (
                <div className="text-center text-success mb-3">{successMessage}</div>
            )}

            {error && (
                <div className="alert alert-danger text-center" role="alert">
                    {error}
                </div>
            )}

            {loading ? (
                <div className="text-center">Loading orders...</div>
            ) : (
                <div className="table-responsive">
                    <table className="table table-bordered text-center align-middle">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Customer Name</th>
                                <th>Total</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((order) => (
                                <tr key={order.id}>
                                    <td>
                                        <button
                                            type="button"
                                            className="btn btn-link p-0"
                                            onClick={() => handleViewOrder(order.id)}
                                        >
                                            #{order.id}
                                        </button>
                                    </td>
                                    <td>{order.customerName}</td>
                                    <td>{Number(order.total || 0).toFixed(2)}</td>
                                    <td>{order.status}</td>
                                    <td>
                                        <button
                                            type="button"
                                            className="btn btn-warning"
                                            onClick={() => handleFulfillOrder(order.id)}
                                        >
                                            Fulfill Order
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-danger ms-2"
                                            onClick={() => openCancelModal(order.id)}
                                        >
                                            Cancel
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>

                    {!detailsLoading && orders.length === 0 && (
                        <div className="text-center mt-5">
                            <h3>No Pending Orders!</h3>
                            <p className="text-muted">Come back when a customer places an order.</p>
                            {successMessage && <p className="text-success">{successMessage}</p>}
                        </div>
                    )}
                </div>
            )}

            {showCancelModal && (
                <div
                    className="modal show"
                    style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }}
                    aria-modal="true"
                    role="dialog"
                >
                    <div className="modal-dialog">
                        <div className="modal-content text-center">
                            <div className="modal-header">
                                <h4 className="modal-title">Cancel Order</h4>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to cancel this order?</p>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-light" onClick={closeCancelModal}>
                                    No
                                </button>
                                <button className="btn btn-primary" onClick={confirmCancelOrder}>
                                    Yes
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default OrderQueueComponent
