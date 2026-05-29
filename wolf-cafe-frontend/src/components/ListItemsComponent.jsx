/**
 * ListItemsComponent displays all menu items and provides functionality
 * for searching, filtering, sorting, and managing items.
 * 
 * Features:
 * - Search items by name
 * - Filter items (all, favorites, affordable, premium)
 * - Sort items (price, name)
 * - Favorite/unfavorite items (stored in localStorage)
 * - Add, update, and delete items (staff only)
 * 
 * This component enhances usability by allowing dynamic item discovery
 * and personalized filtering.
 * 
 * @author Shreeya Wadodkar
 * @author Jayani Sivakumar
 */
import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { deleteItemById, getAllItems } from '../services/ItemService'
import { getTaxRate } from '../services/TaxRateService'
import { placeOrder } from '../services/OrderService'
import { getLoggedInUser, isUserLoggedIn } from '../services/AuthService'
import addToCartIcon from '../../icons/add-to-cart-icon.png'
import shoppingCartIcon from '../../icons/shopping-cart-icon.png'
import deleteIcon from '../../icons/delete-icon.png'

const CART_STORAGE_KEY_PREFIX = 'wolfCafeCart'

const getCustomerCartStorageKey = (username) => `${CART_STORAGE_KEY_PREFIX}:${username}`
const formatIngredientName = (name = '') => name
	.toLowerCase()
	.split(' ')
	.filter(Boolean)
	.map((word) => word.charAt(0).toUpperCase() + word.slice(1))
	.join(' ')

const ListItemsComponent = () => {
	
	const [items, setItems] = useState([])
    const [pageError, setPageError] = useState('')
	const [loading, setLoading] = useState(true)
	const [cartItems, setCartItems] = useState([])
	const [showCart, setShowCart] = useState(false)
	const [showTipSelector, setShowTipSelector] = useState(false)
	const [showOrderSummary, setShowOrderSummary] = useState(false)
	const [taxRate, setTaxRate] = useState(0)
	const [selectedTipType, setSelectedTipType] = useState('NONE')
	const [selectedTipValue, setSelectedTipValue] = useState(0)
	const [customTipInput, setCustomTipInput] = useState('')
	const [customTipError, setCustomTipError] = useState('')
	const [orderMessage, setOrderMessage] = useState('')
	
	const [searchTerm, setSearchTerm] = useState(localStorage.getItem('itemSearch') || '')
    const [filterBy, setFilterBy] = useState(localStorage.getItem('itemFilter') || 'all')
    const [sortBy, setSortBy] = useState(localStorage.getItem('itemSort') || 'default')
    const [favorites, setFavorites] = useState(
        JSON.parse(localStorage.getItem('favoriteItems') || '[]')
    )

	const navigate = useNavigate()

	const role = sessionStorage.getItem('role') || localStorage.getItem('role') || ''
	const currentUsername = getLoggedInUser()
	const isCustomerUser = isUserLoggedIn() && role.includes('CUSTOMER') && currentUsername
	const canManageItems = role.includes('STAFF')
	const canPlaceOrders = !role.includes('STAFF') && !role.includes('ADMIN')
	
	/**
	 * Loads items from backend on component mount.
	 */
	useEffect(() => {
	    listItems()
		fetchTaxRate()
	}, [])

	/**
	 * Loads the correct cart for the current auth state.
	 * Customer carts are restored per user, while anonymous carts stay temporary.
	 */
	useEffect(() => {
		if (isCustomerUser) {
			const savedCart = JSON.parse(
				localStorage.getItem(getCustomerCartStorageKey(currentUsername)) || '[]'
			)
			setCartItems(savedCart)
			return
		}

		setCartItems([])
	}, [currentUsername, isCustomerUser])
	
	/**
	 * Persists search, filter, and sort preferences in localStorage.
	 */
	useEffect(() => {
        localStorage.setItem('itemSearch', searchTerm)
        localStorage.setItem('itemFilter', filterBy)
        localStorage.setItem('itemSort', sortBy)
    }, [searchTerm, filterBy, sortBy])

	/**
	 * Persists only customer carts. Anonymous carts remain session-only.
	 */
	useEffect(() => {
		if (isCustomerUser) {
			localStorage.setItem(
				getCustomerCartStorageKey(currentUsername),
				JSON.stringify(cartItems)
			)
		}
	}, [cartItems, currentUsername, isCustomerUser])

	/**
	 * Fetches all items from the backend API.
	 * Updates state or displays error if request fails.
	 */
	function listItems() {
	    setLoading(true)
	    getAllItems().then((response) => {
	        setItems(response.data)
	        setPageError('')
	    }).catch(error => {
	        console.error(error)
	        setPageError('Failed to load items from backend.')
	    }).finally(() => {
	        setLoading(false)
	    })
	}

	/**
	 * Loads the configured sales tax rate for order summary calculations.
	 */
	function fetchTaxRate() {
		getTaxRate().then((response) => {
			setTaxRate(Number(response.data.rate) || 0)
		}).catch((error) => {
			console.error(error)
			setTaxRate(0)
		})
	}

	/**
	 * Navigates to the add item page.
	 */
	function addNewItem() {
		navigate('/add-item')
	}

	/**
	 * Navigates to update item page for a specific item.
	 *
	 * @param {number} id item ID
	 */
	function updateItem(id) {
		navigate(`/update-item/${id}`)
	}

	/**
	 * Deletes an item after user confirmation.
	 * Refreshes item list upon success.
	 *
	 * @param {number} id item ID
	 */
	function deleteItem(id) {
		if (!window.confirm('Delete this item from the menu?')) {
			return
		}

		deleteItemById(id).then(() => {
			listItems()
		}).catch((error) => {
			console.error(error)
			setPageError('Caannot delete item that has been used in an order.')
		})
	}
	
	/**
	 * Adds an item to the cart, or increments quantity
	 * if the item is already present.
	 *
	 * @param {Object} item item to add
	 */
	function handleAddToCart(item) {
		setOrderMessage('')
		setCartItems((prevCart) => {
			const existingItem = prevCart.find((cartItem) => cartItem.id === item.id)

			if (existingItem) {
				return prevCart.map((cartItem) =>
					cartItem.id === item.id
						? { ...cartItem, quantity: cartItem.quantity + 1 }
						: cartItem
				)
			}

			return [...prevCart, { ...item, quantity: 1 }]
		})
	}

	/**
	 * Returns the total quantity of items in the cart.
	 *
	 * @returns {number} cart count
	 */
	function getCartCount() {
		return cartItems.reduce((total, item) => total + item.quantity, 0)
	}

	/**
	 * Removes an item from the cart.
	 *
	 * @param {number} itemId item ID to remove
	 */
	function handleRemoveFromCart(itemId) {
		setOrderMessage('')
		setCartItems((prevCart) => prevCart.filter((item) => item.id !== itemId))
	}

	/**
	 * Moves the user from the cart modal to the tip selection modal.
	 */
	function handleCheckout() {
		if (cartItems.length === 0) {
			return
		}

		setShowCart(false)
		setShowTipSelector(true)
	}

	/**
	 * Selects a preset or no-tip option.
	 *
	 * @param {string} tipType backend tip type
	 * @param {number} tipValue percentage or custom amount
	 */
	function handleTipSelection(tipType, tipValue) {
		setSelectedTipType(tipType)
		setSelectedTipValue(tipValue)
		setCustomTipError('')
		if (tipType !== 'CUSTOM') {
			setCustomTipInput('')
		}
	}

	/**
	 * Tracks the custom tip input and stores it using backend semantics.
	 *
	 * @param {string} value custom tip text input
	 */
	function handleCustomTipChange(value) {
		if (value === '') {
			setCustomTipInput('')
			setSelectedTipType('CUSTOM')
			setSelectedTipValue(0)
			setCustomTipError('')
			return
		}

		if (!/^\d*$/.test(value)) {
			setCustomTipError('Custom tip must be a non-negative percentage.')
			return
		}

		const parsedValue = Number(value)
		if (Number.isNaN(parsedValue) || parsedValue < 0) {
			setCustomTipError('Custom tip must be a non-negative percentage.')
			return
		}

		setCustomTipError('')
		setCustomTipInput(value)
		setSelectedTipType('PERCENTAGE')
		setSelectedTipValue(parsedValue)
	}

	/**
	 * Returns the current cart subtotal.
	 *
	 * @returns {number} subtotal
	 */
	function getSubtotal() {
		return cartItems.reduce((total, item) => total + (Number(item.price) * item.quantity), 0)
	}

	/**
	 * Calculates the sales tax from subtotal and configured tax rate.
	 *
	 * @returns {number} tax amount
	 */
	function getSalesTax() {
		return getSubtotal() * (taxRate / 100)
	}

	/**
	 * Calculates the selected tip amount using backend tip rules.
	 *
	 * @returns {number} tip amount
	 */
	function getTipAmount() {
		const subtotal = getSubtotal()

		if (selectedTipType === 'PERCENTAGE') {
			return subtotal * (selectedTipValue / 100)
		}

		if (selectedTipType === 'CUSTOM') {
			return Number(selectedTipValue) || 0
		}

		return 0
	}

	/**
	 * Returns the final order total.
	 *
	 * @returns {number} order total
	 */
	function getOrderTotal() {
		return getSubtotal() + getSalesTax() + getTipAmount()
	}

	/**
	 * Returns a readable tip label for the order summary.
	 *
	 * @returns {string} tip summary label
	 */
	function getTipSummaryLabel() {
		if (selectedTipType === 'PERCENTAGE') {
			return `Tip (${selectedTipValue}%)`
		}

		return 'Tip (No Tip)'
	}

	/**
	 * Advances from tip selection to the order summary modal.
	 */
	function handleTipNext() {
		setShowTipSelector(false)
		setShowOrderSummary(true)
	}

	/**
	 * Resets checkout UI state after a successful order.
	 */
	function resetCheckoutState() {
		setCartItems([])
		setSelectedTipType('NONE')
		setSelectedTipValue(0)
		setCustomTipInput('')
		setCustomTipError('')
		setShowCart(false)
		setShowTipSelector(false)
		setShowOrderSummary(false)
	}

	/**
	 * Places the current order using the existing backend API.
	 * Successful customer orders are visible on the My Orders page.
	 */
	function handlePlaceOrder() {
		const orderPayload = {
			items: cartItems.map((item) => ({
				itemId: item.id,
				quantity: item.quantity
			})),
			tipType: selectedTipType,
			tipValue: Number(selectedTipValue) || 0
		}

		placeOrder(orderPayload).then((response) => {
			const createdOrder = response.data
			resetCheckoutState()
			setPageError('')
			setOrderMessage(`Order #${createdOrder.id} placed successfully.`)

			if (role.includes('CUSTOMER')) {
				navigate('/orders')
			}
		}).catch((error) => {
			console.error(error)
			setPageError(error?.response?.data?.error || 'Failed to place order.')
		})
	}

	/**
	 * Returns whether the current custom tip state is valid for advancing.
	 *
	 * @returns {boolean} true when valid
	 */
	function isCustomTipValid() {
		return customTipInput === '' || customTipError === ''
	}
	
	/**
	 * Toggles favorite status for an item.
	 * Stores favorites in localStorage.
	 * 
	 * @param {number} id item ID
	 */
	function toggleFavorite(id) {
        const newFavorites = favorites.includes(id)
            ? favorites.filter(fav => fav !== id)
            : [...favorites, id]

        setFavorites(newFavorites)
        localStorage.setItem('favoriteItems', JSON.stringify(newFavorites))
    }

	/**
	 * Filter items according to favorites, affordable and premium.
	 * Sort items by name, low to high prices and high to low prices.
	 */
    function getProcessedItems() {
        let filtered = items.filter(item => {
            const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase())

            const matchesFilter =
                filterBy === 'all' ||
                (filterBy === 'favorites' && favorites.includes(item.id)) ||
                (filterBy === 'affordable' && item.price <= 5) ||
                (filterBy === 'premium' && item.price > 5)

            return matchesSearch && matchesFilter
        })

        if (sortBy === 'priceLow') {
            filtered.sort((a, b) => a.price - b.price)
        } else if (sortBy === 'priceHigh') {
            filtered.sort((a, b) => b.price - a.price)
        } else if (sortBy === 'name') {
            filtered.sort((a, b) => a.name.localeCompare(b.name))
        }

        return filtered
    }

    const processedItems = getProcessedItems()

	function renderIngredients(itemIngredients = []) {
		if (itemIngredients.length === 0) {
			return 'No ingredients'
		}

		return itemIngredients.map((ingredient) =>
			`${formatIngredientName(ingredient.name)} (${ingredient.amount})`
		).join(', ')
	}

	return (
		<div className='container uc3-page'>
			<br /> <br />
			<h2 className='text-center mb-3'>Items</h2>
			<div className='d-flex justify-content-end mb-3'>
			    {
			        canManageItems && 
			        <button className='btn btn-success' onClick={addNewItem}>
			            Add Item
			        </button>
			    }
			</div>
			<div className="row mb-3 text-center">
			  <div className="col">
			    <div className="card p-2">
			      <strong>{items.length}</strong>
			      <div>Total Items</div>
			    </div>
			  </div>
			  <div className="col">
			    <div className="card p-2">
			      <strong>{favorites.length}</strong>
			      <div>Favorites</div>
			    </div>
			  </div>
			  <div className="col">
			    <div className="card p-2">
			      <strong>{processedItems.length}</strong>
			      <div>Filtered</div>
			    </div>
			  </div>
			</div>
			<div className="mb-2">
			  <strong>Active Filters:</strong>
			  {filterBy !== 'all' && <span className="badge bg-primary text-capitalize mx-1">{filterBy}</span>}
			  {searchTerm && <span className="badge bg-secondary mx-1">Search: {searchTerm}</span>}
			</div>
			{/* SEARCH + FILTER + SORT */}
            <div className="row mb-3 g-2">
                <div className="col-md-4">
					<label htmlFor="searchItems" className="visually-hidden">Search items</label>
					<input
					    id="searchItems"
					    type="text"
					    className="form-control"
					    placeholder="🔍 Search items..."
					    value={searchTerm}
					    onChange={(e) => setSearchTerm(e.target.value)}
					/>
                </div>

                <div className="col-md-3">
                    <select className="form-select" value={filterBy} onChange={(e) => setFilterBy(e.target.value)}>
                        <option value="all">All Items</option>
                        <option value="favorites">⭐ Favorites</option>
                        <option value="affordable">💰 Affordable ($5 or less)</option>
                        <option value="premium">👑 Premium (Over $5)</option>
                    </select>
                </div>

                <div className="col-md-3">
                    <select className="form-select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                        <option value="default">Sort By</option>
                        <option value="priceLow">Price: Low → High</option>
                        <option value="priceHigh">Price: High → Low</option>
                        <option value="name">Name (A–Z)</option>
                    </select>
                </div>
				<div className="col-md-2">
				  <button 
				    className="btn btn-outline-secondary w-100"
				    onClick={() => {
				      setSearchTerm('')
				      setFilterBy('all')
				      setSortBy('default')
				    }}
				  >
				    Clear
				  </button>
				</div>
            </div>

            {/* RESULT COUNT */}
            <div className="mb-2 text-muted">
                Showing {processedItems.length} of {items.length} items
            </div>
            {orderMessage && <div className='alert alert-success'>{orderMessage}</div>}
            {pageError && <div className='alert alert-danger'>{pageError}</div>}
			{loading ? (
			  <div className="text-center py-4">Loading items...</div>
			) : (
				<div className='uc3-table-shell'>
					<table className='table table-bordered table-striped align-middle mb-0'>
						<thead>
							<tr>
								<th>⭐</th>
								<th>Item Name</th>
								<th>Description</th>
								<th>Ingredients</th>
								<th>Price</th>
								{canManageItems && <th>Actions</th>}
								{canPlaceOrders && <th>Add to Cart</th>}
							</tr>
						</thead>
						<tbody>
							{
								processedItems.map((item) =>
									<tr key={item.id}>
										<td>
											<button
											    aria-label="Toggle favorite item"
										        className="btn btn-sm btn-link p-0"
										        onClick={() => toggleFavorite(item.id)}
										        style={{ fontSize: '1.3rem' }}
												tabIndex="0"
										    >
										        {favorites.includes(item.id) ? '⭐' : '☆'}
										    </button>
										</td>
										<td>{item.name}</td>
										<td>{item.description}</td>
										<td>{renderIngredients(item.ingredients || [])}</td>
										<td>${Number(item.price).toFixed(2)}</td>
										{canManageItems && (
											<td>
												<button className='btn btn-info' onClick={() => updateItem(item.id)}>Update</button>
												<button className='btn btn-danger' onClick={() => deleteItem(item.id)} style={{ marginLeft: '10px' }}>Delete</button>
											</td>
										)}
										{canPlaceOrders && (
											<td className="text-center">
												<button
													type="button"
													className="btn btn-link text-dark p-0"
													aria-label={`Add ${item.name} to cart`}
													title={`Add ${item.name} to cart`}
													onClick={() => handleAddToCart(item)}
													style={{ textDecoration: 'none' }}
												>
													<img
														src={addToCartIcon}
														alt=""
														style={{ width: '24px', height: '24px' }}
													/>
												</button>
											</td>
										)}
									</tr>
								)
							}
	                        {
	                            processedItems.length === 0 && (
	                                <tr>
	                                    <td colSpan={canManageItems && canPlaceOrders ? 7 : canManageItems || canPlaceOrders ? 6 : 5} className='text-center py-4'>
											{/* 🔥 NEW */}
											<div className="text-center py-4">
											  <h5>No items found</h5>
											  <p className="text-muted">Try adjusting your search or filters.</p>
											  <button 
											    className="btn btn-outline-secondary mt-2"
											    onClick={() => {
											      setSearchTerm('')
											      setFilterBy('all')
											      setSortBy('default')
											    }}
											  >
											    Reset Filters
											  </button>
											</div>
	                                    </td>
	                                </tr>
	                            )
	                        }
						</tbody>
					</table>
				</div>
			)}

			{canPlaceOrders && (
				<button
					type="button"
					className="btn btn-success rounded-circle position-fixed"
					onClick={() => setShowCart(true)}
					aria-label="Open cart"
					title="Open cart"
					style={{
						right: '24px',
						bottom: '24px',
						width: '64px',
						height: '64px',
						fontSize: '1.5rem',
						zIndex: 1050
					}}
				>
					<img
						src={shoppingCartIcon}
						alt=""
						style={{ width: '32px', height: '32px' }}
					/>
					{getCartCount() > 0 && (
						<span
							className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
							style={{ fontSize: '0.75rem' }}
						>
							{getCartCount()}
						</span>
					)}
				</button>
			)}

			{canPlaceOrders && showCart && (
				<div
					style={{
						position: 'fixed',
						inset: 0,
						backgroundColor: 'rgba(0, 0, 0, 0.4)',
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						zIndex: 1100
					}}
				>
					<div className="card shadow" style={{ width: '520px', maxWidth: '92vw' }}>
						<div className="card-body">
							<div className="d-flex justify-content-between align-items-center mb-3">
								<h4 className="mb-0">My Cart</h4>
								<button
									type="button"
									className="btn-close"
									aria-label="Close cart"
									onClick={() => setShowCart(false)}
								></button>
							</div>

							{cartItems.length === 0 ? (
								<p className="text-muted mb-0">Your cart is empty.</p>
							) : (
								<table className="table table-bordered align-middle mb-3">
									<thead>
										<tr>
											<th>Item</th>
											<th>Price</th>
											<th>Quantity</th>
											<th>Delete</th>
										</tr>
									</thead>
									<tbody>
										{cartItems.map((item) => (
											<tr key={item.id}>
												<td>{item.name}</td>
												<td>${Number(item.price).toFixed(2)}</td>
												<td>{item.quantity}</td>
												<td className="text-center">
													<button
														type="button"
														className="btn btn-link text-danger p-0"
														aria-label={`Remove ${item.name} from cart`}
														title={`Remove ${item.name} from cart`}
														onClick={() => handleRemoveFromCart(item.id)}
														style={{ textDecoration: 'none' }}
													>
														<img
															src={deleteIcon}
															alt=""
															style={{ width: '18px', height: '18px' }}
														/>
													</button>
												</td>
											</tr>
										))}
									</tbody>
								</table>
							)}

							<div className="d-flex justify-content-end">
								<button
									type="button"
									className="btn btn-success"
									onClick={handleCheckout}
									disabled={cartItems.length === 0}
								>
									Check Out
								</button>
							</div>
						</div>
					</div>
				</div>
			)}

			{canPlaceOrders && showTipSelector && (
				<div
					style={{
						position: 'fixed',
						inset: 0,
						backgroundColor: 'rgba(0, 0, 0, 0.4)',
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						zIndex: 1100
					}}
				>
					<div className="card shadow" style={{ width: '420px', maxWidth: '92vw' }}>
						<div className="card-body">
							<div className="d-flex justify-content-between align-items-center mb-3">
								<h4 className="mb-0">Select Tip</h4>
								<button
									type="button"
									className="btn-close"
									aria-label="Close tip selector"
									onClick={() => setShowTipSelector(false)}
								></button>
							</div>

							<div className="row g-3 mb-4">
								<div className="col-6">
									<button
										type="button"
										className={`btn w-100 ${selectedTipType === 'PERCENTAGE' && selectedTipValue === 15 ? 'btn-primary' : 'btn-outline-primary'}`}
										onClick={() => handleTipSelection('PERCENTAGE', 15)}
									>
										15%
									</button>
								</div>
								<div className="col-6">
									<button
										type="button"
										className={`btn w-100 ${selectedTipType === 'PERCENTAGE' && selectedTipValue === 20 ? 'btn-primary' : 'btn-outline-primary'}`}
										onClick={() => handleTipSelection('PERCENTAGE', 20)}
									>
										20%
									</button>
								</div>
								<div className="col-6">
									<button
										type="button"
										className={`btn w-100 ${selectedTipType === 'PERCENTAGE' && selectedTipValue === 25 ? 'btn-primary' : 'btn-outline-primary'}`}
										onClick={() => handleTipSelection('PERCENTAGE', 25)}
									>
										25%
									</button>
								</div>
								<div className="col-6">
									<button
										type="button"
										className={`btn w-100 ${selectedTipType === 'NONE' ? 'btn-secondary' : 'btn-outline-secondary'}`}
										onClick={() => handleTipSelection('NONE', 0)}
									>
										Skip
									</button>
								</div>
							</div>

							<div className="mb-4">
								<label className="form-label">Custom Tip Percentage</label>
								<div className="d-flex align-items-center gap-2">
									<span className="fw-semibold">%</span>
									<input
										type="text"
										inputMode="numeric"
										pattern="[0-9]*"
										className="form-control"
										placeholder="Enter whole number"
										value={customTipInput}
										onChange={(e) => handleCustomTipChange(e.target.value)}
									/>
								</div>
								{customTipError && (
									<div className="text-danger mt-2">{customTipError}</div>
								)}
							</div>

							<div className="d-flex justify-content-end">
								<button
									type="button"
									className="btn btn-success"
									onClick={handleTipNext}
									disabled={!isCustomTipValid()}
								>
									Next
								</button>
							</div>
						</div>
					</div>
				</div>
			)}

			{canPlaceOrders && showOrderSummary && (
				<div
					style={{
						position: 'fixed',
						inset: 0,
						backgroundColor: 'rgba(0, 0, 0, 0.4)',
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						zIndex: 1100
					}}
				>
					<div className="card shadow" style={{ width: '520px', maxWidth: '92vw' }}>
						<div className="card-body">
							<div className="d-flex justify-content-between align-items-center mb-3">
								<h4 className="mb-0">Order Summary</h4>
								<button
									type="button"
									className="btn-close"
									aria-label="Close order summary"
									onClick={() => setShowOrderSummary(false)}
								></button>
							</div>

							<table className="table table-bordered align-middle mb-3">
								<thead>
									<tr>
										<th>Item</th>
										<th>Quantity</th>
										<th>Amount</th>
									</tr>
								</thead>
								<tbody>
									{cartItems.map((item) => (
										<tr key={item.id}>
											<td>{item.name}</td>
											<td>{item.quantity}</td>
											<td>${(Number(item.price) * item.quantity).toFixed(2)}</td>
										</tr>
									))}
								</tbody>
							</table>

							<div className="mb-3">
								<div className="d-flex justify-content-between">
									<span>Subtotal</span>
									<span>${getSubtotal().toFixed(2)}</span>
								</div>
								<div className="d-flex justify-content-between">
									<span>Sales Tax</span>
									<span>${getSalesTax().toFixed(2)}</span>
								</div>
								<div className="d-flex justify-content-between">
									<span>{getTipSummaryLabel()}</span>
									<span>${getTipAmount().toFixed(2)}</span>
								</div>
								<div className="d-flex justify-content-between fw-bold">
									<span>Total</span>
									<span>${getOrderTotal().toFixed(2)}</span>
								</div>
							</div>

							<div className="d-flex justify-content-between">
								<button
									type="button"
									className="btn btn-outline-secondary"
									onClick={() => {
										setShowOrderSummary(false)
										setShowTipSelector(true)
									}}
								>
									Back
								</button>
								<button
									type="button"
									className="btn btn-success"
									onClick={handlePlaceOrder}
								>
									Place Order
								</button>
							</div>
						</div>
					</div>
				</div>
			)}
			
		</div>
	)
}

export default ListItemsComponent
