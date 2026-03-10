import React from 'react';
import { Link } from 'react-router-dom';
import './LandingPage.css';

const LandingPage = () => {
    return (
        <div className="landing-container">
            
            <div className="landing-visual">
                <div className="visual-content">
                </div>
            </div>

            <div className="landing-content">
                <div className="content-wrapper">
                    <h1 className="landing-title">
                        The Future of <br />
                        Connected Logistics.
                    </h1>
                    <p className="landing-subtitle">
                        Manage production, optimize warehousing, and streamline wholesale distribution with our advanced B2B platform.
                    </p>

                    <div className="button-group">
                        <Link to="/login">
                            <button className="btn btn-primary">Sign In Now</button>
                        </Link>
                        <Link to="/register">
                            <button className="btn btn-secondary">Create Account</button>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LandingPage;