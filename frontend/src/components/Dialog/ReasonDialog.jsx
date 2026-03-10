import React, { useState } from 'react';
import { confirmable, createConfirmation } from 'react-confirm';
import './ReasonDialog.css';

const ReasonDialog = ({
  show,
  proceed,
  title = 'Reason',
  message = 'Put a reason in.',
  okText = 'Submit',
  cancelText = 'Cancel',
}) => {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');  // Track error for empty reason

  if (!show) return null;

  // When clicking on the overlay (cancel), return false and empty string
  const onOverlayClick = () => proceed({ ok: false, reason: '' });
  const onDialogClick = (e) => e.stopPropagation();

  // Handle the submit, check if reason is provided
  const handleSubmit = () => {
    if (reason.trim() === '') {
      setError('Reason is required');  // Set error if the reason is empty
    } else {
      setError('');  // Clear error if the reason is provided
      proceed({ ok: true, reason });  // Pass the reason and ok as true
    }
  };

  return (
    <div className="dialog-overlay" onClick={onOverlayClick} role="presentation">
      <div className="dialog" onClick={onDialogClick} role="dialog" aria-modal="true">
        <div className="dialog-title">{title}</div>
        <div className="dialog-message">{message}</div>

        <input
          type="text"
          className="dialog-input"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          placeholder="Enter your reason"
        />

        {error && <div className="error-message">{error}</div>}  {/* Display error message */}

        <div className="dialog-actions">
          <button className="dialog-btn cancel" onClick={() => proceed({ ok: false, reason: '' })} type="button">
            {cancelText}
          </button>
          <button
            className="dialog-btn confirm"
            onClick={handleSubmit}
            type="button"
            disabled={reason.trim() === ''}  // Disable submit if reason is empty
          >
            {okText}
          </button>
        </div>
      </div>
    </div>
  );
};

// Wrap the ReasonDialog with createConfirmation for custom confirmation behavior
export const reasonConfirm = createConfirmation(confirmable(ReasonDialog));