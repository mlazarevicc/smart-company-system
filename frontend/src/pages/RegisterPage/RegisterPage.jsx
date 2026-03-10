import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../../services/authService';
import '../Auth.css';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: ''
    });

    const [file, setFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        if (selectedFile) {
            setFile(selectedFile);
            setPreviewUrl(URL.createObjectURL(selectedFile));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match.")
            return;
        }
        setLoading(true);

        try {
            const data = new FormData();
            data.append('username', formData.username);
            data.append('email', formData.email);
            data.append('password', formData.password);
            data.append('firstName', formData.firstName);
            data.append('lastName', formData.lastName);

            if (file) {
                data.append('profileImage', file);
            }

            await registerUser(data);

            navigate('/login?registered=true');
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Error happened while registering, please try again.')
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div 
                className="auth-visual" 
                style={{ backgroundImage: "url('https://images.unsplash.com/photo-1553413077-190dd305871c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80')" }}
            ></div>

            <div className="auth-content">
                <div className="auth-wrapper" style={{maxHeight: '100vh', overflowY: 'auto', padding: '20px', scrollbarWidth: 'none'}}>
                    <h1 className="auth-title">Create Account</h1>
                    <p className="auth-subtitle">Join SmartChain today.</p>

                    <form onSubmit={handleSubmit}>
                        
                        <div className="image-upload-container">
                            <label htmlFor="file-input" className="image-preview">
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Profile Preview" />
                                ) : (
                                    <span className="upload-icon">+</span>
                                )}
                            </label>
                            <input 
                                id="file-input"
                                type="file" 
                                accept="image/*"
                                onChange={handleFileChange}
                                className="hidden-input"
                            />
                            <label htmlFor="file-input" className="upload-label">
                                {file ? "Change Photo" : "Upload Profile Photo"}
                            </label>
                        </div>

                        <div style={{display: 'flex', gap: '10px'}}>
                            <div className="form-group" style={{flex: 1}}>
                                <label className="form-label">First Name</label>
                                <input 
                                    type="text" name="firstName" className="form-input" 
                                    value={formData.firstName} onChange={handleChange} required
                                />
                            </div>
                            <div className="form-group" style={{flex: 1}}>
                                <label className="form-label">Last Name</label>
                                <input 
                                    type="text" name="lastName" className="form-input" 
                                    value={formData.lastName} onChange={handleChange} required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Username</label>
                            <input 
                                type="text" name="username" className="form-input" 
                                value={formData.username} onChange={handleChange} required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Email Address</label>
                            <input 
                                type="email" name="email" className="form-input" 
                                value={formData.email} onChange={handleChange} required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Password</label>
                            <input 
                                type="password" name="password" className="form-input" 
                                value={formData.password} onChange={handleChange} required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Confirm Password</label>
                            <input 
                                type="password" name="confirmPassword" className="form-input" 
                                value={formData.confirmPassword} onChange={handleChange} required
                            />
                        </div>

                        {error && <div style={{color: 'red', marginBottom: '10px'}}>{error}</div>}

                        <button type="submit" className="btn btn-primary auth-button" disabled={loading}>
                            {loading ? 'Please wait...' : 'Sign Up'}
                        </button>
                    </form>

                    <div className="auth-footer">
                        Already have an account? 
                        <Link to="/login" className="auth-link">Sign in</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;