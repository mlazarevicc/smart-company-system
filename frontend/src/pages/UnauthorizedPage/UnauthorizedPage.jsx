import React from 'react';
import { useNavigate } from 'react-router-dom';

const UnauthorizedPage = () => {
  const navigate = useNavigate();

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
      padding: '20px'
    }}>
      <div style={{
        textAlign: 'center',
        maxWidth: '500px',
        background: 'white',
        padding: '60px 40px',
        borderRadius: '16px',
        boxShadow: '0 8px 24px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ fontSize: '5rem', marginBottom: '20px' }}>🚫</div>
        <h1 style={{ color: '#0A2540', fontSize: '2rem', marginBottom: '16px' }}>
          Access Denied
        </h1>
        <p style={{ color: '#526071', fontSize: '1.1rem', marginBottom: '32px' }}>
          You don't have permission to access this page.
        </p>
        <div style={{ display: 'flex', gap: '16px', justifyContent: 'center' }}>
          <button
            onClick={() => navigate(-1)}
            style={{
              padding: '12px 24px',
              backgroundColor: 'transparent',
              color: '#526071',
              border: '1px solid #e2e8f0',
              borderRadius: '8px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            Go Back
          </button>
          <button
            onClick={() => navigate('/home')}
            style={{
              padding: '12px 24px',
              backgroundColor: '#00A8E8',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            Go to Home
          </button>
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;
