import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage/LandingPage';
import LoginPage from './pages/LoginPage/LoginPage';
import RegisterPage from './pages/RegisterPage/RegisterPage';
import HomePage from './pages/HomePage/HomePage';
import MainLayout from './layout/MainLayout';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, PublicRoute, RoleRoute } from './utils/RouteGuards';
import ManagerListPage from './pages/ManagerListPage/ManagerListPage';
import RegisterManagerPage from './pages/RegisterManagerPage/RegisterManagerPage';
import ChangePasswordPage from './pages/ChangePasswordPage/ChangePasswordPage';
import UnauthorizedPage from './pages/UnauthorizedPage/UnauthorizedPage';
import ProductDetailPage from './pages/ProductDetailPage/ProductDetailPage';
import ProductListPage from './pages/ProductListPage/ProductListPage';
import CreateProductPage from './pages/CreateProductPage/CreateProductPage';
import EditProductPage from './pages/EditProductPage/EditProductPage';
import WarehouseListPage from './pages/WarehouseListPage/WarehouseListPage';
import CreateWarehousePage from './pages/CreateWarehousePage/CreateWarehousePage';
import WarehouseDetailPage from './pages/WarehouseDetailPage/WarehouseDetailPage';
import SectorDetailPage from './pages/SectorDetailPage/SectorDetailPage';
import EditWarehousePage from './pages/EditWarehousePage/EditWarehousePage';
import FactoryListPage from './pages/FactoryListPage/FactoryListPage';
import CreateFactoryPage from './pages/CreateFactoryPage/CreateFactoryPage';
import FactoryDetailPage from './pages/FactoryDetailPage/FatoryDetailPage';
import EditFactoryPage from './pages/EditFactoryPage/EditFactoryPage';
import { CartProvider } from './context/CartContext';
import ShopPage from './pages/ShopPage/ShopPage';
import CheckoutPage from './pages/CheckoutPage/CheckoutPage';
import VehicleListPage from './pages/VehicleListPage/VehicleListPage';
import CreateVehiclePage from './pages/CreateVehiclePage/CreateVehiclePage';
import EditVehiclePage from './pages/EditVehiclePage/EditVehiclePage';
import VehicleDetailPage from './pages/VehicleDetailPage/VehicleDetailPage';
import CompanyListPage from './pages/CompanyListPage/CompanyListPage';
import CreateCompanyPage from './pages/CreateCompanyPage/CreateCompanyPage';
import CompanyApprovalPage from './pages/CompanyApprovalPage/CompanyApprovalPage';

const Placeholder = ({ title }) => <h1 style={{ padding: 40 }}>{title} Page (WIP)</h1>;

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={
            <PublicRoute>
              <LandingPage />
            </PublicRoute>
          } />

          <Route path="/login" element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          } />

          <Route path="/register" element={
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          } />

          {/* Change Password - Special route */}
          <Route path="/change-password" element={
            <ProtectedRoute>
              <ChangePasswordPage />
            </ProtectedRoute>
          } />

          {/* Unauthorized Page */}
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Protected Routes */}
          <Route element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }>
            <Route path="/home" element={<HomePage />} />
            
            {/* Manager & SUPERMANAGER Routes */}
            <Route path="/factories" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <FactoryListPage />
              </RoleRoute>
            } />

            <Route path="/factories/create" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <CreateFactoryPage />
              </RoleRoute>
            } />
            
            <Route path="/factories/:id" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <FactoryDetailPage />
              </RoleRoute>
            } />
            
            <Route path="/factories/:id/edit" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <EditFactoryPage />
              </RoleRoute>
            } />
            
            <Route path="/warehouses" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                  <WarehouseListPage />
              </RoleRoute>
            } />

            <Route path="/warehouses/create" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <CreateWarehousePage />
              </RoleRoute>
            } />

            <Route path="/warehouses/:id" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <WarehouseDetailPage />
              </RoleRoute>
            } />

            <Route path="/warehouses/:id/edit" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <EditWarehousePage />
              </RoleRoute>
            } />

            <Route path="/warehouses/:warehouseId/sectors/:id" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <SectorDetailPage />
              </RoleRoute>
            } />
            
            <Route path="/vehicles" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <VehicleListPage />
              </RoleRoute>
            } />

            <Route path="/vehicles/create" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <CreateVehiclePage />
              </RoleRoute>
            } />

            <Route path="/vehicles/:id" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <VehicleDetailPage />
              </RoleRoute>
            } />
            
            <Route path="/vehicles/:id/edit" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <EditVehiclePage />
              </RoleRoute>
            } />

            <Route path="/pending-companies" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <CompanyApprovalPage />
              </RoleRoute>
            } />

            {/* CUSTOMER Routes */}
            <Route path="/shop" element={
              <RoleRoute allowedRoles={['ROLE_CUSTOMER']}>
                <ShopPage />
              </RoleRoute>
            } />

            <Route path="/checkout" element={
              <RoleRoute allowedRoles={['ROLE_CUSTOMER']}>
                <CheckoutPage />
              </RoleRoute>
            } />

            <Route path="/companies" element={
              <RoleRoute allowedRoles={['ROLE_CUSTOMER']}>
                <CompanyListPage />
              </RoleRoute>
            } />

            <Route path="/companies/create" element={
              <RoleRoute allowedRoles={['ROLE_CUSTOMER']}>
                <CreateCompanyPage />
              </RoleRoute>
            } />


            {/* SUPERMANAGER Only Routes */}
            <Route path="/managers" element={
            <RoleRoute allowedRoles={['SUPERMANAGER']}>
              <ManagerListPage />
            </RoleRoute>
            } />

          <Route path="/managers/register" element={
            <RoleRoute allowedRoles={['SUPERMANAGER']}> 
              <RegisterManagerPage />
            </RoleRoute>
            } />

            <Route path="/products" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <ProductListPage />
              </RoleRoute>
            } />
            
            <Route path="/products/create" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <CreateProductPage />
              </RoleRoute>
            } />
            
            <Route path="/products/:id" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <ProductDetailPage />
              </RoleRoute>
            } />
            
            <Route path="/products/:id/edit" element={
              <RoleRoute allowedRoles={['MANAGER', 'SUPERMANAGER']}>
                <EditProductPage />
              </RoleRoute>
            } />

          </Route>

          {/* 404 */}
          <Route path="*" element={
            <div style={{ padding: 40, textAlign: 'center' }}>
              <h1>404 - Page Not Found</h1>
              <a href="/home">Go to Home</a>
            </div>
          } />
        </Routes>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
