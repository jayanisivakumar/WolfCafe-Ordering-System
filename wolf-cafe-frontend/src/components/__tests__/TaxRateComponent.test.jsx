import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaxRateComponent from '../TaxRateComponent';
import { getTaxRate, updateTaxRate } from '../../services/TaxRateService';
import { describe, test, expect, beforeEach, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';

// Mock localStorage for Vitest
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
    }
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

// Mock API
vi.mock('../../services/TaxRateService', () => ({
  getTaxRate: vi.fn(),
  updateTaxRate: vi.fn(),
}));

// Helper to wrap component with Router
const renderWithRouter = (ui) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  );
};

describe('TaxRateComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  test('renders unauthorized for non-admin users', () => {
    localStorage.setItem('role', 'STAFF');

    renderWithRouter(<TaxRateComponent />);

    expect(screen.getByText(/unauthorized/i)).toBeInTheDocument();
  });

  test('renders system settings page for admin', async () => {
    localStorage.setItem('role', 'ADMIN');
    getTaxRate.mockResolvedValue({ data: { rate: 2.0 } });

    renderWithRouter(<TaxRateComponent />);

    expect(screen.getByText(/system settings/i)).toBeInTheDocument();

    // handles both 2% and 2.0%
    expect(await screen.findByText(/2(\.0)?%/i)).toBeInTheDocument();
  });

  test('opens change tax rate modal', async () => {
    localStorage.setItem('role', 'ADMIN');
    getTaxRate.mockResolvedValue({ data: { rate: 2.0 } });

    renderWithRouter(<TaxRateComponent />);

    const button = screen.getByText(/change tax rate/i);
    await userEvent.click(button);

    expect(screen.getByText(/enter new tax rate/i)).toBeInTheDocument();
  });

  test('shows error for invalid tax rate input', async () => {
    localStorage.setItem('role', 'ADMIN');
    getTaxRate.mockResolvedValue({ data: { rate: 2.0 } });

    renderWithRouter(<TaxRateComponent />);

    await userEvent.click(screen.getByText(/change tax rate/i));

    const input = screen.getByRole('spinbutton');
    await userEvent.clear(input);
    await userEvent.type(input, '-5');

    await userEvent.click(screen.getByText(/save/i));

    expect(screen.getByText(/positive number/i)).toBeInTheDocument();
  });

  test('updates tax rate successfully', async () => {
    localStorage.setItem('role', 'ADMIN');
    getTaxRate.mockResolvedValue({ data: { rate: 2.0 } });
    updateTaxRate.mockResolvedValue({ data: { rate: 5.0 } });

    renderWithRouter(<TaxRateComponent />);

    await userEvent.click(screen.getByText(/change tax rate/i));

    const input = screen.getByRole('spinbutton');
    await userEvent.clear(input);
    await userEvent.type(input, '5');

    await userEvent.click(screen.getByText(/save/i));

    expect(await screen.findByText(/updated successfully/i)).toBeInTheDocument();
    expect(updateTaxRate).toHaveBeenCalledWith(5);
  });
});