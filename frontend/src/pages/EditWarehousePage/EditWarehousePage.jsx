import React, { useState, useEffect, version } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { getWarehouseById, updateWarehouseWithImages } from '../../services/warehouseService';
import { mapWarehouseFromAPI } from '../../api/warehouse.mapper';
import locationService from '../../services/locationService';
import geocodingService from '../../services/geocodingService';
import SecureImage from '../../components/SecureImage';

import './EditWarehousePage.css';

const EditWarehousePage = () => {
  const navigate = useNavigate();
  const { id } = useParams();

  const [loading, setLoading] = useState(false);
  const [loadingWarehouse, setLoadingWarehouse] = useState(true);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    name: '',
    address: '',
    countryId: '',
    cityId: '',
    latitude: null,
    longitude: null,
  });

  const [warehouse, setWarehouse] = useState(null);
  const [imageUrls, setImageUrls] = useState([]);
  const [newImageFiles, setNewImageFiles] = useState([]);
  const [newImagePreviews, setNewImagePreviews] = useState([]);

  // Location search
  const [locationQuery, setLocationQuery] = useState('');
  const [locationResults, setLocationResults] = useState([]);
  const [locationOpen, setLocationOpen] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);

  // Map
  const [mapPosition, setMapPosition] = useState(null);
  const [mapSelected, setMapSelected] = useState(false);
  const [mapCenter, setMapCenter] = useState([44.8, 20.46]);

  useEffect(() => {
    loadWarehouse();
  }, [id]);

  const MapCenterSetter = ({ center }) => {
    const map = useMap();
    map.setView(center, 13);
    return null;
  };

  const loadWarehouse = async () => {
    setLoadingWarehouse(true);
    setError('');
    try {
      const response = await getWarehouseById(id);
      const mappedWarehouse = mapWarehouseFromAPI(response);
      setWarehouse(mappedWarehouse);
      setImageUrls(mappedWarehouse.imageUrls || []);

      const lat = mappedWarehouse.latitude;
      const lon = mappedWarehouse.longitude;

      const cityName = mappedWarehouse.city || '';
      const countryName = mappedWarehouse.country || '';

      setFormData({
        name: mappedWarehouse.name || '',
        address: mappedWarehouse.address || '',
        cityId: '',
        countryId: '',
        latitude: lat ?? null,
        longitude: lon ?? null,
        version: mappedWarehouse.version || 0,
      });

      if (lat && lon) {
        setMapCenter([lat, lon]);
        setMapPosition({ lat, lng: lon });
        setMapSelected(true);
      }

      if (cityName && countryName) {
        setLocationQuery(`${cityName}, ${countryName}`);

        try {
          const res = await locationService.searchCityCountry(cityName, 10);
          const results = res.data || [];

          const match = results.find(
            (r) =>
              r.cityName?.toLowerCase() === cityName.toLowerCase() &&
              r.countryName?.toLowerCase() === countryName.toLowerCase()
          );

          if (match) {
            setFormData((prev) => ({
              ...prev,
              cityId: match.cityId,
              countryId: match.countryId,
            }));
          } else {
            console.warn('Could not resolve cityId/countryId for:', cityName, countryName);
          }
        } catch (err) {
          console.error('Failed to resolve city/country IDs:', err);
        }
      }
    } catch (err) {
      console.error('Failed to load warehouse:', err);
      setError('Failed to load warehouse details.');
    } finally {
      setLoadingWarehouse(false);
    }
  };


  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
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
    setFormData((prev) => ({ ...prev, latitude: null, longitude: null }));
  };

  const LocationMarker = () => {
    useMapEvents({
      click: async (e) => {
        setMapPosition(e.latlng);
        setMapSelected(true);

        const lat = e.latlng.lat;
        const lon = e.latlng.lng;

        setFormData((prev) => ({ ...prev, latitude: lat, longitude: lon }));

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

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    if (files.length + newImageFiles.length > 5) {
      setError('Maximum 5 images total allowed.');
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
        setNewImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    }

    setNewImageFiles((prev) => [...prev, ...newImages]);
    setError('');
  };


  const handleRemoveNewImage = (index) => {
    setNewImageFiles((prev) => prev.filter((_, i) => i !== index));
    setNewImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const validateForm = () => {
    if (!formData.name.trim()) {
      setError('Warehouse name is required.');
      return false;
    }
    if (!formData.cityId || !formData.countryId) {
      setError('Please select a valid location (city, country).');
      return false;
    }
    if (imageUrls.length === 0 && newImageFiles.length === 0) {
      setError('At least one image is required.');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) return;

    setLoading(true);
    try {
      let finalLat = formData.latitude;
      let finalLon = formData.longitude;

      if (!mapSelected || finalLat == null || finalLon == null) {
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
          version: formData.version,
        };

        await updateWarehouseWithImages(id, warehouseData, newImageFiles);

        toast.success('Warehouse updated successfully!');
        setTimeout(() => navigate(`/warehouses/${id}`), 1500);

      } catch (err) {
        const errorMsg = err.response?.data?.message || 'Failed to update warehouse.';
        console.log(err)
        setError(errorMsg);
        toast.error(errorMsg);
      } finally {
        setLoading(false);
      }
    };

  if (loadingWarehouse) {
    return (
      <div className="edit-warehouse-container">
        <div className="loading">Loading warehouse details...</div>
      </div>
    );
  }

  if (error && !warehouse) {
    return (
      <div className="edit-warehouse-container">
        <div className="error-message">{error}</div>
        <button className="btn-back" onClick={() => navigate('/warehouses')}>
          Back to Warehouses
        </button>
      </div>
    );
  }

  return (
    <div className="edit-warehouse-container">
      <div className="edit-warehouse-header">
        <h1>Edit Warehouse</h1>
        <button className="btn-back" type="button" onClick={() => navigate(`/warehouses/${id}`)}>
          Back
        </button>
      </div>

      <div className="edit-warehouse-card">
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          {/* Name */}
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
            />
          </div>

          {/* Location search */}
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

          {/* Address */}
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
            />
          </div>

          {/* Mapa */}
          <div className="form-group map-wrapper">
            <label className="form-label">
              Location on map <span className="required">*</span>
            </label>
            <div style={{ height: '300px', width: '100%' }}>
              <MapContainer center={mapCenter} zoom={13} style={{ height: '100%', width: '100%' }}>
                <MapCenterSetter center={mapCenter} />
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution="&copy; OpenStreetMap contributors"
                />
                <LocationMarker />
              </MapContainer>
            </div>
            <small className="form-hint">
              Click on the map to update the exact location.
            </small>
          </div>

          {/* New images upload */}
          <div className="form-group">
            <label className="form-label">
              {imageUrls.length > 0 ? 'Add New Images' : 'Warehouse Images'}{' '}
              {imageUrls.length === 0 && <span className="required">*</span>}
            </label>

            {newImageFiles.length + imageUrls.length < 5 && (
              <div className="image-upload-area">
                <input
                  type="file"
                  id="new-images"
                  accept="image/*"
                  multiple
                  onChange={handleImageChange}
                  className="file-input"
                />
                <label htmlFor="new-images" className="file-label">
                  <p className="upload-text">Click to upload images</p>
                  <p className="upload-hint">PNG, JPG up to 5MB (max 5 total)</p>
                </label>
              </div>
            )}

            {newImagePreviews.length > 0 && (
              <div className="image-preview-grid" style={{ marginTop: '1rem' }}>
                {newImagePreviews.map((preview, index) => (
                  <div key={index} className="image-preview-item">
                    <img src={preview} alt={`New ${index + 1}`} className="image-preview" />
                    <button
                      type="button"
                      onClick={() => handleRemoveNewImage(index)}
                      className="btn-remove-image"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate(`/warehouses/${id}`)}
              className="btn-cancel"
              disabled={loading}
            >
              Cancel
            </button>
            <button type="submit" className="btn-submit" disabled={loading}>
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>

      <ToastContainer />
    </div>
  );
};

export default EditWarehousePage;
