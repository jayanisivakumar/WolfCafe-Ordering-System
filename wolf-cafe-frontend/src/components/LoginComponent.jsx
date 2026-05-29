/**
 * Login component that handles user authentication.
 * Stores JWT token and user role upon successful login.
 * 
 * @author Jayani Sivakumar
 */

import React, { useState } from 'react'
import { loginAPICall, saveLoggedInUser, storeToken } from '../services/AuthService'
import { useNavigate } from 'react-router-dom'

const LoginComponent = () => {

    const [usernameOrEmail, setUsernameOrEmail] = useState('')
    const [password, setPassword] = useState('')

    const navigator = useNavigate()

	/**
     * Handles login form submission.
     * Sends credentials to backend and stores authentication data.
     * 
     * @param {Event} e Form submission event
     */
    async function handleLoginForm(e) {
        e.preventDefault()

        const loginObj = {usernameOrEmail, password}

        console.log(loginObj)

        await loginAPICall(usernameOrEmail, password).then((response) => {
            console.log(response.data)

            // const token = 'Basic ' + window.btoa(usernameOrEmail + ':' + password);
            const token = response.data.accessToken

            const role = response.data.role

            storeToken(token)
			localStorage.setItem("role", role)
            saveLoggedInUser(usernameOrEmail, role)

            navigator('/items')
			
            //window.location.reload(false)
        }).catch(error => {
            console.error('ERROR1' + error)
        })
    }


  return (
    <div className='container'>
        <br /><br />
        <div className='row'>
            <div className='col-md-6 offset-md-3 offset-md-3'>
                <div className='card'>
                    <div className='card-header'>
                        <h2 className='text-center'>Login Form</h2>
                    </div>
                    <div className='card-body'>
                        <form>
                            <div className='row mb-3'>
                                <label className='col-md-3 control-label'>Username</label>
                                <div className='col-md-9'>
                                    <input
                                        type='text'
                                        name='usernameOrEmail'
                                        className='form-control'
                                        placeholder='Enter username or email'
                                        value={usernameOrEmail}
                                        onChange={(e) => setUsernameOrEmail(e.target.value)}
                                    >
                                    </input>
                                </div>
                            </div>

                            <div className='row mb-3'>
                                <label className='col-md-3 control-label'>Password</label>
                                <div className='col-md-9'>
                                    <input
                                        type='password'
                                        name='password'
                                        className='form-control'
                                        placeholder='Enter password'
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                    >
                                    </input>
                                </div>
                            </div>

                            <div className='form-group mb-3'>
                                <button className='btn btn-primary' onClick={(e) => handleLoginForm(e)}>Submit</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
  )
}

export default LoginComponent
