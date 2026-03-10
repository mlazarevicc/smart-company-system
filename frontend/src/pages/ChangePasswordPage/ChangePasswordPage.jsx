import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios'
import '../Auth.css';
import { ToastContainer, toast } from 'react-toastify';

const ChangePasswordPage = () => {
  const navigate = useNavigate();
  const { logout } = useAuth(); 
  
  const [formData, setFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // Validation
    if (formData.newPassword !== formData.confirmPassword) {
      setError('New passwords do not match.');
      setLoading(false);
      return;
    }

    if (formData.newPassword.length < 8) {
      setError('Password must be at least 8 characters long.');
      setLoading(false);
      return;
    }

    if (formData.oldPassword === formData.newPassword) {
      setError('New password must be different from old password.');
      setLoading(false);
      return;
    }

    try {
      await api.post('/managers/change-password', {
        oldPassword: formData.oldPassword,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword
      });

      // Show success message
      toast.success("Password changed successfully! Please log in with your new password.", {
        position: "top-center",
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        theme: "colored",
    });
      
      // Use logout from AuthContext (clears everything and navigates)
      logout();
      navigate('/login', { replace: true });
      
    } catch (err) {
      console.error('Password change error:', err);
      setError(
        err.response?.data?.message || 
        'Failed to change password. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container" style={{ height: '100vh', overflow: 'hidden' }}>
      <ToastContainer />
      <div 
        className="auth-content" 
        style={{ 
          flex: 1, 
          height: '100vh',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center'
        }}
      >
        <div className="auth-wrapper" style={{ maxWidth: '450px' }}>
          <h1 className="auth-title">Change Password</h1>
          <p className="auth-subtitle">
            For security reasons, you must change your password before continuing.
          </p>

          <div style={{
            padding: '12px 16px',
            backgroundColor: '#fff3cd',
            color: '#856404',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            fontSize: '0.9rem',
            border: '1px solid #ffeaa7'
          }}>
            ⚠️ <strong>Important:</strong> You will be logged out after changing your password.
          </div>

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
            {/* Old Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="oldPassword">
                Current Password
              </label>
              <input
                type="password"
                id="oldPassword"
                name="oldPassword"
                className="form-input"
                value={formData.oldPassword}
                onChange={handleChange}
                placeholder="Enter your current password"
                required
              />
            </div>

            {/* New Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="newPassword">
                New Password
              </label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                className="form-input"
                value={formData.newPassword}
                onChange={handleChange}
                placeholder="Enter new password (min. 8 characters)"
                required
                minLength={8}
              />
            </div>

            {/* Confirm New Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="confirmPassword">
                Confirm New Password
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                className="form-input"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Re-enter new password"
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
                opacity: loading ? 0.6 : 1,
                marginTop: '1rem',
                width: '100%'
              }}
              disabled={loading}
            >
              {loading ? 'Changing Password...' : 'Change Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ChangePasswordPage;
