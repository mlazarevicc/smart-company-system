import React from 'react';
import { confirmable, createConfirmation } from 'react-confirm';
import './ConfirmDialog.css';

const ConfirmDialog = ({
  show,
  proceed,
  title = 'Confirm',
  message = 'Are you sure?',
  okText = 'Yes',
  cancelText = 'No',
}) => {
  if (!show) return null;

  const onOverlayClick = () => proceed(false);
  const onDialogClick = (e) => e.stopPropagation();

  return (
    <div className="dialog-overlay" onClick={onOverlayClick} role="presentation">
      <div className="dialog" onClick={onDialogClick} role="dialog" aria-modal="true">
        <div className="dialog-title">{title}</div>
        <div className="dialog-message">{message}</div>

        <div className="dialog-actions">
          <button className="dialog-btn cancel" onClick={() => proceed(false)} type="button">
            {cancelText}
          </button>
          <button className="dialog-btn confirm" onClick={() => proceed(true)} type="button">
            {okText}
          </button>
        </div>
      </div>
    </div>
  );
};

export const confirm = createConfirmation(confirmable(ConfirmDialog));
