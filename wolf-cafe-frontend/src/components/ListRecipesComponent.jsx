import React, { useEffect, useState } from 'react'
import { listRecipes, deleteRecipe } from '../services/RecipesService'
import { useNavigate } from 'react-router-dom'

/**
 * Displays all recipes in the system.
 * 
 * Provides functionality to:
 * - View all recipes
 * - Search and filter recipes
 * - Mark recipes as favorites
 * - Navigate to add or edit recipe pages
 * - Delete existing recipes
 * 
 * Data is retrieved from the backend via the RecipesService.
 * 
 * @author Monica Jin
 */
const ListRecipesComponent = () => {

    const [recipes, setRecipes] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [filterBy, setFilterBy] = useState('all')
    const [favorites, setFavorites] = useState(JSON.parse(localStorage.getItem('favorites') || '[]'))

    const navigator = useNavigate();

    useEffect(() => {
        getAllRecipes()
    }, [])

	/**
	 * Retrieves all recipes from the backend API
	 * and updates the component state.
	 */
    function getAllRecipes() {
        listRecipes().then((response) => {
            setRecipes(response.data)
        }).catch(error => {
            console.error(error)
        })
    }

	/**
	 * Navigates the user to the Add Recipe page.
	 */
    function addNewRecipe() {
        navigator('/add-recipe')
    }

	/**
	 * Deletes a recipe by its ID after user confirmation.
	 * Refreshes the recipe list upon successful deletion.
	 *
	 * @param {number} id - The unique identifier of the recipe to delete.
	 */
    function removeRecipe(id) {
        if (!window.confirm('Are you sure you want to delete this recipe?')) return
        
        console.log(id)

        deleteRecipe(id).then((response) => {
            getAllRecipes()
            showToast('Recipe deleted successfully!', 'success')
        }).catch(error => {
            console.error(error)
            showToast('Failed to delete recipe', 'error')
        })
    }

	/**
	 * Navigates the user to the Edit Recipe page for the selected recipe.
	 * The recipe name is URL-encoded to ensure safe routing.
	 *
	 * @param {string} name - The name of the recipe to edit.
	 */
    function editRecipe(name) {
        console.log(name)
        // encode name to make it safe for URLs (spaces, special chars)
        navigator(`/edit-recipe/${encodeURIComponent(name)}`)
    }	

	/**
	 * Toggles the favorite status of a recipe.
	 * Stores favorite recipe IDs in localStorage.
	 *
	 * @param {number} id - The unique identifier of the recipe.
	 */
    function toggleFavorite(id) {
        const newFavorites = favorites.includes(id) 
            ? favorites.filter(fav => fav !== id)
            : [...favorites, id]
        setFavorites(newFavorites)
        localStorage.setItem('favorites', JSON.stringify(newFavorites))
    }

	/**
	 * Displays a temporary toast notification message.
	 *
	 * @param {string} message - The message to display.
	 * @param {string} type - The type of notification (e.g., 'success', 'error').
	 */
    function showToast(message, type) {
        // Simple toast notification
        const toast = document.createElement('div')
        toast.className = `toast-notification ${type}`
        toast.textContent = message
        document.body.appendChild(toast)
        setTimeout(() => toast.remove(), 3000)
    }

	/**
	 * Filters recipes based on search input and selected filter type.
	 *
	 * @returns {Array} A filtered list of recipes.
	 */
    function getFilteredRecipes() {
        return recipes.filter(recipe => {
            const matchesSearch = recipe.name.toLowerCase().includes(searchTerm.toLowerCase())
            const matchesFilter = filterBy === 'all' || 
                (filterBy === 'favorites' && favorites.includes(recipe.id)) ||
                (filterBy === 'affordable' && recipe.price <= 5) ||
                (filterBy === 'premium' && recipe.price > 5)
            return matchesSearch && matchesFilter
        })
    }
	
	/**
	 * Formats the ingredients list into a readable string.
	 *
	 * @param {Array} ingredients - The list of ingredient objects.
	 * @returns {string} A formatted string of ingredients.
	 */
	function formatIngredients(ingredients) {
        if (!ingredients || ingredients.length === 0) {
            return "No ingredients"
        }
        return ingredients.map(ing => `${ing.name}(${ing.amount})`).join(", ")
	}

    return (
        <div className="container">
            <h2 className="text-center mb-4">☕ Coffee Recipes</h2>
            
            <div className="row mb-3">
                <div className="col-md-6">
                    <input 
                        type="text" 
                        className="form-control" 
                        placeholder="🔍 Search recipes..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="col-md-3">
                    <select 
                        className="form-select" 
                        value={filterBy}
                        onChange={(e) => setFilterBy(e.target.value)}
                    >
                        <option value="all">All Recipes</option>
                        <option value="favorites">⭐ Favorites</option>
                        <option value="affordable">💰 Affordable ($5 or less)</option>
                        <option value="premium">👑 Premium (Over $5)</option>
                    </select>
                </div>
                <div className="col-md-3">
                    <button className="btn btn-primary w-100" onClick={ addNewRecipe }>
                        ➕ Add Recipe
                    </button>
                </div>
            </div>
            <table className="table table-hover table-bordered">
                <thead className="table-dark">
                    <tr>
                        <th>⭐</th>
                        <th>Recipe Name</th>
                        <th>Price</th>
                        <th>Ingredients</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {
                        getFilteredRecipes().map(recipe => 
                        <tr key={recipe.id}>
                            <td>
                                <button 
                                    className="btn btn-sm btn-link p-0"
                                    onClick={() => toggleFavorite(recipe.id)}
                                    style={{fontSize: '1.5rem'}}
                                >
                                    {favorites.includes(recipe.id) ? '⭐' : '☆'}
                                </button>
                            </td>
                            <td><strong>{recipe.name}</strong></td>
                            <td><span className="badge bg-success">${recipe.price}</span></td>
                            <td>{formatIngredients(recipe.ingredients)}</td>
                            <td>
                                <button className='btn btn-info' onClick={() => editRecipe(recipe.name)}>Edit</button>
                                <button 
                                    className="btn btn-danger btn-sm" 
                                    onClick={() => removeRecipe(recipe.id)}
                                >
                                    🗑️ Delete
                                </button>
                                
                            </td>
                        </tr>)
                    }
                </tbody>
            </table>

            {getFilteredRecipes().length === 0 && (
                <div className="alert alert-info text-center">
                    <h4>No recipes found</h4>
                    <p>Try adjusting your search or filter, or add a new recipe!</p>
                </div>
            )}
        </div>
    )

}

export default ListRecipesComponent