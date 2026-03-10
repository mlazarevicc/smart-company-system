import React, { useState, useEffect } from 'react';

const SecureImage = ({ imageUrl, altText, className }) => {
  const [src, setSrc] = useState(null);

  useEffect(() => {
    if (!imageUrl) return;

    const fetchImage = async () => {
      const token = localStorage.getItem('token');

      let cleanUrl = imageUrl
        .replace('http://localhost:8080', '')
        .replace('https://localhost:8080', '');

      if (!cleanUrl.startsWith('/')) {
        cleanUrl = '/' + cleanUrl;
      }

      try {
        const response = await fetch(cleanUrl, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const blob = await response.blob();
          setSrc(URL.createObjectURL(blob));
        } else {
          console.error('Slika nije pronađena:', cleanUrl, response.status);
        }
      } catch (err) {
        console.error('Greška pri učitavanju slike:', err);
      }
    };

    fetchImage();

    return () => {
      if (src) URL.revokeObjectURL(src);
    };
  }, [imageUrl]);

  return src
    ? <img src={src} alt={altText} className={className} />
    : <div className={className} style={{ background: '#eee', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>⏳</div>;
};

export default SecureImage;
