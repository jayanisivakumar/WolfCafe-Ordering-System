import axios from "axios"

/** Base URL for the Recipe API - Correspond to methods in Backend's Recipe Controller. */
const REST_API_BASE_URL = "http://localhost:8080/api/recipes"

/** GET Recipes - lists all recipes */
export const listRecipes = () => axios.get(REST_API_BASE_URL)

/** POST Recipe - creates a new recipe */
export const createRecipe = (recipe) => axios.post(REST_API_BASE_URL, recipe)

/** GET Recipe - gets a single recipe by id */
export const getRecipe = (id) => axios.get(REST_API_BASE_URL + "/" + id)

/** GET Recipe by name - uses controller path /name/{name} */
export const getRecipeByName = (name) => axios.get(REST_API_BASE_URL + "/name/" + name)

/** PUT Recipe - updates a recipe by id */
export const updateRecipe = (id, recipe) => axios.put(REST_API_BASE_URL + "/" + id, recipe)

/** DELETE Recipe - deletes the recipe with the given id */
export const deleteRecipe = (id) => axios.delete(REST_API_BASE_URL + "/" + id)