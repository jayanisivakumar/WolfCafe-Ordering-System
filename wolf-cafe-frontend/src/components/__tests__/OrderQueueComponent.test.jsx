import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { BrowserRouter } from 'react-router-dom'
import OrderQueueComponent from '../OrderQueueComponent'
import { cancelOrder, fulfillOrder, getOrder, getPendingOrders } from '../../services/OrderService'

vi.mock('../../services/OrderService', () => ({
  getPendingOrders: vi.fn(),
  getOrder: vi.fn(),
  fulfillOrder: vi.fn(),
  cancelOrder: vi.fn(),
}))

const storageMock = () => {
  let store = {}
  return {
    getItem: (key) => (key in store ? store[key] : null),
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
}

Object.defineProperty(window, 'localStorage', {
  value: storageMock(),
  configurable: true,
})

Object.defineProperty(window, 'sessionStorage', {
  value: storageMock(),
  configurable: true,
})

const renderWithRouter = (ui) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  )
}

describe('OrderQueueComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.localStorage.clear()
    window.sessionStorage.clear()
    window.localStorage.setItem('role', 'ROLE_STAFF')
    window.sessionStorage.setItem('role', 'ROLE_STAFF')
  })

  test('renders pending orders for staff', async () => {
    getPendingOrders.mockResolvedValue({
      data: [
        { id: 1023, customerName: 'Shreeya', total: 5.0, status: 'PENDING' },
        { id: 1025, customerName: 'Jayani', total: 3.0, status: 'PENDING' },
      ],
    })

    renderWithRouter(<OrderQueueComponent />)

    expect(await screen.findByText(/orders/i)).toBeInTheDocument()
    expect(screen.getByText('Shreeya')).toBeInTheDocument()
    expect(screen.getByText('Jayani')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /fulfill order/i })).toHaveLength(2)
  })

  test('clicking order id shows order details', async () => {
    const user = userEvent.setup()
    getPendingOrders.mockResolvedValue({
      data: [{ id: 1023, customerName: 'Shreeya', total: 5.0, status: 'PENDING' }],
    })
    getOrder.mockResolvedValue({
      data: {
        id: 1023,
        customerName: 'Shreeya',
        items: [{ itemId: 1, name: 'Latte', quantity: 1, price: 5.0 }],
        subtotal: 5.0,
        tax: 0.1,
        tip: 1.0,
        total: 6.1,
      },
    })

    renderWithRouter(<OrderQueueComponent />)

    await user.click(await screen.findByRole('button', { name: /#1023/i }))

    expect(await screen.findByText(/order #1023/i)).toBeInTheDocument()
    expect(screen.getByText(/customer:/i)).toBeInTheDocument()
    expect(screen.getByText(/items ordered:/i)).toBeInTheDocument()
    expect(screen.getByText(/subtotal:/i)).toBeInTheDocument()
    expect(screen.getByText(/tax:/i)).toBeInTheDocument()
    expect(screen.getByText(/tip:/i)).toBeInTheDocument()
    expect(screen.getByText(/^Total:$/i)).toBeInTheDocument()
  })

  test('fulfilling an order removes it and shows success message', async () => {
    const user = userEvent.setup()
    getPendingOrders.mockResolvedValue({
      data: [
        { id: 1023, customerName: 'Shreeya', total: 5.0, status: 'PENDING' },
        { id: 1025, customerName: 'Jayani', total: 3.0, status: 'PENDING' },
      ],
    })
    fulfillOrder.mockResolvedValue({
      data: { id: 1025, status: 'READY_FOR_PICKUP' },
    })

    renderWithRouter(<OrderQueueComponent />)

    const fulfillButtons = await screen.findAllByRole('button', { name: /fulfill order/i })
    await user.click(fulfillButtons[1])

    await waitFor(() => {
      expect(screen.getByText(/order #1025 has been fulfilled!/i)).toBeInTheDocument()
    })

    expect(screen.queryByRole('button', { name: /#1025/i })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: /#1023/i })).toBeInTheDocument()
  })

  test('shows empty state when no pending orders remain', async () => {
    getPendingOrders.mockResolvedValue({
      data: [],
    })

    renderWithRouter(<OrderQueueComponent />)

    expect(await screen.findByText(/no pending orders!/i)).toBeInTheDocument()
    expect(screen.getByText(/come back when a customer places an order/i)).toBeInTheDocument()
  })

  test('cancels an order after confirmation', async () => {
    const user = userEvent.setup()
    getPendingOrders.mockResolvedValue({
      data: [
        { id: 1023, customerName: 'Shreeya', total: 5.0, status: 'PENDING' },
      ],
    })
    cancelOrder.mockResolvedValue({
      data: { id: 1023, status: 'CANCELLED' },
    })

    renderWithRouter(<OrderQueueComponent />)

    await user.click(await screen.findByRole('button', { name: /cancel/i }))
    expect(await screen.findByText(/are you sure you want to cancel this order/i)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /^yes$/i }))

    await waitFor(() => {
      expect(screen.getAllByText(/order #1023 has been cancelled!/i).length).toBeGreaterThan(0)
    })

    expect(screen.queryByRole('button', { name: /#1023/i })).not.toBeInTheDocument()
  })

  test('shows backend error when fulfilling an order fails', async () => {
    const user = userEvent.setup()
    getPendingOrders.mockResolvedValue({
      data: [
        { id: 1023, customerName: 'Shreeya', total: 5.0, status: 'PENDING' },
      ],
    })
    fulfillOrder.mockRejectedValue({
      response: {
        data: {
          error: 'Insufficient inventory to fulfill this order. Please cancel this order.',
        },
      },
    })

    renderWithRouter(<OrderQueueComponent />)

    await user.click(await screen.findByRole('button', { name: /fulfill order/i }))

    expect(await screen.findByText(/insufficient inventory to fulfill this order\. please cancel this order\./i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /#1023/i })).toBeInTheDocument()
  })

  test('shows backend error when cancelling an order fails', async () => {
    const user = userEvent.setup()
    getPendingOrders.mockResolvedValue({
      data: [
        { id: 1023, customerName: 'Shreeya', total: 5.0, status: 'PENDING' },
      ],
    })
    cancelOrder.mockRejectedValue({
      response: {
        data: {
          error: 'Order cannot be cancelled in its current state: CANCELLED',
        },
      },
    })

    renderWithRouter(<OrderQueueComponent />)

    await user.click(await screen.findByRole('button', { name: /cancel/i }))
    await user.click(await screen.findByRole('button', { name: /^yes$/i }))

    expect(await screen.findByText(/order cannot be cancelled in its current state: cancelled/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /#1023/i })).toBeInTheDocument()
  })

  test('non-staff users are unauthorized', async () => {
    window.localStorage.clear()
    window.sessionStorage.clear()
    window.localStorage.setItem('role', 'ROLE_CUSTOMER')
    window.sessionStorage.setItem('role', 'ROLE_CUSTOMER')

    renderWithRouter(<OrderQueueComponent />)

    expect(screen.getByText(/unauthorized/i)).toBeInTheDocument()
  })
})
