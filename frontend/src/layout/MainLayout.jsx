import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';

const MainLayout = () => {
    return (
        <div style={{ minHeight: '100vh', backgroundColor: '#f1f5f9' }}>
            <Navbar />
            
            <main>
                <Outlet />
            </main>
        </div>
    );
};

export default MainLayout;