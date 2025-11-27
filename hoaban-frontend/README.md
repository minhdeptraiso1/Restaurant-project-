# Hoa Ban Restaurant Frontend

Modern Vue 3 + TypeScript frontend for restaurant management system.

## Features

- **Modern UI Components**:

  - Product cards with images
  - Modal for dish details
  - Cart drawer for quick checkout
  - Responsive design with Tailwind CSS

- **User Roles**:

  - **Customer**: Browse menu, add to cart, checkout, make reservations
  - **Staff**: Manage orders, view reservations
  - **Admin**: Full CRUD for dishes, categories, areas, tables, users

- **API Integration**: Complete integration with backend APIs
- **State Management**: Pinia stores for auth, cart, and UI state

## Setup

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

## Environment Variables

Create `.env` file:

```
VITE_API_BASE_URL=http://localhost:8080/api
```

## API Endpoints Integrated

- Authentication (login, register, logout, profile)
- Dishes (list, get details)
- Cart management (add, remove, clear)
- Orders (create, pay, list)
- Reservations (create, list)
- Admin APIs (users, dishes, categories, areas, tables)

## Modern UI Features Added

1. **DishCard Component**: Modern product cards with images and actions
2. **DishModal Component**: Popup for dish details
3. **CartDrawer Component**: Side panel for cart management
4. **Responsive Header**: Modern navigation with cart counter
5. **Admin Dashboards**: Full CRUD interfaces for all entities

## User Flow

1. **Login** → Dashboard based on role
2. **Customer**: Browse menu → Add to cart → Checkout
3. **Staff**: Manage orders and reservations
4. **Admin**: Manage all restaurant data

The frontend is now fully functional with modern UI and complete API integration!
