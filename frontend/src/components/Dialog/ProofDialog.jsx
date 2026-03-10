import React, { useState } from 'react';
import './ProofDialog.css';
import SecurePdf from '../SecurePdf';
import SecureImage from '../SecureImage';

const ProofDialog = ({
  show,
  proceed,
  title = 'Proof of Ownership',
  cancelText = 'Close',
  proofs = []
}) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  if (!show) return null;

  const hasProofs = proofs && proofs.length > 0;

  const goNext = () => {
    setCurrentIndex((prev) => (prev + 1) % proofs.length);
  };

  const goPrev = () => {
    setCurrentIndex((prev) =>
      prev === 0 ? proofs.length - 1 : prev - 1
    );
  };

  const onOverlayClick = () => proceed();
  const onDialogClick = (e) => e.stopPropagation();

  return (
    <div className="dialog-overlay" onClick={onOverlayClick}>
      <div className="dialog proof-dialog" onClick={onDialogClick}>
        
        <div className="dialog-header">
          <h2 className="dialog-title">{title}</h2>
        </div>

        <div className="dialog-body">

          {!hasProofs && (
            <div className="no-proofs">
              No proof documents available.
            </div>
          )}

          {hasProofs && (
            <div className="proof-carousel">

              {proofs.length > 1 && (
                <button className="carousel-btn left" onClick={goPrev}>
                  ◀
                </button>
              )}

              <div className="proof-content">
                {proofs[currentIndex].toLowerCase().endsWith('.pdf') ? (
                    <SecurePdf fileUrl={proofs[currentIndex]} className={"proof-image"}/>
                    ) : (
                    <SecureImage imageUrl={proofs[currentIndex]} altText="Proof" className={"proof-image"} />
                )}

                <div className="carousel-index">
                  {currentIndex + 1} / {proofs.length}
                </div>
              </div>

              {proofs.length > 1 && (
                <button className="carousel-btn right" onClick={goNext}>
                  ▶
                </button>
              )}

            </div>
          )}

        </div>

        <div className="dialog-actions">
          <button
            className="dialog-btn cancel"
            onClick={proceed}
            type="button"
          >
            {cancelText}
          </button>
        </div>

      </div>
    </div>
  );
};

export default ProofDialog;