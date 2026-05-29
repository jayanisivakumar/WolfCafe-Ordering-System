/**
 * Main application component that defines routing and layout for WolfCafe.
 * Handles protected routes using authentication checks.
 * 
 * @author Jayani Sivakumar
 * @author Shreeya Wadodkar
 * @author Kyle Dunn
 */
import './App.css'
import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom'
import ListRecipesComponent from './components/ListRecipesComponent'
import HeaderComponent from './components/HeaderComponent'
import FooterComponent from './components/FooterComponent'
import ListItemsComponent from './components/ListItemsComponent'
import ItemComponent from './components/ItemComponent'
import RegisterComponent from './components/RegisterComponent'
import LoginComponent from './components/LoginComponent'
import { isUserLoggedIn } from './services/AuthService'
import RecipeComponent from './components/RecipeComponent'
import InventoryComponent from './components/InventoryComponent'
import MakeRecipeComponent from './components/MakeRecipeComponent'
import EditRecipeComponent from './components/EditRecipeComponent'
import TaxRateComponent from "./components/TaxRateComponent";
import UserComponent from "./components/UserComponent";
import OrdersComponent from './components/OrdersComponent'
import OrderQueueComponent from './components/OrderQueueComponent'

/**
 * Root component for the WolfCafe frontend.
 * Configures routing, authentication guards,
 * and role-based access control.
 */
function App() {
	
  /**
   * Wrapper component to restrict access to authenticated users only.
   * Redirects unauthenticated users to login page.
   * 
   * @param {Object} props React children components
   * @returns Protected route or redirect
   */
  function AuthenticatedRoute({children}) {
    const isAuth = isUserLoggedIn()
	if (isAuth) {
	  return children
	}
	return <Navigate to='/' />
  }
  
  /**
   * Wrapper component to restrict access based on user role.
   * Only allows access if the logged-in user's role matches the required role.
   * Redirects unauthorized users to the login page.
   * 
   * @param {Object} props React children components
   * @param {string} props.allowedRole Role required to access the route (e.g., "ADMIN", "STAFF")
   * @returns Protected route or redirect
   */
  function RoleRoute({ children, allowedRole }) {
    const role = localStorage.getItem("role");

    if (role && role.includes(allowedRole)) {
      return children;
    }

    return <Navigate to="/" />;
  }

  return (
    <>
      <BrowserRouter>
	  <HeaderComponent />
	  <Routes>
	  	<Route path='/' element={<LoginComponent />}></Route>
		<Route path='/register' element={<RegisterComponent />}></Route>
		<Route path='/login' element={<LoginComponent />}></Route>
		<Route path='/items' element={<ListItemsComponent />}></Route>
		<Route path='/orders' element={<AuthenticatedRoute><OrdersComponent /></AuthenticatedRoute>} />
		<Route path='/add-item' element={<AuthenticatedRoute><ItemComponent /></AuthenticatedRoute>}></Route>
		<Route path='/update-item/:id' element={<AuthenticatedRoute><ItemComponent /></AuthenticatedRoute>}></Route>
		<Route path='/recipes' element={<AuthenticatedRoute><ListRecipesComponent /></AuthenticatedRoute>} />
		<Route path='/add-recipe' element={<AuthenticatedRoute><RecipeComponent /></AuthenticatedRoute>} />
		<Route path='/inventory' element={
		  <AuthenticatedRoute>
		    <RoleRoute allowedRole="STAFF">
		      <InventoryComponent />
		    </RoleRoute>
		  </AuthenticatedRoute>
		} />
		<Route path='/order-queue' element={
		  <AuthenticatedRoute>
		    <RoleRoute allowedRole="STAFF">
		      <OrderQueueComponent />
		    </RoleRoute>
		  </AuthenticatedRoute>
		} />
		<Route path='/make-recipe' element={<AuthenticatedRoute><MakeRecipeComponent /></AuthenticatedRoute>} />
		<Route path='/edit-recipe/:incomingName' element={<AuthenticatedRoute><EditRecipeComponent /></AuthenticatedRoute>} />
		<Route path='/settings' element={
		  <AuthenticatedRoute>
		    <RoleRoute allowedRole="ADMIN">
		      <TaxRateComponent />
		    </RoleRoute>
		  </AuthenticatedRoute>
		} /> 
		<Route path='/users' element={
		  <AuthenticatedRoute>
		    <RoleRoute allowedRole="ADMIN">
		      <UserComponent />
		    </RoleRoute>
		  </AuthenticatedRoute>
		} /> 
	  </Routes>
	  <FooterComponent />
	  </BrowserRouter>
    </>
  )
}

export default App
