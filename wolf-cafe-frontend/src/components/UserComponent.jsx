import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllUsers, deleteUser, createStaff, updateUser } from '../services/UserService'

/**
 * Displays and manages users in the system.
 * Supports listing users, creating staff users, editing customer/staff users,
 * and deleting non-admin users.
 *
 * @returns {JSX.Element} rendered user management component
 */
const UserComponent = () => {
    const [users, setUsers] = useState([])
    const [showCreateCard, setShowCreateCard] = useState(false)

    const [name, setName] = useState('')
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [errorMessage, setErrorMessage] = useState('')

    const [editingUserId, setEditingUserId] = useState(null)
    const [editName, setEditName] = useState('')
    const [editUsername, setEditUsername] = useState('')
    const [editEmail, setEditEmail] = useState('')
    const [editPassword, setEditPassword] = useState('')
    const [editErrorMessage, setEditErrorMessage] = useState('')

    const navigate = useNavigate()

    useEffect(() => {
        const role = localStorage.getItem('role')

        if (role && !isCurrentUserAdmin()) {
            navigate('/unauthorized')
            return
        }

        listUsers()
    }, [navigate])

    function isCurrentUserAdmin() {
        const role = localStorage.getItem('role')
        return role && role.includes('ADMIN')
    }

    /**
     * Retrieves all users from the backend and stores them in state.
     */
    function listUsers() {
        getAllUsers()
            .then((response) => {
                setUsers(response.data)
            })
            .catch((error) => {
                console.error(error)
            })
    }

    /**
     * Builds a display string for a user's role or roles.
     *
     * @param {Object} user the user whose role information is being displayed
     * @returns {string} comma-separated role names, single role name, or 'No Role'
     */
    function getRoleDisplay(user) {
        if (user.roles && user.roles.length > 0) {
            return user.roles.map((role) => role.name).join(', ')
        }

        if (user.role) {
            return user.role
        }

        return 'No Role'
    }

    /**
     * Checks whether the given user has the CUSTOMER role.
     *
     * @param {Object} user the user to check
     * @returns {boolean} true if the user is a customer, false otherwise
     */
    function isCustomer(user) {
        const role = getRoleDisplay(user)
        return role && role.includes('CUSTOMER')
    }

    /**
     * Checks whether the given user has the STAFF role.
     *
     * @param {Object} user the user to check
     * @returns {boolean} true if the user is staff, false otherwise
     */
    function isStaff(user) {
        const role = getRoleDisplay(user)
        return role && role.includes('STAFF')
    }

    /**
     * Checks whether the given user has the ADMIN role.
     *
     * @param {Object} user the user to check
     * @returns {boolean} true if the user is an admin, false otherwise
     */
    function isAdmin(user) {
        const role = getRoleDisplay(user)
        return role && role.includes('ADMIN')
    }

    /**
     * Deletes a user by id unless the user is an admin.
     * Refreshes the user list after successful deletion.
     *
     * @param {number} id the id of the user to delete
     */
    function removeUser(id) {
        const selectedUser = users.find((user) => user.id === id)

        if (selectedUser && isAdmin(selectedUser)) {
            return
        }

        const confirmed = window.confirm('Are you sure you want to delete this user?')

        if (!confirmed) {
            return
        }

        deleteUser(id)
            .then(() => {
                listUsers()
            })
            .catch((error) => {
                console.error(error)
            })
    }

    /**
     * Creates a new staff user after checking for required fields,
     * duplicate username, and duplicate email.
     * Displays validation or server error messages when creation fails.
     *
     * @param {Object} e the event triggered by the save action
     */
    function saveStaffUser(e) {
        e.preventDefault()
        setErrorMessage('')

        if (!name.trim() || !username.trim() || !email.trim() || !password.trim()) {
            setErrorMessage('All fields (name, username, email, and password) are required.')
            return
        }

        const existingUser = users.find(
            (user) =>
                user.username &&
                user.username.toLowerCase() === username.toLowerCase()
        )

        if (existingUser) {
            setErrorMessage('That username already exists.')
            return
        }

        const existingEmail = users.find(
            (user) => user.email && user.email.toLowerCase() === email.toLowerCase()
        )

        if (existingEmail) {
            setErrorMessage('That email already exists.')
            return
        }

        const user = {
            name,
            username,
            email,
            password,
            role: 'ROLE_STAFF'
        }

        createStaff(user)
            .then(() => {
                cancelCreate()
                listUsers()
            })
            .catch((error) => {
                console.error(error)

                if (error.response?.data?.message) {
                    setErrorMessage(error.response.data.message)
                } else if (error.response?.status === 400) {
                    setErrorMessage('Unable to create user. Username or email may already exist.')
                } else {
                    setErrorMessage('Failed to create user.')
                }
            })
    }

    /**
     * Hides the create-user form and clears all create-user input fields and errors.
     */
    function cancelCreate() {
        setShowCreateCard(false)
        setName('')
        setUsername('')
        setEmail('')
        setPassword('')
        setErrorMessage('')
    }

    /**
     * Puts the selected user into edit mode and populates the edit form fields.
     *
     * @param {Object} user the user to edit
     */
    function startEditUser(user) {
        setEditingUserId(user.id)
        setEditName(user.name || '')
        setEditUsername(user.username || '')
        setEditEmail(user.email || '')
        setEditPassword('')
        setEditErrorMessage('')
    }

    /**
     * Exits edit mode and clears all edit form fields and errors.
     */
    function cancelEdit() {
        setEditingUserId(null)
        setEditName('')
        setEditUsername('')
        setEditEmail('')
        setEditPassword('')
        setEditErrorMessage('')
    }

    /**
     * Saves updates for an existing user after checking for required fields
     * other than password, duplicate username, and duplicate email.
     * Refreshes the user list after a successful update.
     *
     * @param {Object} e the event triggered by the save action
     * @param {Object} user the original user being edited
     */
    function saveEditedUser(e, user) {
        e.preventDefault()
        setEditErrorMessage('')

        if (!editName.trim() || !editUsername.trim() || !editEmail.trim()) {
            setEditErrorMessage('Name, username, and email are required.')
            return
        }

        const duplicateUsername = users.find(
            (u) =>
                u.id !== user.id &&
                u.username &&
                u.username.toLowerCase() === editUsername.toLowerCase()
        )

        if (duplicateUsername) {
            setEditErrorMessage('That username already exists.')
            return
        }

        const duplicateEmail = users.find(
            (u) =>
                u.id !== user.id &&
                u.email &&
                u.email.toLowerCase() === editEmail.toLowerCase()
        )

        if (duplicateEmail) {
            setEditErrorMessage('That email already exists.')
            return
        }

        const updatedUser = {
            name: editName,
            username: editUsername,
            email: editEmail,
            password: editPassword,
            role: getRoleDisplay(user)
        }

        updateUser(user.id, updatedUser)
            .then(() => {
                cancelEdit()
                listUsers()
            })
            .catch((error) => {
                console.error(error)

                if (error.response?.data?.message) {
                    setEditErrorMessage(error.response.data.message)
                } else if (error.response?.status === 400) {
                    setEditErrorMessage('Unable to update user.')
                } else {
                    setEditErrorMessage('Failed to update user.')
                }
            })
    }

    /**
     * Returns the appropriate title for the edit form based on the user's role.
     *
     * @param {Object} user the user being edited
     * @returns {string} the edit form title
     */
    function getEditTitle(user) {
        if (isStaff(user)) {
            return 'Edit Staff User'
        }

        if (isCustomer(user)) {
            return 'Edit Customer'
        }

        return 'Edit User'
    }

    return (
        <div className='container'>
            <br /><br />
            <h2 className='text-center'>Users</h2>

            <div className='row'>
                {users.map((user) => (
                    <div className='col-md-4 mb-4' key={user.id}>
                        <div className='card h-100 shadow'>
                            {editingUserId === user.id ? (
                                <div className='card-body'>
                                    <h5>{getEditTitle(user)}</h5>

                                    {editErrorMessage && (
                                        <div className='alert alert-danger py-2'>
                                            {editErrorMessage}
                                        </div>
                                    )}

                                    <div className='mb-2'>
                                        <label htmlFor={`edit-name-${user.id}`}>Name</label>
                                        <input
                                            id={`edit-name-${user.id}`}
                                            className='form-control'
                                            value={editName}
                                            onChange={(e) => setEditName(e.target.value)}
                                        />
                                    </div>

                                    <div className='mb-2'>
                                        <label htmlFor={`edit-username-${user.id}`}>Username</label>
                                        <input
                                            id={`edit-username-${user.id}`}
                                            className='form-control'
                                            value={editUsername}
                                            onChange={(e) => setEditUsername(e.target.value)}
                                        />
                                    </div>

                                    <div className='mb-2'>
                                        <label htmlFor={`edit-email-${user.id}`}>Email</label>
                                        <input
                                            id={`edit-email-${user.id}`}
                                            type='email'
                                            className='form-control'
                                            value={editEmail}
                                            onChange={(e) => setEditEmail(e.target.value)}
                                        />
                                    </div>

                                    <div className='mb-3'>
                                        <label htmlFor={`edit-password-${user.id}`}>Password</label>
                                        <input
                                            id={`edit-password-${user.id}`}
                                            type='password'
                                            className='form-control'
                                            value={editPassword}
                                            onChange={(e) => setEditPassword(e.target.value)}
                                            placeholder='Enter new password'
                                        />
                                    </div>

                                    <div className='text-center'>
                                        <button
                                            className='btn btn-success me-2'
                                            onClick={(e) => saveEditedUser(e, user)}
                                        >
                                            Save
                                        </button>

                                        <button
                                            className='btn btn-secondary'
                                            onClick={cancelEdit}
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <>
                                    <div className='card-body'>
                                        <h5>
                                            <strong>Name:</strong> {user.name || 'Unknown'}
                                        </h5>

                                        <p>
                                            <strong>Username:</strong> {user.username}
                                        </p>

                                        <p>
                                            <strong>Email:</strong> {user.email}
                                        </p>

                                        <p>
                                            <strong>Role:</strong> {getRoleDisplay(user)}
                                        </p>
                                    </div>

                                    <div className='card-footer text-center'>
                                        {isCustomer(user) && (
                                            <button
                                                className='btn btn-warning me-2'
                                                onClick={() => startEditUser(user)}
                                            >
                                                Edit Customer
                                            </button>
                                        )}

                                        {isStaff(user) && (
                                            <button
                                                className='btn btn-warning me-2'
                                                onClick={() => startEditUser(user)}
                                            >
                                                Edit Staff
                                            </button>
                                        )}

                                        {!isAdmin(user) && (
                                            <button
                                                className='btn btn-danger'
                                                onClick={() => removeUser(user.id)}
                                            >
                                                Delete
                                            </button>
                                        )}
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                ))}

                <div className='col-md-4 mb-4'>
                    <div className='card h-100 shadow'>
                        {!showCreateCard ? (
                            <div className='card-body d-flex flex-column justify-content-center text-center'>
                                <button
                                    className='btn btn-primary'
                                    onClick={() => setShowCreateCard(true)}
                                >
                                    Create New Staff User
                                </button>
                            </div>
                        ) : (
                            <div className='card-body'>
                                <h5>Create Staff User</h5>

                                {errorMessage && (
                                    <div className='alert alert-danger py-2'>
                                        {errorMessage}
                                    </div>
                                )}

                                <div className='mb-2'>
                                    <label htmlFor='name'>Name</label>
                                    <input
                                        id='name'
                                        className='form-control'
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                    />
                                </div>

                                <div className='mb-2'>
                                    <label htmlFor='username'>Username</label>
                                    <input
                                        id='username'
                                        className='form-control'
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                    />
                                </div>

                                <div className='mb-2'>
                                    <label htmlFor='email'>Email</label>
                                    <input
                                        id='email'
                                        type='email'
                                        className='form-control'
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                    />
                                </div>

                                <div className='mb-3'>
                                    <label htmlFor='password'>Password</label>
                                    <input
                                        id='password'
                                        type='password'
                                        className='form-control'
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                    />
                                </div>

                                <div className='text-center'>
                                    <button
                                        className='btn btn-success me-2'
                                        onClick={saveStaffUser}
                                    >
                                        Save
                                    </button>

                                    <button
                                        className='btn btn-secondary'
                                        onClick={cancelCreate}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default UserComponent