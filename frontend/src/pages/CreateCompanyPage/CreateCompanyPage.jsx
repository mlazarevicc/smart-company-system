import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import companyService from '../../services/companyService';
import locationService from '../../services/locationService';
import geocodingService from '../../services/geocodingService';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import './CreateCompanyPage.css';

const CreateCompanyPage = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    address: '',
    cityId: null,
    countryId: null,
    latitude: null,
    longitude: null,
  });

  const [error, setError] = useState('');
  const [loading] = useState(false);

  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);

  const [locationQuery, setLocationQuery] = useState('');
  const [locationResults, setLocationResults] = useState([]);
  const [locationOpen, setLocationOpen] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const [mapPosition, setMapPosition] = useState(null);
  const [mapSelected, setMapSelected] = useState(false);
  const [mapCenter, setMapCenter] = useState([44.8, 20.46]);

  const [ownershipPreviews, setOwnershipPreviews] = useState([]);
  const [ownershipFiles, setOwnershipFiles] = useState([]);
  const [ownershipError, setOwnershipError] = useState('');

  useEffect(() => {
    return () => {
      ownershipPreviews.forEach(p => URL.revokeObjectURL(p.url));
    };
  }, [ownershipPreviews]);

  const MapCenterSetter = ({ center }) => {
    const map = useMap();
    map.setView(center, 12);
    return null;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleImagesChange = (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    const validFiles = [];

    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        setError('All files must be images.');
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        setError('Each image must be less than 5MB.');
        return;
      }
      validFiles.push(file);
    }

    validFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });

    setImageFiles((prev) => [...prev, ...validFiles]);
    setError('');
  };

  const handleRemoveImage = (index) => {
    setImageFiles((prev) => prev.filter((_, i) => i !== index));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleOwnershipChange = (e) => {
  const files = Array.from(e.target.files || []);
  if (!files.length) return;

  const validFiles = [];
  const previews = [];

  for (const file of files) {
    const isImage = file.type.startsWith('image/');
    const isPdf = file.type === 'application/pdf';

    if (!isImage && !isPdf) {
      setOwnershipError('File must be an image or PDF.');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setOwnershipError('Each file must be less than 5MB.');
      return;
    }

    validFiles.push(file);
    previews.push({
      type: isPdf ? 'pdf' : 'image',
      url: URL.createObjectURL(file),
      name: file.name
    });
  }

  setOwnershipFiles(validFiles);
  setOwnershipPreviews(previews);
  setOwnershipError('');
};

  const handleLocationInputChange = async (e) => {
    const value = e.target.value;
    setLocationQuery(value);

    if (!value.trim()) {
      setLocationResults([]);
      setLocationOpen(false);
      return;
    }

    setLocationLoading(true);
    try {
      const res = await locationService.searchCityCountry(value, 10);
      setLocationResults(res.data);
      setLocationOpen(true);
    } catch (err) {
      console.error('Failed to search locations:', err);
    } finally {
      setLocationLoading(false);
    }
  };

  const handleLocationSelect = (item) => {
    setFormData((prev) => ({
      ...prev,
      cityId: item.cityId,
      countryId: item.countryId,
    }));
    setLocationQuery(`${item.cityName}, ${item.countryName}`);
    setLocationOpen(false);
  
    // ako backend šalje lat/lon za grad
    if (item.cityLatitude && item.cityLongitude) {
      setMapCenter([item.cityLatitude, item.cityLongitude]);
    }
  
    setMapSelected(false);
    setMapPosition(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
  
    if (!formData.name.trim()) {
      setFormError('Name is required.');
      return;
    }
    if (!formData.cityId || !formData.countryId) {
      setFormError('Please select a valid location (city, country).');
      return;
    }

    if (!ownershipFiles.length) {
      setFormError('Proof of ownership is required.');
      setSubmitting(false);
      return;
    }
  
    setSubmitting(true);
  
    try {
      let finalLat = null;
      let finalLon = null;
  
      if (mapSelected && formData.latitude && formData.longitude) {
        finalLat = formData.latitude;
        finalLon = formData.longitude;
      } else {
        // nije kliknuto na mapu → moramo imati adresu
        if (!formData.address.trim()) {
          setFormError(
            'Please enter street address or click on the map to select location.'
          );
          setSubmitting(false);
          return;
        }
        const geoRes = await geocodingService.geocodeCompanyAddress(
          formData.address,
          formData.cityId
        );
        const geo = geoRes.data;
        finalLat = geo.latitude;
        finalLon = geo.longitude;
      }
  
      const payload = {
        name: formData.name.trim(),
        address: formData.address.trim(),
        cityId: formData.cityId,
        countryId: formData.countryId,
        latitude: finalLat,
        longitude: finalLon,
        productIds: [],
      };
  
      await companyService.createCompany(payload, imageFiles, ownershipFiles);
      navigate('/companies');
    } catch (err) {
      console.error('Failed to create company:', err);
      const msg =
        err.response?.data?.message ||
        'Could not create company. Please check address or map location and try again.';
      setFormError(msg);
    } finally {
      setSubmitting(false);
    }
  };
  

  const LocationMarker = () => {
    useMapEvents({
      click: async (e) => {
        setMapPosition(e.latlng);
        setMapSelected(true);
  
        const lat = e.latlng.lat;
        const lon = e.latlng.lng;
  
        setFormData((prev) => ({
          ...prev,
          latitude: lat,
          longitude: lon,
        }));
  
        try {
          const res = await geocodingService.reverseGeocode(lat, lon);
          const data = res.data;
  
          setFormData((prev) => {
            const updated = { ...prev };
  
            if (data.streetAddress) {
              updated.address = data.streetAddress;
            }
  
            // ako cityId/countryId nisu već setovani, popuni ih iz reverse geocodinga
            if (!prev.cityId && data.cityId) {
              updated.cityId = data.cityId;
            }
            if (!prev.countryId && data.countryId) {
              updated.countryId = data.countryId;
            }
  
            return updated;
          });
  
          // popuni Location input (tekstualni) ako imamo naziv grada/države
          if (data.cityName && data.countryName) {
            setLocationQuery(`${data.cityName}, ${data.countryName}`);
          }
        } catch (err) {
          console.error('Failed to reverse geocode:', err);
        }
      },
    });
  
    return mapPosition ? <Marker position={mapPosition} /> : null;
  };
  
  return (
    <div className="company-form-container">
      <div className="company-form-header">
        <h1>Create Company</h1>
        <button
          type="button"
          className="btn-back"
          onClick={() => navigate('/companies')}
        >
          Back
        </button>
      </div>

      <div className="company-form-card">
        {formError && <div className="error-message">{formError}</div>}
        {error && <div className="error-message">{error}</div>}
        {loading && <div className="loading">Loading...</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Name *</label>
            <input
              type="text"
              name="name"
              className="form-input"
              value={formData.name}
              onChange={handleChange}
              placeholder="Company name"
            />
          </div>

           {/* Image upload – koristi postojeći stil iz CompanyFormPage.css */}
           <div className="form-group">
            <label className="form-label">Images</label>

            <div className="image-upload-area">
              <input
                type="file"
                id="company-images-input"
                multiple
                accept="image/*"
                className="file-input"
                onChange={handleImagesChange}
              />
              <label
                htmlFor="company-images-input"
                className="file-label"
              >
                <span className="upload-icon">📷</span>
                <span className="upload-text">Click to upload images</span>
                <span className="upload-hint">
                  JPEG/PNG, up to 5MB each. Multiple files allowed.
                </span>
              </label>
            </div>

            {imagePreviews.length > 0 && (
              <div className="multi-image-preview-container">
                <div className="image-preview-grid">
                  {imagePreviews.map((src, index) => (
                    <div className="image-preview-wrapper" key={index}>
                      <img
                        src={src}
                        alt={`Preview ${index + 1}`}
                        className="image-preview"
                      />
                      <button
                        type="button"
                        className="btn-remove-image"
                        onClick={() => handleRemoveImage(index)}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Location (City, Country) searchable dropdown */}
          <div className="form-group location-group">
            <label className="form-label">Location (City, Country) *</label>
            <div className="location-input-wrapper">
              <input
                type="text"
                className="form-input location-input"
                placeholder="Start typing city or country..."
                value={locationQuery}
                onChange={handleLocationInputChange}
                onFocus={() => {
                  if (locationResults.length > 0) setLocationOpen(true);
                }}
              />
              {locationLoading && (
                <span className="location-spinner">...</span>
              )}
            </div>

            {locationOpen && locationResults.length > 0 && (
              <div className="location-dropdown">
                {locationResults.map((item) => (
                  <button
                    type="button"
                    key={item.cityId}
                    className="location-option"
                    onClick={() => handleLocationSelect(item)}
                  >
                    <span className="location-main">
                      {item.cityName}, {item.countryName}
                    </span>
                    <span className="location-sub">
                      {item.countryCode}
                    </span>
                  </button>
                ))}
              </div>
            )}

            {locationOpen &&
              !locationLoading &&
              locationResults.length === 0 && (
                <div className="location-dropdown empty">
                  <span>No locations found.</span>
                </div>
              )}

            <small className="form-hint">
              We will save both city and country based on your selection.
            </small>
          </div>

          <div className="form-group">
            <label className="form-label">Street address *</label>
            <input
              type="text"
              name="address"
              className="form-input"
              value={formData.address}
              onChange={handleChange}
              placeholder="e.g. Bulevar Oslobođenja 1"
            />
          </div>

          {/* Map for selecting exact location */}
          <div className="form-group map-wrapper">
            <label className="form-label">Location on map *</label>
            <div style={{ height: '300px', width: '100%' }}>
              <MapContainer
                center={mapCenter}
                zoom={12}
                style={{ height: '100%', width: '100%' }}
              >
                <MapCenterSetter center={mapCenter} />

                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution="&copy; OpenStreetMap contributors"
                />

                <LocationMarker />
              </MapContainer>
            </div>
            <small className="form-hint">
              Click on the map to choose the exact company location.
            </small>
          </div>

          <div className="form-group">
            <label className="form-label">Proof of Ownership *</label>

            <div className="image-upload-area">
              <input
                type="file"
                id="ownership-files-input"
                multiple
                accept="image/*,application/pdf"
                className="file-input"
                onChange={handleOwnershipChange}
              />
              <label
                htmlFor="ownership-files-input"
                className="file-label"
              >
                <span className="upload-icon">📄</span>
                <span className="upload-text">
                  Upload proof of ownership (PDF or image)
                </span>
                <span className="upload-hint">
                  PDF/JPEG/PNG, up to 5MB each.
                </span>
              </label>
            </div>

            {ownershipPreviews.length > 0 && (
              <div className="multi-image-preview-container">
                <div className="image-preview-grid">
                  {ownershipPreviews.map((file, index) => (
                    <div key={index} className="image-preview-wrapper">
                      {file.type === 'image' ? (
                        <img
                          src={file.url}
                          alt={file.name}
                          className="image-preview"
                        />
                      ) : (
                        <iframe
                          src={file.url}
                          title={file.name}
                          width="100%"
                          height="200px"
                          style={{ border: '1px solid #ddd', borderRadius: '6px' }}/>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {ownershipError && (
              <div className="error-message">{ownershipError}</div>
            )}
          </div>


          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={() => navigate('/companies')}
              disabled={submitting || loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={submitting || loading}
            >
              {submitting ? 'Saving...' : 'Create company'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateCompanyPage;
