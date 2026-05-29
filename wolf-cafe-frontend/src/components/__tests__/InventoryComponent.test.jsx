import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, test, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import InventoryComponent from '../InventoryComponent';
import { getInventory, updateInventory } from '../../services/InventoryService';
import { createIngredient } from '../../services/IngredientService';

const navigateMock = vi.fn();

vi.mock('../../services/InventoryService', () => ({
  getInventory: vi.fn(),
  updateInventory: vi.fn(),
}));

vi.mock('../../services/IngredientService', () => ({
  createIngredient: vi.fn(),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

const renderWithRouter = (ui) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  );
};

describe('InventoryComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    getInventory.mockResolvedValue({
      data: {
        ingredients: {
          COFFEE: 10,
          'VANILLA SYRUP': 5,
        },
      },
    });
  });

  test('loads and displays inventory from the backend', async () => {
    renderWithRouter(<InventoryComponent />);

    expect(await screen.findByText('Coffee')).toBeInTheDocument();
    expect(screen.getByText('Vanilla Syrup')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  test('shows validation error for invalid inventory amount', async () => {
    const user = userEvent.setup();
    renderWithRouter(<InventoryComponent />);

    const inputs = await screen.findAllByPlaceholderText(/enter value/i);
    await user.type(inputs[0], '-1');
    await user.click(screen.getByRole('button', { name: /update inventory/i }));

    expect(screen.getByText(/coffee amount must be a positive integer/i)).toBeInTheDocument();
    expect(updateInventory).not.toHaveBeenCalled();
  });

  test('submits inventory updates successfully', async () => {
    const user = userEvent.setup();
    updateInventory.mockResolvedValue({ data: {} });

    renderWithRouter(<InventoryComponent />);

    const inputs = await screen.findAllByPlaceholderText(/enter value/i);
    await user.type(inputs[0], '4');
    await user.click(screen.getByRole('button', { name: /update inventory/i }));

    await waitFor(() => {
      expect(updateInventory).toHaveBeenCalledWith({
        ingredients: {
          COFFEE: 4,
        },
      });
    });

    expect(await screen.findByText(/inventory updated successfully/i)).toBeInTheDocument();
  });

  test('adds a new ingredient through the modal', async () => {
    const user = userEvent.setup();
    createIngredient.mockResolvedValue({
      data: { name: 'Sugar', amount: 8 },
    });

    renderWithRouter(<InventoryComponent />);

    await screen.findByText('Coffee');
    await user.click(screen.getByRole('button', { name: /add ingredient/i }));

    const nameInput = screen.getByPlaceholderText(/enter name/i);
    const amountInput = screen.getAllByPlaceholderText(/enter value/i).at(-1);

    await user.type(nameInput, 'Sugar');
    await user.type(amountInput, '8');
    await user.click(screen.getAllByRole('button', { name: /add ingredient/i })[1]);

    await waitFor(() => {
      expect(createIngredient).toHaveBeenCalledWith({
        name: 'Sugar',
        amount: 8,
      });
    });

    expect(await screen.findByText(/ingredient added successfully/i)).toBeInTheDocument();
  });
});
