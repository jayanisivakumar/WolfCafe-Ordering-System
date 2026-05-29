import { useState, useEffect } from 'react'
import { createRecipe, listRecipes } from '../services/RecipesService'
import { useNavigate } from 'react-router-dom'

/** Form to create a new recipe. */
const RecipeComponent = () => {
  const [name, setName] = useState("")
  const [price, setPrice] = useState("")
  const [ingredients, setIngredients] = useState([])
  const [ingredientName, setIngredientName] = useState("")
  const [ingredientUnits, setIngredientUnits] = useState("")
  const [errors, setErrors] = useState({})
  const [generalErrors, setGeneralErrors] = useState([])
  const [successMessage, setSuccessMessage] = useState("")
  const [ingredientError, setIngredientError] = useState("")
  const navigator = useNavigate()

  useEffect(() => {
  }, [])

  /**
   * Add an ingredient to the recipe. Validates the input and checks for duplicates.
   * @returns void
   */
  function addIngredient() {
    setIngredientError("")

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
      setIngredientError('Initial amount must be a positive integer')
      return
    }

    // Check for duplicates
    const isDuplicate = ingredients.some(ing => ing.name.toLowerCase() === ingredientName.toLowerCase())
    if (isDuplicate) {
      setIngredientError('This ingredient is already in the recipe')
      return
    }

    // Add the ingredient
    setIngredients([...ingredients, { name: ingredientName, units: Number(ingredientUnits) }])
    setIngredientName("")
    setIngredientUnits("")
    setIngredientError("")
  }

  /**
   * Function to remove an ingredient from the recipe by index.
   * @param {number} index - The index of the ingredient to remove
   */
  function removeIngredient(index) {
    const newIngredients = ingredients.filter((_, i) => i !== index)
    setIngredients(newIngredients)
  }

  /**
   * Validates the form inputs for the recipe. 
   * Checks for required fields, positive integers, and at least one ingredient.
   * @returns {boolean} - True if the form is valid, false otherwise
   */
  function validateForm() {
    const newErrors = {}
    const newGeneralErrors = []

    // Validate name
    if (!name.trim()) {
      newGeneralErrors.push('name')
    }

    // Validate price
    if (!price) {
      newErrors.price = 'Price is required'
    } else if (!Number.isInteger(Number(price)) || Number(price) <= 0) {
      newErrors.price = `${name || 'Recipe'} price must be a positive integer`
    }

    // Check for no ingredients
    if (ingredients.length === 0) {
      newGeneralErrors.push('ingredients')
    }

    setErrors(newErrors)
    setGeneralErrors(newGeneralErrors)
    return Object.keys(newErrors).length === 0 && newGeneralErrors.length === 0
  }

  /**
   * Function to check for duplicate recipe names.
   * @returns true if the recipe name is unique and the total number of recipes is less than 3, false otherwise
   * If duplicate or too many recipes, adds an error message to the generalErrors state.
   * @returns {Promise<boolean>} - true if valid, false otherwise
   */
  function checkDuplicate() {
    return listRecipes().then((response) => {
      const recipes = response.data
      const isDuplicate = recipes.some(recipe => recipe.name.toLowerCase() === name.toLowerCase())
      
      if (isDuplicate) {
        setGeneralErrors(prev => [...prev, 'duplicate'])
        return false
      }

      if (recipes.length >= 3) {
        setGeneralErrors(prev => [...prev, 'toomany'])
        return false
      }

      return true
    }).catch(error => {
      console.error(error)
      return false
    })
  }

  /**
   * Function to save the recipe. 
   * Validates the form and checks for duplicates before sending a POST request to create the recipe.
   * @returns void
   */
  function saveRecipe() {
    if (!validateForm()) return

    checkDuplicate().then((isValid) => {
      if (!isValid) return

      // Build recipe data with ingredients array (name, amount format)
      const recipeData = {
        name: name,
        price: Number(price),
        ingredients: ingredients.map(ing => ({
          name: ing.name,
          amount: ing.units
        }))
      }

      createRecipe(recipeData).then((response) => {
        setSuccessMessage('Recipe added successfully!')
        setTimeout(() => navigator('/recipes'), 1500)
      }).catch(error => {
        console.error(error)
        setGeneralErrors(['Failed to save recipe. Please try again.'])
      })
    })
  }

  return (
    <div className='container mt-4'>
      <div className='row'>
        <div className='card col-md-6 offset-md-3'>
          <h2 className="text-center mt-3">➕ Add Recipe</h2>
          <div className="card-body">
            <form>
              <div className="form-group mb-3">
                <label className="form-label">Recipe Name</label>
                <input
                  type="text"
                  className={`form-control ${generalErrors.includes('name') ? 'is-invalid' : ''}`}
                  placeholder="Enter Recipe Name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
                {generalErrors.includes('name') && (
                  <small className="text-danger fw-bold d-block mt-1">Recipe name is required</small>
                )}
                {generalErrors.includes('duplicate') && (
                  <small className="text-danger fw-bold d-block mt-1">[Duplicate] A recipe with this name already exists</small>
                )}
                {generalErrors.includes('toomany') && (
                  <small className="text-danger fw-bold d-block mt-1">[Too Many] Maximum of 3 recipes allowed in the system</small>
                )}
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
                          className="form-control"
                          value={ingredient.units}
                          disabled
                        />
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
                      className={`form-control ${generalErrors.includes('ingredients') ? 'is-invalid' : ''}`}
                      placeholder="Ingredient name"
                      value={ingredientName}
                      onChange={(e) => setIngredientName(e.target.value)}
                    />
                  </div>
                  <div className="col-4">
                    <input
                      type="number"
                      className={`form-control ${generalErrors.includes('ingredients') ? 'is-invalid' : ''}`}
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
                {generalErrors.includes('ingredients') && (
                  <small className="text-danger fw-bold d-block mt-1">[No Ingredients] Recipe must have at least one ingredient</small>
                )}
                {generalErrors.includes('savefailed') && (
                  <small className="text-danger fw-bold d-block mt-1">Failed to save recipe. Please try again.</small>
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

export default RecipeComponent