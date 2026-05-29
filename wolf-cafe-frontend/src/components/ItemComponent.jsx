import React from 'react'
import { useEffect, useState } from 'react'
import { getItemById, saveItem, updateItem } from '../services/ItemService'
import { getInventory } from '../services/InventoryService'
import { useNavigate, useParams } from 'react-router-dom'

const createIngredientRow = (name = '', amount = '') => ({ name, amount })
const formatIngredientName = (name = '') => name
    .toLowerCase()
    .split(' ')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')

const TodoComponent = () => {

    const [name, setName] = useState('')
    const [description, setDescription] = useState('')
	const [price, setPrice] = useState('')
    const [inventoryIngredientNames, setInventoryIngredientNames] = useState([])
    const [ingredients, setIngredients] = useState([createIngredientRow()])
    const [errors, setErrors] = useState({})
    const [formError, setFormError] = useState('')
    const { id } = useParams()

    const navigate = useNavigate()
    const role = sessionStorage.getItem('role') || localStorage.getItem('role') || ''
    const isStaffView = role.includes('STAFF')

    useEffect(() => {
        if (!isStaffView) {
            return
        }

        getInventory().then((response) => {
            const ingredientNames = Object.keys(response?.data?.ingredients || {})
            setInventoryIngredientNames(ingredientNames)
            setIngredients((currentIngredients) => {
                if (currentIngredients.length === 0) {
                    return [createIngredientRow(ingredientNames[0] || '', '')]
                }

                return currentIngredients.map((ingredient, index) => (
                    index === 0 && !ingredient.name
                        ? { ...ingredient, name: ingredientNames[0] || '' }
                        : ingredient
                ))
            })
        }).catch((error) => {
            console.error(error)
        })
    }, [isStaffView])

    useEffect(() => {
        if(id) {
            getItemById(id).then((response) => {
                console.log(response.data)
                setName(response.data.name)
                setDescription(response.data.description)
				setPrice(response.data.price)
                if (isStaffView) {
                    const itemIngredients = response.data.ingredients || []
                    setIngredients(
                        itemIngredients.length > 0
                            ? itemIngredients.map((ingredient) => createIngredientRow(ingredient.name || '', ingredient.amount ?? ''))
                            : [createIngredientRow(inventoryIngredientNames[0] || '', '')]
                    )
                }
            }).catch(error => {
                console.error(error)
                setFormError('Failed to load item details.')
            })
        }
    }, [id, isStaffView, inventoryIngredientNames])

    function validateForm() {
        const trimmedName = name.trim()
        const trimmedDescription = description.trim()
        const nextErrors = {}

        if (!trimmedName) {
            nextErrors.name = 'Item name is required.'
        }

        if (!trimmedDescription) {
            nextErrors.description = 'Item description is required.'
        }

        if (price === '' || price === null || price === undefined) {
            nextErrors.price = 'Item price is required.'
        } else if (Number.isNaN(Number(price))) {
            nextErrors.price = 'Item price must be a number.'
        } else if (Number(price) <= 0) {
            nextErrors.price = 'Item price must be greater than 0.'
        }

        if (isStaffView) {
            const hasInvalidIngredientAmount = ingredients.some((ingredient) =>
                ingredient.amount === '' ||
                ingredient.amount === null ||
                ingredient.amount === undefined ||
                Number.isNaN(Number(ingredient.amount)) ||
                Number(ingredient.amount) <= 0
            )

            if (hasInvalidIngredientAmount) {
                nextErrors.ingredients = 'ingredient amounts must be positive'
            }
        }

        setErrors(nextErrors)
        return Object.keys(nextErrors).length === 0
    }

    function getBackendErrorMessage(error, fallbackMessage) {
        const backendMessage = error?.response?.data

        if (typeof backendMessage === 'string' && backendMessage.trim()) {
            return backendMessage
        }

        return fallbackMessage
    }

    function saveOrUpdateItem(e) {
        e.preventDefault()

        if (!validateForm()) {
            return
        }

        setFormError('')

        const item = {
            name: name.trim(),
            description: description.trim(),
            price: Number(price),
            ingredients: isStaffView
                ? ingredients.map((ingredient) => ({
                    name: ingredient.name,
                    amount: Number(ingredient.amount)
                }))
                : []
        }

        if (id) {
            updateItem(id, item).then(() => {
                navigate('/items')
            }).catch(error => {
                console.error(error)
                setFormError(getBackendErrorMessage(error, 'Failed to update item.'))
            })
        } else {
            saveItem(item).then(() => {
                navigate('/items')
            }).catch(error => {
                console.error(error)
                setFormError(getBackendErrorMessage(error, 'Failed to add item.'))
            })
        }
    }

    function pageTitle() {
        if (id) {
            return <h2 className='text-center'>Update Item</h2>
        } else {
            return <h2 className='text-center'>Add Item</h2>
        }
    }

    function updateIngredientRow(index, field, value) {
        setIngredients((currentIngredients) => currentIngredients.map((ingredient, ingredientIndex) => (
            ingredientIndex === index
                ? { ...ingredient, [field]: value }
                : ingredient
        )))
        setErrors((current) => ({ ...current, ingredients: null }))
    }

    function addIngredientRow() {
        setIngredients((currentIngredients) => [
            ...currentIngredients,
            createIngredientRow(inventoryIngredientNames[0] || '', '')
        ])
        setErrors((current) => ({ ...current, ingredients: null }))
    }

  return (
    <div className='container uc3-page'>
        <br /> <br />
        <div className='row justify-content-center'>
            <div className='card col-md-7 col-lg-6 uc3-form-card'>
                { pageTitle() }
                
                <div className='card-body'>
                    <form onSubmit={saveOrUpdateItem} noValidate>
                        <p className='uc3-form-subtitle'>
                            Capture the item name, customer-facing description, and price.
                        </p>

                        <div className='form-group mb-2'>
                            <label className='form-label'>Item Name:</label>
                            <input 
                                type='text'
                                className={`form-control ${errors.name ? 'is-invalid' : ''}`}
                                placeholder='Enter Item Name'
                                name='name'
                                value={name}
                                onChange={(e) => {
                                    setName(e.target.value)
                                    setErrors((current) => ({ ...current, name: null }))
                                }}
                            >
                            </input>
                            {errors.name && <div className='invalid-feedback'>{errors.name}</div>}
                        </div>

                        <div className='form-group mb-2'>
                            <label className='form-label'>Item Description:</label>
                            <input 
                                type='text'
                                className={`form-control ${errors.description ? 'is-invalid' : ''}`}
                                placeholder='Enter Item Description'
                                name='description'
                                value={description}
                                onChange={(e) => {
                                    setDescription(e.target.value)
                                    setErrors((current) => ({ ...current, description: null }))
                                }}
                            >
                            </input>
                            {errors.description && <div className='invalid-feedback'>{errors.description}</div>}
                        </div>

                        <div className='form-group mb-2'>
                            <label className='form-label'>Item Price:</label>
							<input 
                                type='number'
                                className={`form-control ${errors.price ? 'is-invalid' : ''}`}
                                placeholder='Enter Item Price'
                                name='price'
                                value={price}
                                min='0'
                                step='0.01'
                                onChange={(e) => {
                                    setPrice(e.target.value)
                                    setErrors((current) => ({ ...current, price: null }))
                                }}
                            >
                            </input>
                            {errors.price && <div className='invalid-feedback'>{errors.price}</div>}
                        </div>

                        {isStaffView && (
                            <div className='form-group mt-4'>
                                <label className='form-label'>Ingredients:</label>
                                <div className='d-flex flex-column gap-2'>
                                    {ingredients.map((ingredient, index) => (
                                        <div className='row g-2' key={index}>
                                            <div className='col-7'>
                                                <select
                                                    className='form-select'
                                                    value={ingredient.name}
                                                    onChange={(e) => updateIngredientRow(index, 'name', e.target.value)}
                                                    disabled={inventoryIngredientNames.length === 0}
                                                >
                                                    {inventoryIngredientNames.length === 0 ? (
                                                        <option value=''>No inventory ingredients available</option>
                                                    ) : (
                                                        inventoryIngredientNames.map((ingredientName) => (
                                                            <option key={ingredientName} value={ingredientName}>
                                                                {formatIngredientName(ingredientName)}
                                                            </option>
                                                        ))
                                                    )}
                                                </select>
                                            </div>
                                            <div className='col-5'>
                                                <input
                                                    type='number'
                                                    className={`form-control ${errors.ingredients ? 'is-invalid' : ''}`}
                                                    placeholder='Amount'
                                                    value={ingredient.amount}
                                                    min='0'
                                                    step='1'
                                                    onChange={(e) => updateIngredientRow(index, 'amount', e.target.value)}
                                                >
                                                </input>
                                                {errors.ingredients && index === 0 && (
                                                    <div className='invalid-feedback'>{errors.ingredients}</div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <div className='d-flex justify-content-end mt-3'>
                                    <button
                                        type='button'
                                        className='btn btn-success'
                                        onClick={addIngredientRow}
                                        disabled={inventoryIngredientNames.length === 0}
                                        aria-label='Add another ingredient'
                                    >
                                        +
                                    </button>
                                </div>
                            </div>
                        )}

                        {formError && <div className='alert alert-danger mt-3 mb-3'>{formError}</div>}

                        <div className='d-flex gap-2 mt-4 justify-content-end'>
                            <button type='button' className='btn btn-outline-secondary' onClick={() => navigate('/items')}>Cancel</button>
                            <button type='submit' className='btn btn-success'>{id ? 'Save Changes' : 'Add Item'}</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
  )
}

export default TodoComponent
