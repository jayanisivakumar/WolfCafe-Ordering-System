import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registerAPICall } from '../services/AuthService'

const RegisterComponent = () => {

    const [name, setName] = useState('')
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [showSuccessPopup, setShowSuccessPopup] = useState(false)

    const navigate = useNavigate()

    function handleRegistrationForm(e) {
        e.preventDefault();

        const register = {name, username, email, password}

        console.log(register)

        registerAPICall(register).then((response) => {
            console.log(response.data)
            setShowSuccessPopup(true)
        }).catch(error => {
            console.error(error)
        })
    }

  return (
    <div className='container'>
        <br /><br />
        <div className='row'>
            <div className='col-md-6 offset-md-3 offset-md-3'>
                <div className='card'>
                    <div className='card-header'>
                        <h2 className='text-center'>User Registration Form</h2>
                    </div>
                    <div className='card-body'>
                        <form>
                            <div className='row mb-3'>
                                <label className='col-md-3 control-label'>Name</label>
                                <div className='col-md-9'>
                                    <input
                                        type='text'
                                        name='name'
                                        className='form-control'
                                        placeholder='Enter name'
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                    >
                                    </input>
                                </div>
                            </div>

                            <div className='row mb-3'>
                                <label className='col-md-3 control-label'>Username</label>
                                <div className='col-md-9'>
                                    <input
                                        type='text'
                                        name='username'
                                        className='form-control'
                                        placeholder='Enter username'
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                    >
                                    </input>
                                </div>
                            </div>

                            <div className='row mb-3'>
                                <label className='col-md-3 control-label'>Email</label>
                                <div className='col-md-9'>
                                    <input
                                        type='text'
                                        name='email'
                                        className='form-control'
                                        placeholder='Enter email'
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
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
                                <button className='btn btn-primary' onClick={(e) => handleRegistrationForm(e)}>Submit</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        {showSuccessPopup && (
            <div
                style={{
                    position: 'fixed',
                    inset: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.4)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1100
                }}
            >
                <div className='card shadow' style={{ width: '420px', maxWidth: '92vw' }}>
                    <div className='card-body text-center p-4'>
                        <h3 className='mb-3'>Account created successfully!</h3>
                        <button
                            type='button'
                            className='btn btn-primary'
                            onClick={() => navigate('/login')}
                        >
                            Go to Login
                        </button>
                    </div>
                </div>
            </div>
        )}
    </div>
  )
}

export default RegisterComponent
