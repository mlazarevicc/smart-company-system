import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute
 * - čeka da se auth state učita
 * - dozvoljava samo ulogovanim korisnicima
 */
export const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh'
      }}>
        Loading...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (user.requiresPasswordReset && location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />;
  }

  return children;
};

/**
 * PublicRoute
 * - dozvoljava samo neulogovane
 */
export const PublicRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (user) {
    if (user.requiresPasswordReset) {
      return <Navigate to="/change-password" replace />;
    }
    return <Navigate to="/home" replace />;
  }

  return children;
};

export const RoleRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh'
      }}>
        Loading...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (user.requiresPasswordReset && location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />;
  }

  if (!allowedRoles.includes(localStorage.getItem('role'))) {
    console.warn(
      'Access denied. User role:',
      localStorage.getItem('role'),
      'Allowed:',
      allowedRoles
    );
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};