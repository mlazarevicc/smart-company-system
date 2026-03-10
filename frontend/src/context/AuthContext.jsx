import React, { createContext, useContext, useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext(null);

const TOKEN_KEY = 'token';

/**
 * Normalizuje role iz JWT-a:
 * backend može slati:
 * - role: "MANAGER"
 * - roles: ["MANAGER", "X"]
 *
 * frontend UVEK koristi STRING
 */
const normalizeRole = (decoded) => {
  if (decoded.role) return decoded.role;
  if (Array.isArray(decoded.roles) && decoded.roles.length > 0) {
    return decoded.roles[0];
  }
  return null;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    const requiresPasswordReset =
      localStorage.getItem('requiresPasswordReset') === 'true';

    if (!token) {
      setLoading(false);
      return;
    }

    try {
      const decoded = jwtDecode(token);
      const currentTime = Date.now() / 1000;

      if (decoded.exp && decoded.exp < currentTime) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem('requiresPasswordReset');
        setUser(null);
      } else {
        setUser({
          email: decoded.sub,
          role: normalizeRole(decoded),
          userId: decoded.user_id || decoded.userId,
          firstName: decoded.firstName || 'User',
          lastName: decoded.lastName || '',
          fullName: `${decoded.firstName || ''} ${decoded.lastName || ''}`.trim(),
          requiresPasswordReset,
        });
      }
    } catch (err) {
      console.error('Invalid token:', err);
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem('requiresPasswordReset');
      setUser(null);
    }

    setLoading(false);
  }, []);

  const login = (loginData) => {
    const { jwt, role, username, userId, requiresPasswordReset } = loginData;

    localStorage.setItem(TOKEN_KEY, jwt);

    if (requiresPasswordReset) {
      localStorage.setItem('requiresPasswordReset', 'true');
    } else {
      localStorage.removeItem('requiresPasswordReset');
    }

    try {
      const decoded = jwtDecode(jwt);

      setUser({
        email: decoded.sub,
        role: role || normalizeRole(decoded),
        userId: userId || decoded.user_id || decoded.userId,
        firstName: decoded.firstName || username || 'User',
        lastName: decoded.lastName || '',
        fullName: `${decoded.firstName || username || ''} ${decoded.lastName || ''}`.trim(),
        requiresPasswordReset: !!requiresPasswordReset,
      });
    } catch (err) {
      console.error('Error decoding token during login:', err);
    }
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem('requiresPasswordReset');
    setUser(null);
  };

  const clearPasswordResetFlag = () => {
    localStorage.removeItem('requiresPasswordReset');
    setUser((prev) =>
      prev ? { ...prev, requiresPasswordReset: false } : prev
    );
  };

  /**
   * ================= ROLE HELPERS =================
   */

  const hasRole = (roles) => {
    if (!user || !localStorage.getItem('role')) return false;
    if (Array.isArray(roles)) {
      return roles.includes(localStorage.getItem('role'));
    }
    return localStorage.getItem('role') === roles;
  };

  const isSuperManager = () => hasRole('SUPERMANAGER');
  const isManager = () =>
    hasRole(['MANAGER', 'SUPERMANAGER']);
  const isCustomer = () => hasRole('ROLE_CUSTOMER');

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        clearPasswordResetFlag,
        hasRole,
        isSuperManager,
        isManager,
        isCustomer,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
};