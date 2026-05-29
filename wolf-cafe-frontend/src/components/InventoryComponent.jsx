import { useState, useEffect } from 'react'
import { getInventory, updateInventory } from '../services/InventoryService'
import { createIngredient } from '../services/IngredientService'
import { useNavigate } from 'react-router-dom';

const formatIngredientName = (name = "") => name
    .toLowerCase()
    .split(" ")
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

/** 
 * Inventory Component for UC5 - Update Inventory
 * Demonstrates layout and functionality for arbitrary ingredients
 * Includes validation to demonstrate both success and error scenarios
 * 
 * @author Jayani Sivakumar
 * @author Sohini Das
 */
const InventoryComponent = () => {
	const navigate = useNavigate();

    const [inventory, setInventory] = useState([]);
    // Fetch inventory from backend on mount
    useEffect(() => {
        fetchInventory();
    }, []);

	/**
	 * Fetches current inventory from backend API.
	 * Transforms the returned ingredient map into
	 * an array format suitable for rendering in the table.
	 *
	 * Sets error message if retrieval fails.
	 */
    const fetchInventory = async () => {
        try {
            const response = await getInventory();
            // Parse backend InventoryDto: { id, ingredients: { name: amount, ... } }
            const ingredientsObj = response.data.ingredients || {};
            const inv = Object.entries(ingredientsObj).map(([name, current]) => ({
                name,
                current,
                toAdd: ""
            }));
            setInventory(inv);
        } catch (err) {
            setMessage("Failed to load inventory from backend");
            setMessageType("error");
        }
    };
    const [message, setMessage] = useState("Yay coffee!");
    const [messageType, setMessageType] = useState("default"); // "default", "success", "error"
    const [errorFieldIndex, setErrorFieldIndex] = useState(null); // Track which field has error
	const [showAddIngredient, setShowAddIngredient] = useState(false);

	// fields for new ingredient (static)
	const [newIngredientName, setNewIngredientName] = useState("");
	const [newIngredientAmount, setNewIngredientAmount] = useState("");
	const [addIngredientError, setAddIngredientError] = useState("");

	/**
	 * Handles submission of inventory updates.
	 *
	 * - Validates all input fields.
	 * - Builds a delta-based ingredient map.
	 * - Prevents empty submissions.
	 * - Sends update request to backend.
	 * - Displays success or error message.
	 *
	 * @param {Event} e - Form submission event
	 */
    async function handleSubmit(e) {
        e.preventDefault();
        if (validateForm()) {
            // Convert inventory array to DTO format for backend
            const ingredientsMap = {};
			inventory.forEach(ingredient => {
		        if (ingredient.toAdd && parseInt(ingredient.toAdd) > 0) {
		            ingredientsMap[ingredient.name] = parseInt(ingredient.toAdd);
		        }
		    });

		    // Prevent empty submission
		    if (Object.keys(ingredientsMap).length === 0) {
		        setMessage("Please enter at least one value to update.");
		        setMessageType("error");
		        return;
		    }
            const inventoryDto = { ingredients: ingredientsMap };
            try {
                await updateInventory(inventoryDto);
                setMessage("Inventory updated successfully!");
                setMessageType("success");
                setErrorFieldIndex(null);
				setTimeout(() => {
			        navigate("/");
			    }, 1500);
                fetchInventory(); // Refresh from backend

            } catch (err) {
                setMessage("Failed to update inventory");
                setMessageType("error");
            }
        }
    }

	/**
	 * Validates all entered inventory values.
	 *
	 * Ensures each value:
	 * - Is numeric
	 * - Is greater than zero
	 * - Is an integer (no decimals)
	 *
	 * If invalid, sets appropriate error message.
	 *
	 * @returns {boolean} true if form is valid, false otherwise
	 */
    function validateForm() {
        // UC5 Alternative Flow [Invalid Unit]: Check for positive integers
        for (let i = 0; i < inventory.length; i++) {
            const ingredient = inventory[i];
            const value = ingredient.toAdd;
            
            if (value !== "" && value !== null && value !== undefined) {
                // Check if it's a valid number
                if (isNaN(value)) {
                    setMessage(`${formatIngredientName(ingredient.name)} amount must be a positive integer`);
                    setMessageType("error");
                    setErrorFieldIndex(i); // Highlight this field
                    return false;
                }
                // Check if it's positive
                else if (parseFloat(value) <= 0) {
                    setMessage(`${formatIngredientName(ingredient.name)} amount must be a positive integer`);
                    setMessageType("error");
                    setErrorFieldIndex(i); // Highlight this field
                    return false;
                }
                // Check if it's an integer (not decimal)
                else if (!Number.isInteger(parseFloat(value))) {
                    setMessage(`${formatIngredientName(ingredient.name)} amount must be a positive integer (no decimals)`);
                    setMessageType("error");
                    setErrorFieldIndex(i); // Highlight this field
                    return false;
                }
            }
        }
        return true;
    }

	/**
	 * Updates the "toAdd" value for a specific ingredient
	 * and clears any existing error message.
	 *
	 * @param {number} index - Index of ingredient in inventory array
	 * @param {string} value - User-entered input value
	 */
    function handleInputChange(index, value) {
        const newInventory = [...inventory];
        newInventory[index].toAdd = value;
        setInventory(newInventory);
        
        // Reset to default message when user starts typing
        setMessage("Yay coffee!");
        setMessageType("default");
        setErrorFieldIndex(null); // Clear error highlighting
    }

	/**
	 * Determines CSS styling class for the feedback message.
	 *
	 * @returns {string} Bootstrap class for success or error message
	 */
    function getMessageClass() {
        if (messageType === "error") return "text-danger fw-bold";
        if (messageType === "success") return "text-success fw-bold";
        return ""; // default - just plain text
    }

    return (
        <div className="container">
            <br /><br />
            <div className="row">
                <div className="card col-md-8 offset-md-2">
                    <h2 className="text-center">Inventory</h2>

                    <div className="card-body">
                        <form onSubmit={handleSubmit}>
                            <table className="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                        <th>Ingredients</th>
                                        <th>Current</th>
                                        <th>Amount to Add</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {inventory.map((ingredient, index) => (
                                        <tr key={index}>
                                            <td className="align-middle">{formatIngredientName(ingredient.name)}</td>
                                            <td className="align-middle text-center">
                                                <strong>{ingredient.current}</strong>
                                            </td>
                                            <td>
                                                <input 
                                                    type="number"
                                                    placeholder="Enter value"
                                                    value={ingredient.toAdd}
                                                    onChange={(e) => handleInputChange(index, e.target.value)}
                                                    className={`form-control ${errorFieldIndex === index ? 'is-invalid border-danger' : ''}`}
                                                    step="1"
                                                />
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            
							<div className="text-center">
							    <button
							        type="button"
							        className="btn btn-secondary btn-lg me-2"
									style={{ backgroundColor: "#6f42c1", borderColor: "#6f42c1" }} // purple
							        onClick={() => setShowAddIngredient(true)}
							    >
							        Add Ingredient
							    </button>

							    <button 
							        className="btn btn-success btn-lg" 
							        type="submit"
							    >
							        Update Inventory
							    </button>
							</div>
                            
                            {/* Message area - shows text at bottom */}
                            <div className="text-center mt-3">
                                <p className={getMessageClass()}>{message}</p>
                            </div>
                        </form>
						{showAddIngredient && (
						    <div
						        style={{
						            position: "fixed",
						            inset: 0,
						            backgroundColor: "rgba(0, 0, 0, 0.4)",
						            display: "flex",
						            alignItems: "center",
						            justifyContent: "center",
						            zIndex: 1000
						        }}
						    >
						        <div className="p-4 border rounded bg-light" style={{ width: "500px" }}>
						            <h3 className="text-center">New Ingredient</h3>

						            <div className="row mt-3">
						                <div className="col-md-6">
						                    <label className="form-label">Name</label>
						                    <input
						                        type="text"
						                        className="form-control"
						                        placeholder="Enter name"
						                        value={newIngredientName}
						                        onChange={(e) => setNewIngredientName(e.target.value)}
						                    />
						                </div>

						                <div className="col-md-6">
						                    <label className="form-label">Initial Amount</label>
						                    <input
						                        type="number"
						                        className="form-control"
						                        placeholder="Enter value"
						                        value={newIngredientAmount}
						                        onChange={(e) => setNewIngredientAmount(e.target.value)}
						                    />
						                </div>
						            </div>

						            {addIngredientError && (
						                <div className="text-danger fw-bold text-center mb-2">{addIngredientError}</div>
						            )}

						            <div className="text-center mt-4">
						                <button
						                    type="button"
						                    className="btn btn-secondary me-2"
						                    style={{ backgroundColor: "#6f42c1", borderColor: "#6f42c1" }} // purple
						                    onClick={async () => {
						                        setAddIngredientError("");
						                        const amount = parseInt(newIngredientAmount);
						                        const trimmedName = newIngredientName.trim();

						                        if (trimmedName === "") {
						                            setAddIngredientError("Ingredient name is required");
						                            return;
						                        }
						                        if (inventory.some(ing => ing.name.toLowerCase() === trimmedName.toLowerCase())) {
						                            setAddIngredientError("Ingredient name already exists");
						                            return;
						                        }
						                        if (isNaN(amount) || amount <= 0) {
						                            setAddIngredientError("Initial amount must be a positive integer");
						                            return;
						                        }
						                        try {
						                            await createIngredient({
						                                name: trimmedName,
						                                amount
						                            });
						                            setNewIngredientName("");
						                            setNewIngredientAmount("");
						                            setShowAddIngredient(false);
						                            setAddIngredientError("");
						                            setMessage("Ingredient added successfully!");
						                            setMessageType("success");
						                            fetchInventory();
						                        } catch (err) {
						                            setAddIngredientError("Failed to add ingredient");
						                        }
						                    }}
						                >
						                    Add Ingredient
						                </button>

						                <button
						                    type="button"
						                    className="btn btn-link"
						                    onClick={() => {
						                        setShowAddIngredient(false);
						                        setAddIngredientError("");
						                    }}
						                >
						                    Cancel
						                </button>
						            </div>
						        </div>
						    </div>
						)}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default InventoryComponent
