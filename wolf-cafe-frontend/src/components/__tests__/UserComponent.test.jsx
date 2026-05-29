import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import UserComponent from '../UserComponent'
import { getAllUsers, deleteUser, createStaff } from '../../services/UserService'
import { describe, test, expect, beforeEach, vi } from 'vitest'
import { BrowserRouter } from 'react-router-dom'

vi.mock('../../services/UserService', () => ({
  getAllUsers: vi.fn(),
  deleteUser: vi.fn(),
  createStaff: vi.fn(),
}))

const localStorageMock = (() => {
  let store = {}
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => {
      store[key] = value.toString()
    },
    removeItem: (key) => {
      delete store[key]
    },
    clear: () => {
      store = {}
    },
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
})

const renderWithRouter = (ui) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  )
}

describe('UserComponent', () => {
  const mockUsers = [
    {
      id: 1,
      name: 'Admin User',
      username: 'admin',
      email: 'admin@admin.edu',
      role: 'ROLE_ADMIN',
    },
    {
      id: 2,
      name: 'Staff User',
      username: 'staff1',
      email: 'staff1@wolfcafe.com',
      role: 'ROLE_STAFF',
    },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    window.localStorage.clear()
    window.localStorage.setItem('role', 'ROLE_ADMIN')
    getAllUsers.mockResolvedValue({ data: mockUsers })
  })

  test('renders users on page load', async () => {
    renderWithRouter(<UserComponent />)

    expect(await screen.findByText(/admin user/i)).toBeInTheDocument()
    expect(screen.getByText('Staff User')).toBeInTheDocument()
    expect(screen.getByText(/admin@admin.edu/i)).toBeInTheDocument()
    expect(screen.getByText(/staff1@wolfcafe.com/i)).toBeInTheDocument()
  })

  test('cancel clears create form fields', async () => {
    renderWithRouter(<UserComponent />)

    await userEvent.click(await screen.findByText(/create new staff user/i))

    const nameInput = screen.getByLabelText('Name')
    const usernameInput = screen.getByLabelText('Username')
    const emailInput = screen.getByLabelText('Email')
    const passwordInput = screen.getByLabelText('Password')

    await userEvent.type(nameInput, 'New Staff')
    await userEvent.type(usernameInput, 'newstaff')
    await userEvent.type(emailInput, 'newstaff@wolfcafe.com')
    await userEvent.type(passwordInput, 'password123')

    await userEvent.click(screen.getByText(/cancel/i))

    await userEvent.click(screen.getByText(/create new staff user/i))

    expect(screen.getByLabelText('Name')).toHaveValue('')
    expect(screen.getByLabelText('Username')).toHaveValue('')
    expect(screen.getByLabelText('Email')).toHaveValue('')
    expect(screen.getByLabelText('Password')).toHaveValue('')
  })

  test('opens create staff user card', async () => {
    renderWithRouter(<UserComponent />)

    const button = await screen.findByText(/create new staff user/i)
    await userEvent.click(button)

    expect(screen.getByText(/create staff user/i)).toBeInTheDocument()
    expect(screen.getByLabelText('Name')).toBeInTheDocument()
    expect(screen.getByLabelText('Username')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
  })

  test('cancel closes create card', async () => {
    renderWithRouter(<UserComponent />)

    await userEvent.click(await screen.findByText(/create new staff user/i))
    await userEvent.click(screen.getByText(/cancel/i))

    expect(screen.queryByText(/create staff user/i)).not.toBeInTheDocument()
    expect(screen.getByText(/create new staff user/i)).toBeInTheDocument()
  })

  test('shows error for duplicate username', async () => {
    renderWithRouter(<UserComponent />)

    await userEvent.click(await screen.findByText(/create new staff user/i))

    await userEvent.type(screen.getByLabelText('Name'), 'New Staff')
    await userEvent.type(screen.getByLabelText('Username'), 'admin')
    await userEvent.type(screen.getByLabelText('Email'), 'newstaff@wolfcafe.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password123')

    await userEvent.click(screen.getByText(/save/i))

    expect(screen.getByText(/that username already exists/i)).toBeInTheDocument()
    expect(createStaff).not.toHaveBeenCalled()
  })

  test('shows error for duplicate email', async () => {
    renderWithRouter(<UserComponent />)

    await userEvent.click(await screen.findByText(/create new staff user/i))

    await userEvent.type(screen.getByLabelText('Name'), 'New Staff')
    await userEvent.type(screen.getByLabelText('Username'), 'newstaff')
    await userEvent.type(screen.getByLabelText('Email'), 'admin@admin.edu')
    await userEvent.type(screen.getByLabelText('Password'), 'password123')

    await userEvent.click(screen.getByText(/save/i))

    expect(screen.getByText(/that email already exists/i)).toBeInTheDocument()
    expect(createStaff).not.toHaveBeenCalled()
  })

  test('creates staff user successfully', async () => {
    createStaff.mockResolvedValue({ data: {} })

    renderWithRouter(<UserComponent />)

    await userEvent.click(await screen.findByText(/create new staff user/i))

    await userEvent.type(screen.getByLabelText('Name'), 'New Staff')
    await userEvent.type(screen.getByLabelText('Username'), 'newstaff')
    await userEvent.type(screen.getByLabelText('Email'), 'newstaff@wolfcafe.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password123')

    await userEvent.click(screen.getByText(/save/i))

    await waitFor(() => {
      expect(createStaff).toHaveBeenCalledWith({
        name: 'New Staff',
        username: 'newstaff',
        email: 'newstaff@wolfcafe.com',
        password: 'password123',
        role: 'ROLE_STAFF',
      })
    })

    expect(getAllUsers).toHaveBeenCalledTimes(2)
  })
})
