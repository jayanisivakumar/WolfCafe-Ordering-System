/**
 * Test suite for OrdersComponent (UC6)
 * 
 * @author Jayani Sivakumar
 */

import { vi, describe, it, beforeEach, afterEach, expect } from 'vitest'

// Mock React BEFORE importing the component so useEffect behaves like mount-only
vi.mock('react', async () => {
  const actual = await vi.importActual('react')
  return {
    ...actual,
    useEffect: (fn) => actual.useEffect(fn, [])
  }
})

import { render, screen, fireEvent, waitFor, cleanup, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import OrdersComponent from '../OrdersComponent'
import * as OrderService from '../../services/OrderService'

vi.mock('../../services/OrderService')

// storage mock
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
    }
  }
}

Object.defineProperty(window, 'localStorage', {
  value: storageMock(),
  configurable: true
})

Object.defineProperty(window, 'sessionStorage', {
  value: storageMock(),
  configurable: true
})

describe('OrdersComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    sessionStorage.clear()
    localStorage.setItem('role', 'CUSTOMER')
    sessionStorage.setItem('role', 'CUSTOMER')
    sessionStorage.setItem('authenticatedUser', 'customer1')
  })

  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
    vi.clearAllTimers()
    vi.useRealTimers()
  })

  function renderComponent() {
    return render(
      <MemoryRouter>
        <OrdersComponent />
      </MemoryRouter>
    )
  }

  it('ST1: displays pending order without pickup button', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 1, items: [], total: 5, status: 'PENDING' }]
    })

    renderComponent()

    expect(await screen.findByText(/pending/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /pick up order/i })).toBeNull()
  })

  it('ST2: allows pickup for ready orders and removes picked up order', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 2, items: [], total: 5, status: 'READY_FOR_PICKUP' }]
    })

    OrderService.pickupOrder.mockResolvedValue({
      data: { id: 2 }
    })

    renderComponent()

    fireEvent.click(await screen.findByRole('button', { name: /pick up order/i }))
    fireEvent.click(screen.getByRole('button', { name: /confirm/i }))

    await waitFor(() => {
      expect(screen.getByText(/thank you/i)).toBeInTheDocument()
      expect(screen.getByText(/picked up/i)).toBeInTheDocument()
    })

    await waitFor(() => {
      expect(screen.queryByText('#2', { selector: 'td' })).toBeNull()
    })
  })

  it('ST3: displays cancelled order notification and confirm action', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 3, items: [], total: 5, status: 'CANCELLED' }]
    })

    renderComponent()

    expect(await screen.findByText(/order cancelled/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /confirm/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /pick up order/i })).toBeNull()
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('ST3b: confirm dismisses cancelled order from active view', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 7, items: [], total: 5, status: 'CANCELLED' }]
    })

    renderComponent()

    fireEvent.click(await screen.findByRole('button', { name: /confirm/i }))

    await waitFor(() => {
      expect(screen.getByText(/you have no active orders/i)).toBeInTheDocument()
    })
  })

  it('ST4: does not allow pickup for pending orders', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 4, items: [], total: 5, status: 'PENDING' }]
    })

    renderComponent()

    expect(await screen.findByText(/pending/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /pick up order/i })).toBeNull()
  })

  it('ST5: shows picked up orders in history view', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 5, items: [], total: 5, status: 'PICKED_UP' }]
    })

    renderComponent()

    fireEvent.click(await screen.findByRole('button', { name: /order history/i }))

    expect(await screen.findByText('#5')).toBeInTheDocument()
    expect(screen.queryByText(/no picked up orders/i)).toBeNull()
  })

  it('ST6: shows ready notification after refreshing orders', async () => {
    OrderService.getMyOrders
      .mockResolvedValueOnce({
        data: [{ id: 6, items: [], total: 5, status: 'PENDING' }]
      })
      .mockResolvedValueOnce({
        data: [{ id: 6, items: [], total: 5, status: 'READY_FOR_PICKUP' }]
      })

    renderComponent()

    expect(await screen.findByText(/pending/i)).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /refresh orders/i }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Your order #6 is ready for pickup!')
    })

    await waitFor(() => {
      const row = document.querySelector('#order-6')
      expect(row).not.toBeNull()
      expect(within(row).getByText('Ready for Pickup')).toBeInTheDocument()
    })
  })

  it('ST7: shows cancellation notification after refreshing orders', async () => {
    OrderService.getMyOrders
      .mockResolvedValueOnce({
        data: [{ id: 8, items: [], total: 5, status: 'PENDING' }]
      })
      .mockResolvedValueOnce({
        data: [{ id: 8, items: [], total: 5, status: 'CANCELLED' }]
      })

    renderComponent()

    expect(await screen.findByText(/pending/i)).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /refresh orders/i }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Your order #8 was cancelled by staff.')
    })
  })

  it('does not show a cancellation toast for already-cancelled orders on first load', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: [{ id: 9, items: [], total: 5, status: 'CANCELLED' }]
    })

    renderComponent()

    expect(await screen.findByText(/order cancelled/i)).toBeInTheDocument()
    expect(screen.queryByRole('alert')).toBeNull()
  })
})
