import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerManager } from '../../services/managerService';
import '../Auth.css';

const RegisterManagerPage = () => {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    
    if (selectedFile) {
      if (!selectedFile.type.startsWith('image/')) {
        setError('Please upload an image file');
        return;
      }
      
      if (selectedFile.size > 5 * 1024 * 1024) {
        setError('File size should not exceed 5MB');
        return;
      }
      
      setFile(selectedFile);
      setPreviewUrl(URL.createObjectURL(selectedFile));
      setError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      setLoading(false);
      return;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters long.');
      setLoading(false);
      return;
    }

    try {
      const data = new FormData();
      data.append('firstName', formData.firstName);
      data.append('lastName', formData.lastName);
      data.append('username', formData.username);
      data.append('email', formData.email);
      data.append('password', formData.password);
      if (file != null) {
        data.append('profileImage', file);
      }
      
      await registerManager(data);
      navigate('/managers?registered=true');
    } catch (err) {
      console.error('Manager registration error:', err);
      setError(
        err.response?.data?.message || 
        'Error happened while registering manager. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container" style={{ height: '100vh', overflow: 'hidden' }}>
      {/* Scrollable content area */}

      <div 
        className="auth-content" 
        style={{ 
          flex: 1, 
          height: '100vh',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'flex-start',
          paddingTop: '40px',
          paddingBottom: '40px'
        }}
      >
        <div className="auth-wrapper" style={{ 
          maxWidth: '500px',
          width: '90%',
          margin: '0 auto'
        }}>
          <button
    type="button"
    className="btn-back auth-back-btn"
    onClick={() => navigate('/managers')}
  >
    ← Back to managers
  </button>
          <h1 className="auth-title">Register New Manager</h1>
          <p className="auth-subtitle">
            Create a new manager account for SmartChain platform
          </p>

          {error && (
            <div style={{
              padding: '12px 16px',
              backgroundColor: '#fee2e2',
              color: '#991b1b',
              borderRadius: '8px',
              marginBottom: '1.5rem',
              fontSize: '0.9rem',
              border: '1px solid #fecaca'
            }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            {/* Profile Image Upload */}
            <div className="image-upload-container">
              <div className="image-preview" onClick={() => document.getElementById('profileImageInput').click()}>
                {previewUrl ? (
                  <img src={previewUrl} alt="Profile preview" />
                ) : (
                  <span className="upload-icon">+</span>
                )}
              </div>
              <label className="upload-label" htmlFor="profileImageInput">
                {previewUrl ? 'Change Image' : 'Upload Profile Image'}
              </label>
              <input
                type="file"
                id="profileImageInput"
                className="hidden-input"
                accept="image/*"
                onChange={handleFileChange}
              />
            </div>

            {/* First Name */}
            <div className="form-group">
              <label className="form-label" htmlFor="firstName">First Name</label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                className="form-input"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="Enter first name"
                required
                minLength={2}
                maxLength={50}
              />
            </div>

            {/* Last Name */}
            <div className="form-group">
              <label className="form-label" htmlFor="lastName">Last Name</label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                className="form-input"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Enter last name"
                required
                minLength={2}
                maxLength={50}
              />
            </div>

            {/* Username */}
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username</label>
              <input
                type="text"
                id="username"
                name="username"
                className="form-input"
                value={formData.username}
                onChange={handleChange}
                placeholder="Choose a username"
                required
                minLength={4}
                maxLength={30}
              />
            </div>

            {/* Email */}
            <div className="form-group">
              <label className="form-label" htmlFor="email">Email Address</label>
              <input
                type="email"
                id="email"
                name="email"
                className="form-input"
                value={formData.email}
                onChange={handleChange}
                placeholder="manager@example.com"
                required
              />
            </div>

            {/* Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="password">Password</label>
              <input
                type="password"
                id="password"
                name="password"
                className="form-input"
                value={formData.password}
                onChange={handleChange}
                placeholder="Enter password (min. 8 characters)"
                required
                minLength={8}
                maxLength={50}
              />
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                className="form-input"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Re-enter password"
                required
              />
            </div>

            {/* Submit Button */}
            <button 
              type="submit" 
              className="auth-button"
              style={{
                padding: '14px 20px',
                backgroundColor: '#00A8E8',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                fontSize: '1rem',
                fontWeight: '600',
                cursor: loading ? 'not-allowed' : 'pointer',
                transition: 'all 0.2s ease',
                opacity: loading ? 0.6 : 1
              }}
              disabled={loading}
              onMouseOver={(e) => !loading && (e.target.style.backgroundColor = '#0077B6')}
              onMouseOut={(e) => !loading && (e.target.style.backgroundColor = '#00A8E8')}
            >
              {loading ? 'Registering...' : 'Register Manager'}
            </button>

            {/* Cancel Button */}
            <button
              type="button"
              style={{
                width: '100%',
                marginTop: '12px',
                padding: '14px 20px',
                backgroundColor: 'transparent',
                color: '#526071',
                border: '1px solid #e2e8f0',
                borderRadius: '8px',
                fontSize: '1rem',
                fontWeight: '500',
                cursor: 'pointer',
                transition: 'all 0.2s ease'
              }}
              onClick={() => navigate('/managers')}
              disabled={loading}
              onMouseOver={(e) => {
                e.target.style.backgroundColor = '#f8fafc';
                e.target.style.borderColor = '#cbd5e1';
              }}
              onMouseOut={(e) => {
                e.target.style.backgroundColor = 'transparent';
                e.target.style.borderColor = '#e2e8f0';
              }}
            >
              Cancel
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RegisterManagerPage;
