import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
    const navigate = useNavigate();
    const { user, logout, isSuperManager, isManager, isCustomer } = useAuth();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <div className="navbar-container">
                <div className="navbar-brand" onClick={() => navigate('/home')}>
                    SmartChain
                </div>

                <div className="navbar-links">
                    {isManager() && (
                        <NavLink to="/factories" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Factories
                        </NavLink>)}
                    {isManager() && (
                        <NavLink to="/warehouses" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Warehouses
                        </NavLink>)}
                    {isManager() && (
                        <NavLink to="/vehicles" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Vehicles
                        </NavLink>)}
                    {isManager() && (
                        <NavLink to="/products" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Products
                        </NavLink>
                    )}
                    {isManager() && (
                        <NavLink to="/pending-companies" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Pending Companies
                        </NavLink>
                    )}
                    {isCustomer() && (
                        <NavLink to="/companies" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Companies
                        </NavLink>
                    )}
                    {isCustomer() && (
                        <NavLink to="/shop" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Shop
                        </NavLink>
                    )}
                    {isCustomer() && (
                        <NavLink to="/checkout" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                            Checkout
                        </NavLink>
                    )}
                    {isSuperManager() && (
                    <NavLink to="/managers" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
                        Managers
                    </NavLink>
                    )}
                </div>

                <div className="navbar-actions">
                    <span className="user-greeting">
                        Hello, {user?.fullName || "User"}
                    </span>
                    <button onClick={handleLogout} className="btn-logout">
                        Logout
                    </button>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;