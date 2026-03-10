import React, { useState, useEffect } from 'react';

const SecurePdf = ({ fileUrl, className, width = '100%', height = '400px' }) => {
  const [src, setSrc] = useState(null);

  useEffect(() => {
    if (!fileUrl) return;

    const fetchPDF = async () => {
      const token = localStorage.getItem('token');

      let cleanUrl = fileUrl
        .replace('http://localhost:8080', '')
        .replace('https://localhost:8080', '');

      if (!cleanUrl.startsWith('/')) cleanUrl = '/' + cleanUrl;

      try {
        const response = await fetch(cleanUrl, {
          headers: { Authorization: `Bearer ${token}` }
        });

        if (response.ok) {
          const blob = await response.blob();
          setSrc(URL.createObjectURL(blob));
        } else {
          console.error('PDF not found:', cleanUrl, response.status);
        }
      } catch (err) {
        console.error('Error fetching PDF:', err);
      }
    };

    fetchPDF();

    return () => {
      if (src) URL.revokeObjectURL(src);
    };
  }, [fileUrl]);

  if (!src) return <div className={className} style={{ background: '#eee', height, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>⏳</div>;

  return (
    <iframe
      src={src}
      title="Secure PDF"
      width={width}
      height={height}
      className={className}
      style={{ border: '1px solid #ddd', borderRadius: '6px' }}
    />
  );
};

export default SecurePdf;