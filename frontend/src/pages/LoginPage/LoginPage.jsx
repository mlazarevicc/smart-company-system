import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { loginUser } from '../../services/authService';
import '../Auth.css';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { useAuth } from '../../context/AuthContext';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const location = useLocation();
    const {login} = useAuth();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        
        if (params.get('verified') === 'true') {
            toast.success("Account activated successfully! You can now log in.", {
                position: "top-center",
                autoClose: 5000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                theme: "colored",
            });
            navigate('/login', { replace: true });
        }
        else if (params.get('registered') === 'true') {
            toast.success("Successful registration! Check email for activation link.", {
                position: "top-center",
                autoClose: 5000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                theme: "colored",
            });
            navigate('/login', { replace: true });
        }
    }, [location, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const data = await loginUser(email, password);
            login(data);
            if (data.requiresPasswordReset) {
                navigate('/change-password');
            } else {
                navigate('/home');
            }
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="auth-container">
            <ToastContainer />
            <div 
                className="auth-visual"
                style={{ backgroundImage: "url('https://images.unsplash.com/photo-1497366216548-37526070297c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80')" }}
            ></div>

            <div className="auth-content">
                <div className="auth-wrapper">
                    <h1 className="auth-title">Welcome back</h1>
                    <p className="auth-subtitle">Please enter your details to sign in.</p>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">Email Address</label>
                            <input 
                                type="email" 
                                className="form-input" 
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Password</label>
                            <input 
                                type="password" 
                                className="form-input" 
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        {error && <div style={{color: 'red', marginBottom: '10px'}}>{error}</div>}

                        <button type="submit" className="btn btn-primary auth-button">
                            Sign In
                        </button>
                    </form>

                    <div className="auth-footer">
                        Don't have an account? 
                        <Link to="/register" className="auth-link">Create account</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;