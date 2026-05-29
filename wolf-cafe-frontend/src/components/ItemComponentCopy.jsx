import React from 'react'
import { useEffect, useState } from 'react'
import { getItemById, saveItem, updateItem } from '../services/ItemService'
import { useNavigate, useParams } from 'react-router-dom'

const ItemComponent = () => {

    const [name, setName] = useState('')
    const [description, setDescription] = useState('')
	const [price, setPrice] = useState('')
    const [error, setError] = useState('')
    const { id } = useParams()

    const navigate = useNavigate()

    useEffect(() => {
        if(id) {
            getItemById(id).then((response) => {
                console.log(response.data)
                setName(response.data.name)
                setDescription(response.data.description)
				setPrice(response.data.price)
            }).catch(error => {
                console.error(error)
            })
        }
    }, [id])

    function saveOrUpdateItem(e) {
        e.preventDefault()
        // Validation
        if (!name || price === '' || isNaN(price) || parseFloat(price) <= 0) {
            setError("Invalid input: name required and price must be positive")
            return
        }

        const item = { name, description, price }

        if (id) {
            updateItem(id, item).then((response) => {
                console.log(response.data)
                navigate('/items')
            }).catch(error => {
                console.error(error)
            })
        } else {
            saveItem(item).then((response) => {
                console.log(response.data)
                navigate('/items')
            }).catch(error => {
                console.error(error)
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

  return (
    <div className='container'>
        <br /> <br />
        <div className='row'>
            <div className='card col-md-6 offset-md-3 offset-md-3'>
                { pageTitle() }
                
                <div className='card-body'>                    
                    {
                        error && <div className='text-danger mb-2'>{error}</div>
                    }

                    <form>
                        <div className='form-group mb-2'>
                            <label className='form-label'>Item Name:</label>
                            <input 
                                type='number'
                                className='form-control'
                                placeholder='Enter Item Name'
                                name='name'
                                value={name}
                                onChange={(e) => {
                                    setName(e.target.value)
                                    setError('')
                                }}
                            >
                            </input>
                        </div>

                        <div className='form-group mb-2'>
                            <label className='form-label'>Item Description:</label>
                            <input 
                                type='text'
                                className='form-control'
                                placeholder='Enter Item Description'
                                name='description'
                                value={description}
                                onChange={(e) => {
                                    setDescription(e.target.value)
                                    setError('')
                                }}
                            >
                            </input>
                        </div>

                        <div className='form-group mb-2'>
                            <label className='form-label'>Item Price:</label>
							<input 
                                type='text'
                                className='form-control'
                                placeholder='Enter Item Price'
                                name='price'
                                value={price}
                                onChange={(e) => {
                                    setDescription(e.target.value)
                                    setError('')
                                }}
                            >
                            </input>
                        </div>

                        <button type='submit' className='btn btn-success' onClick={(e) => saveOrUpdateItem(e)}>Submit</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
  )
}

export default ItemComponent