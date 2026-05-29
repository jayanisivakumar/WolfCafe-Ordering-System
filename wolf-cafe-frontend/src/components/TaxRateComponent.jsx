import React, { useEffect, useState } from "react";
import { getTaxRate, updateTaxRate } from "../services/TaxRateService";

/**
 * Component for managing system tax rate settings (UC2).
 * 
 * Allows an ADMIN user to:
 * - View the current tax rate
 * - Update the tax rate through a modal form
 * - Receive validation feedback and success/error messages
 * 
 * Enforces role-based access control by restricting access to ADMIN users only.
 * Communicates with backend via TaxRateService.
 * 
 * @author Jayani Sivakumar
 */
const TaxRateComponent = () => {
  const [currentRate, setCurrentRate] = useState(0);
  const [newRate, setNewRate] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  
  const role = localStorage.getItem("role");

  // Admin check
  if (!role || !role.includes("ADMIN")) {
	return (
	  <div className="container mt-5 text-center">
	    <div className="alert alert-danger">Unauthorized</div>
	  </div>
	);
  }

  useEffect(() => {
    fetchTaxRate();
  }, []);

  /**
   * Fetches the current tax rate from the backend API
   * and updates the component state.
   * 
   * Called on component mount using useEffect.
   */
  const fetchTaxRate = async () => {
    try {
      const res = await getTaxRate();
      setCurrentRate(res.data.rate);
    } catch (err) {
      console.log(err.response);
    }
  };

  /**
   * Handles saving a new tax rate.
   * Validates input, sends update request to backend,
   * and updates UI with success or error messages.
   */
  const handleSave = async () => {
    setError("");
    setSuccess("");

    if (isNaN(newRate) || newRate <= 0) {
      setError("Tax rate must be a positive number");
      return;
    }

    try {
      const res = await updateTaxRate(parseFloat(newRate));
      setCurrentRate(res.data.rate);
      setSuccess("Tax rate updated successfully!");
      setShowModal(false);
      setNewRate("");
    } catch (err) {
	    console.log(err.response); // DEBUG

	    if (err.response?.status === 400) {
	      setError("Tax rate must be a positive number");
	    } else if (err.response?.status === 401) {
	      setError("Unauthorized - please login again");
	    } else {
	      setError("Server error");
	    }
	  }
  };

  return (
	<div className="container mt-5">
	  <div className="card p-4 shadow-sm text-center">
      <h2>System Settings</h2>

      {/* Table */}
      <table style={{
        margin: "20px auto",
        borderCollapse: "collapse",
        width: "300px",
        backgroundColor: "#f8f8f8",
        border: "1px solid #ccc"
      }}>
        <tbody>
	      <tr>
			<td colSpan="2" style={{ 
			  textAlign: "center", 
			  fontWeight: "bold",
			  backgroundColor: "#eaeaea",
			  padding: "8px" }}>
			  Sales Tax Rate
			</td>
	      </tr>
          <tr>
            <td style={{ padding: "10px", fontWeight: "bold" }}>
              Default Tax Rate:
            </td>
            <td style={{ padding: "10px" }}>2.0%</td>
          </tr>
          <tr>
            <td style={{ padding: "10px", fontWeight: "bold" }}>
              Current Tax Rate:
            </td>
            <td style={{ padding: "10px" }}>{currentRate}%</td>
          </tr>
        </tbody>
      </table>

      {/* Button */}
	  <div className="d-flex justify-content-center mt-3">
	    <button
	      className="btn btn-primary px-4"
	      onClick={() => setShowModal(true)}
	    >
	      Change Tax Rate
	    </button>
	  </div>

      {/* Success */}
      {success && (
		<div className="alert alert-success mt-3">
		  {success}
		</div>
      )}

      {/* Modal */}
      {showModal && (
        <div style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          backgroundColor: "rgba(0,0,0,0.5)",
          display: "flex",
          justifyContent: "center",
          alignItems: "center"
        }}>
          <div className="card p-4" style={{ width: "350px" }}>
            <h3 className="text-center">Change Tax Rate</h3>

			<label htmlFor="taxRateInput" className="form-label">
			  Enter new tax rate (%):
			</label>

			<input
			  id="taxRateInput"
			  type="number"
			  className={`form-control ${error ? "is-invalid" : ""}`}
              value={newRate}
              onChange={(e) => setNewRate(e.target.value)}
            />

            <small className="text-muted">
              Example: 2 for 2%, 5.5 for 5.5%
            </small>

            {error && (
			  <div className="text-danger small">
				{error}
		 	  </div>
            )}

			<div className="d-flex justify-content-between mt-3">
			<button
			  className="btn btn-secondary"
			  onClick={() => setShowModal(false)}
			>
			  Cancel
			</button>

			<button
			  className="btn btn-primary"
			  onClick={handleSave}
			>
			  Save
			</button>
            </div>
          </div>
        </div>
      )}
    </div>
  </div>
  );
};

export default TaxRateComponent;