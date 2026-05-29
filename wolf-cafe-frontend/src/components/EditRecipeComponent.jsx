import { useState, useEffect } from 'react'
import { getRecipeByName, updateRecipe, listRecipes } from '../services/RecipesService'
import { useParams, useNavigate } from 'react-router-dom'

/** Allows us to edit recipe */
const EditRecipeComponent = () => {
  const [id, setId] = useState(null)
  const [name, setName] = useState("")
  const [price, setPrice] = useState("")
  const [ingredients, setIngredients] = useState([])
  const [ingredientName, setIngredientName] = useState("")
  const [ingredientUnits, setIngredientUnits] = useState("")
  const [errors, setErrors] = useState({})
  const [generalErrors, setGeneralErrors] = useState([])
  const [successMessage, setSuccessMessage] = useState("")
  const [loading, setLoading] = useState(true)
  const [ingredientError, setIngredientError] = useState("")

  const { incomingName } = useParams()
  const navigator = useNavigate()

  useEffect(() => {
    if (incomingName) {
      getRecipeByName(incomingName).then((response) => {
        console.log("received recipe:", response.data)
        setId(response.data.id)
        setName(response.data.name)
        setPrice(response.data.price)
        setIngredients(response.data.ingredients || [])
        setLoading(false)
      }).catch(error => {
        console.error(error)
        setGeneralErrors(['cannotload'])
        setLoading(false)
      })
    }
  }, [incomingName])

  /**
   * Function to add an ingredient to the recipe. 
   * Validates the input and checks for duplicates.
   * @returns void
   */
  function addIngredient() {
	setIngredientError("") // Reset ingredient error
	
    // Validate ingredient name
    if (!ingredientName.trim()) {
      setIngredientError('Please enter an ingredient name')
      return
    }

    // Validate units
    if (!ingredientUnits) {
      setIngredientError('Please enter units')
      return
    }

    if (!Number.isInteger(Number(ingredientUnits)) || Number(ingredientUnits) <= 0) {
      setIngredientError('Units must be a positive integer')
      return
    }

    // Check for duplicates
    const isDuplicate = ingredients.some(ing => ing.name.toLowerCase() === ingredientName.toLowerCase())
    if (isDuplicate) {
      setIngredientError('This ingredient is already in the recipe')
      return
    }

    // Add the ingredient
    setIngredients([...ingredients, { name: ingredientName, amount: Number(ingredientUnits) }])
    setIngredientName("")
    setIngredientUnits("")
	setIngredientError("") // Clear any previous error
  }

  /**
   * Function to remove an ingredient from the recipe by index.
   * @param {*} index the index to remove.
   */
  function removeIngredient(index) {
    const newIngredients = ingredients.filter((_, i) => i !== index)
    setIngredients(newIngredients)
  }

  /**
   * Function to update the units of an ingredient in the recipe by index.
   * @param {*} index the index of the ingredient to update.
   * @param {*} value to update the units to.
   */
  function updateIngredientUnits(index, value) {
    const newIngredients = [...ingredients]
    newIngredients[index].amount = value
    setIngredients(newIngredients)
  }

  /**
   * Validates the form inputs and ingredients.
   * @returns boolean indicating if the form is valid.
   */
  function validateForm() {
    const newErrors = {}

    // Validate price
    if (!price) {
      newErrors.price = 'Price is required'
    } else if (!Number.isInteger(Number(price)) || Number(price) <= 0) {
      newErrors.price = `${name} price must be a positive integer`
    }

    // Validate units
    ingredients.forEach((ingredient, index) => {
      if (!ingredient.amount || !Number.isInteger(Number(ingredient.amount)) || Number(ingredient.amount) <= 0) {
        newErrors[`ingredient_${index}`] = `${ingredient.name} amount must be a positive integer`
      }
    })

    // Check for no ingredients
    const ingredientErrors = Object.keys(newErrors).filter(key => key.startsWith('ingredient_')).length
    if (ingredients.length === 0) {
      newErrors.ingredients = 'noingredienterror'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  /**
   * Function to save the recipe. 
   * Validates the form and sends a PUT request to update the recipe.
   * @returns void
   */
  function saveRecipe() {
    setErrors({})
    setGeneralErrors([])
    
    if (!validateForm()) return

    // Build recipe data with ingredients array
    const recipeData = {
      name: name,
      price: Number(price),
      ingredients: ingredients.map(ing => ({
        name: ing.name,
        amount: Number(ing.amount)
      }))
    }

    updateRecipe(id, recipeData).then((response) => {
      setSuccessMessage('Recipe updated successfully!')
      setTimeout(() => navigator('/recipes'), 1500)
    }).catch(error => {
      console.error(error)
      if (error.response?.status === 404) {
        setGeneralErrors(['[Cannot Edit] Recipe was deleted by another user'])
      } else {
        setGeneralErrors(['Failed to update recipe. Please try again.'])
      }
    })
  }

  if (loading) {
    return (
      <div className="container mt-4 text-center">
        <p>Loading recipe...</p>
      </div>
    )
  }

  return (
    <div className='container mt-4'>
      <div className='row'>
        <div className='card col-md-6 offset-md-3'>
          <h2 className="text-center mt-3">✏️ Edit Recipe</h2>
          <div className="card-body">
            {generalErrors.includes('cannotload') && (
              <small className="text-danger fw-bold d-block mb-3">[Cannot Edit] Recipe not found or was deleted</small>
            )}            
            {generalErrors.length > 0 && !generalErrors.includes('cannotload') && (
              <div className="alert alert-danger mb-3">
                <strong>Error:</strong>
                <ul className="mb-0 mt-2">
                  {generalErrors.map((error, index) => (
                    <li key={index}>{error}</li>
                  ))}
                </ul>
              </div>
            )}
            <form>
              <div className="form-group mb-3">
                <label className="form-label">Recipe Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={name}
                  disabled
                />
              </div>

              <div className="form-group mb-3">
                <label className="form-label">Recipe Price</label>
                <input
                  type="number"
                  className={`form-control ${errors.price ? 'is-invalid' : ''}`}
                  placeholder="Enter Recipe Price"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                />
                {errors.price && (
                  <small className="text-danger fw-bold d-block mt-1">{errors.price}</small>
                )}
              </div>

              <div className="form-group mb-3">
                <label className="form-label">Ingredients</label>
                {ingredients.length === 0 ? (
                  <p className="text-muted">No ingredients added yet</p>
                ) : (
                  ingredients.map((ingredient, index) => (
                    <div key={index} className="row mb-2">
                      <div className="col-6">
                        <input
                          type="text"
                          className="form-control"
                          value={ingredient.name}
                          disabled
                        />
                      </div>
                      <div className="col-4">
                        <input
                          type="number"
                          className={`form-control ${errors[`ingredient_${index}`] ? 'is-invalid' : ''}`}
                          placeholder="Units"
                          value={ingredient.amount}
                          onChange={(e) => updateIngredientUnits(index, e.target.value)}
                        />
                        {errors[`ingredient_${index}`] && (
                          <small className="text-danger fw-bold d-block mt-1">{errors[`ingredient_${index}`]}</small>
                        )}
                      </div>
                      <div className="col-2">
                        <button
                          type="button"
                          className="btn btn-outline-danger w-100"
                          onClick={() => removeIngredient(index)}
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  ))
                )}
                <div className="row mt-2">
                  <div className="col-6">
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Ingredient name"
                      value={ingredientName}
                      onChange={(e) => setIngredientName(e.target.value)}
                    />
                  </div>
                  <div className="col-4">
                    <input
                      type="number"
                      className="form-control"
                      placeholder="Units"
                      value={ingredientUnits}
                      onChange={(e) => setIngredientUnits(e.target.value)}
                    />
                  </div>
                  <div className="col-2">
                    <button
                      type="button"
                      className="btn btn-outline-secondary w-100"
                      onClick={addIngredient}
                    >
                      ➕
                    </button>
                  </div>
                </div>
                {ingredientError && (
                  <small className="text-danger fw-bold d-block mt-1">{ingredientError}</small>
                )}
                {errors.ingredients === 'noingredienterror' && (
                  <small className="text-danger fw-bold d-block mt-2 mb-2">[No Ingredients] Recipe must have at least one ingredient</small>
                )}
              </div>

              <div className="d-flex gap-2">
                <button
                  type="button"
                  className="btn btn-success flex-grow-1"
                  onClick={saveRecipe}
                >
                  Save Recipe
                </button>
                <button
                  type="button"
                  className="btn btn-secondary flex-grow-1"
                  onClick={() => navigator('/recipes')}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

	  {successMessage && (
        <p className="text-success fw-bold text-center mt-2">{successMessage}</p>
      )}
    </div>
  )
}

export default EditRecipeComponent