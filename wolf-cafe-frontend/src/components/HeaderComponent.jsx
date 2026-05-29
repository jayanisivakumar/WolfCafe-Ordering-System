import React from 'react'
import { NavLink } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import { isUserLoggedIn, logout } from '../services/AuthService'

/**
 * Header component that renders navigation bar based on user role.
 * Displays different options for Admin, Staff, and Customer users.
 * 
 * @author Jayani Sivakumar
 * @author Shreeya Wadodkar
 * @author Kyle Dunn
 */

/**
 * Header component that renders navigation bar based on user role.
 * Displays different options for Admin, Staff, and Customer users.
 * Controls visibility of navigation links based on authentication and authorization.
 */
const HeaderComponent = () => {
	
	const isAuth = isUserLoggedIn()
	const role = localStorage.getItem("role") || sessionStorage.getItem("role");

	/**
     * Handles user logout by clearing authentication data
     * and redirecting to login page.
     */
	function handleLogout() {
	    logout()
		localStorage.removeItem("role")
	    navigator('/login')
	}

    const navigator = useNavigate()

  return (
    <div>
        <header>
            <nav className='navbar navbar-expand-md navbar-dark bg-dark'>
                <div>
                    <a href='http://localhost:3000' className='navbar-brand'>
                        WolfCafe
                    </a>
                </div>
                <div className='collapse navbar-collapse'>
					<ul className='navbar-nav'>
					{
						<li className='nav-item'>
							<NavLink to='/items' className='nav-link'>Items</NavLink>
						</li>
					}
					{
					    isAuth && role && role.includes("CUSTOMER") && (
					        <li className='nav-item'>
					            <NavLink to='/orders' className='nav-link'>My Orders</NavLink>
					        </li>
					    )
					}
					{/* STAFF ONLY */}
					{
						isAuth && role && role.includes("STAFF") && (
                        <li className='nav-item'>
                            <NavLink to='/inventory' className='nav-link'>Inventory</NavLink>
                        </li>
                    )}
					{
						isAuth && role && role.includes("STAFF") && (
                        <li className='nav-item'>
                            <NavLink to='/order-queue' className='nav-link'>Order Queue</NavLink>
                        </li>
                    )}

					</ul>
				</div>
				<ul className='navbar-nav'>
                    {
                        !isAuth && 
                        <li className='nav-item'>
                            <NavLink to='/register' className='nav-link'>Register</NavLink>
                        </li>
                    }
                    {
                        !isAuth &&
                        <li className='nav-item'>
                            <NavLink to='/login' className='nav-link'>Login</NavLink>
                        </li>
                    } 
					{/* ADMIN ONLY */}
					{
						isAuth && role && role.includes("ADMIN") && (
					    <li className='nav-item'>
					        <NavLink to='/users' className='nav-link'>
					            Users
					        </NavLink>
					    </li>
					)}
					{/* ADMIN ONLY */}
					{
						isAuth && role && role.includes("ADMIN") && (
					    <li className='nav-item'>
					        <NavLink to='/settings' className='nav-link'>
					            System Settings
					        </NavLink>
					    </li>
					)}
                    {
                        isAuth &&
                        <li className='nav-item'>
                            <NavLink to='/login' className='nav-link' onClick={handleLogout}>Logout</NavLink>
                        </li>
                    }
                </ul>
            </nav>
        </header>
    </div>
  )
}

export default HeaderComponent
