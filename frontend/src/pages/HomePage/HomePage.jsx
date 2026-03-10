import React from 'react';
import './HomePage.css';

const HomePage = () => {
    return (
        <div className="home-container">
            <section className="hero-section">
                <h1>Welcome to SmartChain</h1>
                <p>Your integrated solution for supply chain management and logistics orchestration.</p>
                <div className="hero-actions">
                    <button className="primary-btn">Get Started</button>
                    <button className="secondary-btn">Learn More</button>
                </div>
            </section>

            <section className="content-overview">
                <div className="info-card">
                    <h2>Streamline Operations</h2>
                    <p>Manage your entire supply chain from a single, intuitive interface designed for efficiency.</p>
                </div>
                <div className="info-card">
                    <h2>Real-time Connectivity</h2>
                    <p>Connect with your partners, track shipments, and monitor inventory levels in real-time.</p>
                </div>
            </section>
        </div>
    );
};

export default HomePage;