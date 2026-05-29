/**
 * MakeRecipeComponent allows a user to purchase a beverage.
 * 
 * The user enters an amount paid and selects a recipe to make.
 * The component validates the payment, calls the backend API,
 * displays error messages for alternative flows, and shows
 * the change and success message when the beverage is made.
 * 
 * @author Jayani Sivakumar
 */
import React, { useEffect, useState } from 'react'
import { listRecipes } from '../services/RecipesService'
import { makeRecipe } from '../services/MakeRecipeService'

/** Provides functionality to make a recipe, pay for it, and receive change.*/
const MakeRecipeComponent = () => {

	/** List of available recipes retrieved from backend */
    const [recipes, setRecipes] = useState([])
	
	/** Amount entered by user (stored as string for validation) */
	const [amtPaid, setAmtPaid] = useState("")     // keep as string from input
	
	/** Change returned from backend after successful purchase */
	const [change, setChange] = useState(null)     // number or null

	/** Stores general error messages */
    const [errors, setErrors] = useState( {
        general: ""
    })
	
	/** Stores success message after beverage is made */
	const [successMessage, setSuccessMessage] = useState("")

	/**
     * Loads recipes when component mounts.
     */
    useEffect(() => {
        getAllRecipes()
    }, [])

	/**
     * Retrieves all recipes from the backend.
     */
    function getAllRecipes() {
        listRecipes().then((response) => {
            setRecipes(response.data)
        }).catch(error => {
            console.error(error)
        })
    }

	/**
     * Attempts to make the selected recipe.
     * 
     * Validates payment, sends request to backend,
     * handles success and alternative flows.
     * 
     * @param name name of the recipe to make
	 * @param amtPaid amount paid for the beverage
     */
    function craftRecipe(name, amtPaid) {
		setErrors({ general: "" })
		setChange(null)
		setSuccessMessage("")
		//e.preventDefault()
        console.log(name, amtPaid)

		if (validateForm()) {
			const paidInt = parseInt(amtPaid, 10)
	        makeRecipe(name, paidInt).then((response) => {
	            getAllRecipes()
	            setAmtPaid("")
	            setChange(response.data)
				setErrors({ general: "" })
				setSuccessMessage("Your beverage is ready! Enjoy ☕")
	        }).catch(error => {
				const status = error?.response?.status
				const errorsCopy = { general: "" }

				if (status === 409) {
				    errorsCopy.general = "Insufficient funds to pay."
				}
				else if (status === 400) {
				    errorsCopy.general = "Insufficient inventory."
				}
				else if (status === 404) {
				    errorsCopy.general = "Recipe not found."
				}
				else {
				    errorsCopy.general = "An unexpected error occurred."
				}

				setErrors(errorsCopy)
				setSuccessMessage("")
	        })
		}
    }

	/**
     * Validates the payment input.
     * 
     * Ensures the amount is non-empty, numeric,
     * an integer, and non-negative.
     * 
     * @returns true if valid, false otherwise
     */
	function validateForm() {
	    let valid = true
	    const errorsCopy = { general: "" }

	    if (amtPaid === "" || amtPaid === null) {
	        errorsCopy.general = "Please enter an amount paid."
	        valid = false
	    }
	    else if (isNaN(amtPaid)) {
	        errorsCopy.general = "Amount paid must be a number."
	        valid = false
	    }
	    else if (!Number.isInteger(Number(amtPaid))) {
	        errorsCopy.general = "Amount paid must be an integer."
	        valid = false
	    }
	    else if (Number(amtPaid) < 0) {
	        errorsCopy.general = "Amount paid must be a positive integer."
	        valid = false
	    }

	    setErrors(errorsCopy)
	    return valid
	}

	/**
     * Renders general error message if present.
     */
    function getGeneralErrors() {
        if (errors.general) {
            return <div className="p-3 mb-2 bg-danger text-white">{errors.general}</div>
        }
    }

    return (
        <div className="container">
            <h2 className="text-center">Order Beverage</h2>
            { getGeneralErrors() }
			{successMessage && (
			    <div className="p-3 mb-2 bg-success text-white">
			        {successMessage}
			    </div>
			)}
            <br /><br />
            <div className="card-body">
                <form>
                    <div className="form-group mb-2">
                        <label className="form-label">Amount Paid</label>
                        <input
                            type="text"
                            name="amtPaid"
                            placeholder="How much are you paying?"
                            value={amtPaid}
                            onChange={(e) => setAmtPaid(e.target.value)}
                            className={`form-control ${errors.general ? "is-invalid":""}`}
                        >
                        </input>
                        <label className="form-label">Change: {change}</label>
                    </div>
                </form>
            </div>

            <table className="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Recipe Name</th>
                        <th>Recipe Price</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {
                        recipes.map(recipe => 
                        <tr key={recipe.id}>
                            <td>{recipe.name}</td>
                            <td>{recipe.price}</td>
                            <td>
                                <button className="btn btn-primary" onClick={() => craftRecipe(recipe.name, amtPaid)}
                                    style={{marginLeft: '10px'}}
                                >Make Recipe</button>
                            </td>
                        </tr>)
                    }
                </tbody>
            </table>
        </div>
    )

}

export default MakeRecipeComponent