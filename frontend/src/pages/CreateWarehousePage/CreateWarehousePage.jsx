import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

import { createWarehouse } from '../../services/warehouseService';
import locationService from '../../services/locationService';
import geocodingService from '../../services/geocodingService';

import './CreateWarehousePage.css';

const CreateWarehousePage = () => {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    name: '',
    address: '',
    countryId: null,
    cityId: null,
    latitude: null,
    longitude: null,
  });

  const [sectors, setSectors] = useState([]);
  const [newSector, setNewSector] = useState({ name: '', description: '' });

  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);

  const [locationQuery, setLocationQuery] = useState('');
  const [locationResults, setLocationResults] = useState([]);
  const [locationOpen, setLocationOpen] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);

  const [mapPosition, setMapPosition] = useState(null);
  const [mapSelected, setMapSelected] = useState(false);
  const [mapCenter, setMapCenter] = useState([44.8, 20.46]);

  const MapCenterSetter = ({ center }) => {
    const map = useMap();
    map.setView(center, 12);
    return null;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleAddSector = () => {
    if (!newSector.name.trim()) {
      setError('Sector name is required.');
      return;
    }
    setSectors((prev) => [...prev, { ...newSector, id: Date.now() }]);
    setNewSector({ name: '', description: '' });
    setError('');
  };

  const handleRemoveSector = (id) => {
    setSectors((prev) => prev.filter((sector) => sector.id !== id));
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    if (files.length + imageFiles.length > 5) {
      setError('Maximum 5 images allowed.');
      return;
    }

    const newImages = [];

    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        setError('Please select valid image files.');
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        setError('Image size must be less than 5MB.');
        return;
      }
      newImages.push(file);

      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    }

    setImageFiles((prev) => [...prev, ...newImages]);
    setError('');
  };

  const handleRemoveImage = (index) => {
    setImageFiles((prev) => prev.filter((_, i) => i !== index));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
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
      setLocationResults(res.data || []);
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

    if (item.cityLatitude && item.cityLongitude) {
      setMapCenter([item.cityLatitude, item.cityLongitude]);
    }

    setMapSelected(false);
    setMapPosition(null);
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

            if (data?.streetAddress) updated.address = data.streetAddress;
            if (!prev.cityId && data?.cityId) updated.cityId = data.cityId;
            if (!prev.countryId && data?.countryId) updated.countryId = data.countryId;

            return updated;
          });

          if (data?.cityName && data?.countryName) {
            setLocationQuery(`${data.cityName}, ${data.countryName}`);
          }
        } catch (err) {
          console.error('Failed to reverse geocode:', err);
        }
      },
    });

    return mapPosition ? <Marker position={mapPosition} /> : null;
  };

  const validateFormBase = () => {
    if (!formData.name.trim()) {
      setError('Warehouse name is required.');
      return false;
    }
    if (!formData.cityId || !formData.countryId) {
      setError('Please select a valid location (city, country).');
      return false;
    }
    if (sectors.length === 0) {
      setError('At least one sector is required.');
      return false;
    }
    if (imageFiles.length === 0) {
      setError('At least one image is required.');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateFormBase()) return;

    setLoading(true);
    try {
      let finalLat = null;
      let finalLon = null;

      if (mapSelected && formData.latitude != null && formData.longitude != null) {
        finalLat = formData.latitude;
        finalLon = formData.longitude;
      } else {
        if (!formData.address.trim()) {
          setError('Please enter street address or click on the map to select location.');
          setLoading(false);
          return;
        }
        const geoRes = await geocodingService.geocodeFactoryAddress(
          formData.address,
          formData.cityId
        );
        finalLat = geoRes.data.latitude;
        finalLon = geoRes.data.longitude;
      }

      if (!formData.address.trim()) {
        setError('Address is required.');
        setLoading(false);
        return;
      }

      const warehouseData = {
        name: formData.name.trim(),
        address: formData.address.trim(),
        cityId: parseInt(formData.cityId, 10),
        countryId: parseInt(formData.countryId, 10),
        latitude: Number(finalLat),
        longitude: Number(finalLon),
        sectors: sectors.map(({ id, ...sector }) => sector),
      };

      await createWarehouse(warehouseData, imageFiles);  // ✅ plain objekat + fajlovi

      toast.success('Warehouse created successfully!');
      setTimeout(() => navigate('/warehouses'), 1500);
    } catch (err) {
      console.error('Failed to create warehouse:', err);
      const errorMsg =
        err.response?.data?.message ||
        'Failed to create warehouse. Please check address or map location and try again.';
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="create-warehouse-container">
      <div className="create-warehouse-header">
        <h1>Create New Warehouse</h1>
        <button className="btn-back" type="button" onClick={() => navigate('/warehouses')}>
          Back to Warehouses
        </button>
      </div>

      <div className="create-warehouse-card">
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name" className="form-label">
              Warehouse Name <span className="required">*</span>
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              className="form-input"
              placeholder="e.g., Central Warehouse"
              required
            />
          </div>

          <div className="form-group location-group">
            <label className="form-label">
              Location (City, Country) <span className="required">*</span>
            </label>

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
              {locationLoading && <span className="location-spinner">...</span>}
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
                    <span className="location-sub">{item.countryCode}</span>
                  </button>
                ))}
              </div>
            )}

            {locationOpen && !locationLoading && locationResults.length === 0 && (
              <div className="location-dropdown empty">
                <span>No locations found.</span>
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="address" className="form-label">
              Street address <span className="required">*</span>
            </label>
            <input
              type="text"
              id="address"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              className="form-input"
              placeholder="e.g. Bulevar Oslobođenja 1"
              required
            />
          </div>

          <div className="form-group map-wrapper">
            <label className="form-label">
              Location on map <span className="required">*</span>
            </label>

            <div style={{ height: '300px', width: '100%' }}>
              <MapContainer center={mapCenter} zoom={12} style={{ height: '100%', width: '100%' }}>
                <MapCenterSetter center={mapCenter} />
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution="&copy; OpenStreetMap contributors"
                />
                <LocationMarker />
              </MapContainer>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              Sectors <span className="required">*</span>
            </label>

            <div className="sector-input-group">
              <input
                type="text"
                value={newSector.name}
                onChange={(e) => setNewSector((prev) => ({ ...prev, name: e.target.value }))}
                className="form-input"
                placeholder="Sector name (e.g., Deep Freeze Zone)"
              />
              <textarea
                value={newSector.description}
                onChange={(e) =>
                  setNewSector((prev) => ({ ...prev, description: e.target.value }))
                }
                className="form-textarea-inline"
                placeholder="Sector description (optional)"
                rows={2}
              />
              <button type="button" onClick={handleAddSector} className="btn-add-sector">
                Add Sector
              </button>
            </div>

            {sectors.length > 0 && (
              <div className="sectors-list">
                <h4>Added Sectors ({sectors.length})</h4>
                {sectors.map((sector) => (
                  <div key={sector.id} className="sector-item">
                    <div className="sector-info">
                      <div className="sector-name">{sector.name}</div>
                      {sector.description && (
                        <div className="sector-description">{sector.description}</div>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveSector(sector.id)}
                      className="btn-remove-sector"
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">
              Warehouse Images <span className="required">*</span>
            </label>

            {imagePreviews.length < 5 && (
              <div className="image-upload-area">
                <input
                  type="file"
                  id="images"
                  accept="image/*"
                  multiple
                  onChange={handleImageChange}
                  className="file-input"
                />
                <label htmlFor="images" className="file-label">
                  <p className="upload-text">Click to upload images</p>
                  <p className="upload-hint">PNG, JPG up to 5MB (max 5 images)</p>
                </label>
              </div>
            )}

            {imagePreviews.length > 0 && (
              <div className="image-preview-grid">
                {imagePreviews.map((preview, index) => (
                  <div key={index} className="image-preview-item">
                    <img src={preview} alt={`Preview ${index + 1}`} className="image-preview" />
                    <button
                      type="button"
                      onClick={() => handleRemoveImage(index)}
                      className="btn-remove-image"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/warehouses')}
              className="btn-cancel"
              disabled={loading}
            >
              Cancel
            </button>
            <button type="submit" className="btn-submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create Warehouse'}
            </button>
          </div>
        </form>
      </div>

      <ToastContainer />
    </div>
  );
};

export default CreateWarehousePage;
