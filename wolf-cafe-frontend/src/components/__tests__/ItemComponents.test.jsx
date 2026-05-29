import { render, screen, waitFor } from '@testing-library/react';
import { within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, test, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import ListItemsComponent from '../ListItemsComponent';
import ItemComponent from '../ItemComponent';
import { deleteItemById, getAllItems, getItemById, saveItem, updateItem } from '../../services/ItemService';
import { getInventory } from '../../services/InventoryService';
import { placeOrder } from '../../services/OrderService';
import { getTaxRate } from '../../services/TaxRateService';

const navigateMock = vi.fn();
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => {
      store[key] = value.toString();
    },
    removeItem: (key) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

vi.mock('../../services/ItemService', () => ({
  getAllItems: vi.fn(),
  getItemById: vi.fn(),
  saveItem: vi.fn(),
  updateItem: vi.fn(),
  deleteItemById: vi.fn(),
}));

vi.mock('../../services/TaxRateService', () => ({
  getTaxRate: vi.fn(),
}));

vi.mock('../../services/OrderService', () => ({
  placeOrder: vi.fn(),
}));

vi.mock('../../services/InventoryService', () => ({
  getInventory: vi.fn(),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => ({}),
  };
});

const renderWithRouter = (ui) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  );
};

describe('ListItemsComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.sessionStorage.clear();
    window.localStorage.clear();
    vi.stubGlobal('confirm', vi.fn(() => true));
    getInventory.mockResolvedValue({
      data: {
        ingredients: {
          ESPRESSO: 10,
          MILK: 10,
        },
      },
    });
    getTaxRate.mockResolvedValue({
      data: { rate: 2 },
    });
    getAllItems.mockResolvedValue({
      data: [
        {
          id: 1,
          name: 'Latte',
          description: 'Espresso with milk',
          price: 4.5,
          ingredients: [
            { name: 'ESPRESSO', amount: 2 },
            { name: 'STEAMED MILK', amount: 1 },
          ],
        },
      ],
    });
  });

  test('shows staff item actions and loads items', async () => {
    sessionStorage.setItem('role', 'ROLE_STAFF');

    renderWithRouter(<ListItemsComponent />);

    expect(await screen.findByText('Latte')).toBeInTheDocument();
    expect(screen.getByText('Espresso (2), Steamed Milk (1)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add item/i })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /add latte to cart/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /open cart/i })).not.toBeInTheDocument();
  });

  test('hides management actions for non-staff users', async () => {
    sessionStorage.setItem('role', 'ROLE_CUSTOMER');

    renderWithRouter(<ListItemsComponent />);

    expect(await screen.findByText('Latte')).toBeInTheDocument();
    expect(screen.getByText('Espresso (2), Steamed Milk (1)')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /add item/i })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add latte to cart/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /open cart/i })).toBeInTheDocument();
  });

  test('adds an item to the cart and shows it in the cart modal', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_CUSTOMER');
    sessionStorage.setItem('authenticatedUser', 'customer1');

    renderWithRouter(<ListItemsComponent />);

    await screen.findByText('Latte');
    await user.click(screen.getByRole('button', { name: /add latte to cart/i }));
    await user.click(screen.getByRole('button', { name: /open cart/i }));

    expect(await screen.findByText(/my cart/i)).toBeInTheDocument();
    const cartTable = screen.getAllByRole('table')[1];
    expect(within(cartTable).getAllByText('Latte').length).toBeGreaterThan(0);
    expect(within(cartTable).getAllByText('$4.50').length).toBeGreaterThan(0);
  });

  test('persists logged-in customer cart across remounts', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_CUSTOMER');
    sessionStorage.setItem('authenticatedUser', 'customer1');

    const firstRender = renderWithRouter(<ListItemsComponent />);

    await screen.findByText('Latte');
    await user.click(screen.getByRole('button', { name: /add latte to cart/i }));
    firstRender.unmount();

    renderWithRouter(<ListItemsComponent />);

    await user.click(screen.getByRole('button', { name: /open cart/i }));
    expect(await screen.findByText(/my cart/i)).toBeInTheDocument();
    const cartTable = screen.getAllByRole('table')[1];
    expect(within(cartTable).getAllByText('Latte').length).toBeGreaterThan(0);
  });

  test('does not persist anonymous cart across remounts', async () => {
    const user = userEvent.setup();

    const firstRender = renderWithRouter(<ListItemsComponent />);

    await screen.findByText('Latte');
    await user.click(screen.getByRole('button', { name: /add latte to cart/i }));
    firstRender.unmount();

    renderWithRouter(<ListItemsComponent />);

    await user.click(screen.getByRole('button', { name: /open cart/i }));
    expect(await screen.findByText(/my cart/i)).toBeInTheDocument();
    expect(screen.getByText(/your cart is empty/i)).toBeInTheDocument();
  });

  test('staff update action navigates to the update page', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_STAFF');

    renderWithRouter(<ListItemsComponent />);

    await user.click(await screen.findByRole('button', { name: /update/i }));

    expect(navigateMock).toHaveBeenCalledWith('/update-item/1');
  });

  test('staff delete action removes the item and refreshes the list', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_STAFF');
    deleteItemById.mockResolvedValue({});
    getAllItems
      .mockResolvedValueOnce({
        data: [
          {
            id: 1,
            name: 'Latte',
            description: 'Espresso with milk',
            price: 4.5,
            ingredients: [
              { name: 'ESPRESSO', amount: 2 },
              { name: 'STEAMED MILK', amount: 1 },
            ],
          },
        ],
      })
      .mockResolvedValueOnce({ data: [] });

    renderWithRouter(<ListItemsComponent />);

    await user.click(await screen.findByRole('button', { name: /delete/i }));

    await waitFor(() => {
      expect(deleteItemById).toHaveBeenCalledWith(1);
    });
    expect(await screen.findByText(/no items found/i)).toBeInTheDocument();
  });

  test('customer places an order from the cart with the selected tip', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_CUSTOMER');
    sessionStorage.setItem('authenticatedUser', 'customer1');
    placeOrder.mockResolvedValue({
      data: { id: 12 },
    });

    renderWithRouter(<ListItemsComponent />);

    await screen.findByText('Latte');
    await user.click(screen.getByRole('button', { name: /add latte to cart/i }));
    await user.click(screen.getByRole('button', { name: /open cart/i }));
    await user.click(screen.getByRole('button', { name: /check out/i }));
    await user.click(screen.getByRole('button', { name: /15%/i }));
    await user.click(screen.getByRole('button', { name: /next/i }));
    await user.click(screen.getByRole('button', { name: /place order/i }));

    await waitFor(() => {
      expect(placeOrder).toHaveBeenCalledWith({
        items: [
          {
            itemId: 1,
            quantity: 1,
          },
        ],
        tipType: 'PERCENTAGE',
        tipValue: 15,
      });
    });
    expect(navigateMock).toHaveBeenCalledWith('/orders');
  });

  test('shows backend error when placing an order fails', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_CUSTOMER');
    sessionStorage.setItem('authenticatedUser', 'customer1');
    placeOrder.mockRejectedValue({
      response: {
        data: {
          error: 'Order must contain at least one item',
        },
      },
    });

    renderWithRouter(<ListItemsComponent />);

    await screen.findByText('Latte');
    await user.click(screen.getByRole('button', { name: /add latte to cart/i }));
    await user.click(screen.getByRole('button', { name: /open cart/i }));
    await user.click(screen.getByRole('button', { name: /check out/i }));
    await user.click(screen.getByRole('button', { name: /skip/i }));
    await user.click(screen.getByRole('button', { name: /next/i }));
    await user.click(screen.getByRole('button', { name: /place order/i }));

    expect(await screen.findByText(/order must contain at least one item/i)).toBeInTheDocument();
  });
});

describe('ItemComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.sessionStorage.clear();
    window.localStorage.clear();
    getInventory.mockResolvedValue({
      data: {
        ingredients: {
          MILK: 10,
          ESPRESSO: 20,
        },
      },
    });
  });

  test('shows validation errors for missing required fields', async () => {
    const user = userEvent.setup();
    renderWithRouter(<ItemComponent />);

    await user.click(screen.getByRole('button', { name: /add item/i }));

    expect(screen.getByText(/item name is required/i)).toBeInTheDocument();
    expect(screen.getByText(/item description is required/i)).toBeInTheDocument();
    expect(screen.getByText(/item price is required/i)).toBeInTheDocument();
    expect(saveItem).not.toHaveBeenCalled();
  });

  test('submits a new item successfully', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_STAFF');
    saveItem.mockResolvedValue({ data: { id: 3 } });

    renderWithRouter(<ItemComponent />);

    expect(await screen.findByRole('option', { name: 'Espresso' })).toBeInTheDocument();
    await user.type(screen.getByPlaceholderText(/enter item name/i), 'Mocha');
    await user.type(screen.getByPlaceholderText(/enter item description/i), 'Chocolate espresso drink');
    await user.type(screen.getByPlaceholderText(/enter item price/i), '5.25');
    await user.selectOptions(screen.getByRole('combobox'), 'ESPRESSO');
    await user.type(screen.getByPlaceholderText(/amount/i), '2');
    await user.click(screen.getByRole('button', { name: /add item/i }));

    await waitFor(() => {
      expect(saveItem).toHaveBeenCalledWith({
        name: 'Mocha',
        description: 'Chocolate espresso drink',
        price: 5.25,
        ingredients: [
          {
            name: 'ESPRESSO',
            amount: 2,
          },
        ],
      });
    });

    expect(navigateMock).toHaveBeenCalledWith('/items');
  });

  test('shows ingredient validation when a staff user enters a negative amount', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('role', 'ROLE_STAFF');

    renderWithRouter(<ItemComponent />);

    await user.type(screen.getByPlaceholderText(/enter item name/i), 'Mocha');
    await user.type(screen.getByPlaceholderText(/enter item description/i), 'Chocolate espresso drink');
    await user.type(screen.getByPlaceholderText(/enter item price/i), '5.25');
    await user.type(screen.getByPlaceholderText(/amount/i), '-1');
    await user.click(screen.getByRole('button', { name: /add item/i }));

    expect(await screen.findByText(/ingredient amounts must be positive/i)).toBeInTheDocument();
    expect(saveItem).not.toHaveBeenCalled();
  });

  test('shows backend error when item creation fails', async () => {
    const user = userEvent.setup();
    saveItem.mockRejectedValue({
      response: {
        data: 'Item name already exists',
      },
    });

    renderWithRouter(<ItemComponent />);

    await user.type(screen.getByPlaceholderText(/enter item name/i), 'Latte');
    await user.type(screen.getByPlaceholderText(/enter item description/i), 'Duplicate item');
    await user.type(screen.getByPlaceholderText(/enter item price/i), '4');
    await user.click(screen.getByRole('button', { name: /add item/i }));

    expect(await screen.findByText(/item name already exists/i)).toBeInTheDocument();
  });
});
