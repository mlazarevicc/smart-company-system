import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import factoryService from '../../services/factoryService';
import locationService from '../../services/locationService';
import geocodingService from '../../services/geocodingService';
import {
  MapContainer,
  TileLayer,
  Marker,
  useMapEvents,
  useMap,
} from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import './FactoryForm.css';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const EditFactoryPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    address: '',
    cityId: null,
    countryId: null,
    latitude: null,
    longitude: null,
    productIds: [],
    version: null
  });

  const [loadingFactory, setLoadingFactory] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [newImages, setNewImages] = useState([]);
  const [newImagePreviews, setNewImagePreviews] = useState([]);
  const [replaceImagesLoading, setReplaceImagesLoading] = useState(false);

  const [locationQuery, setLocationQuery] = useState('');
  const [locationResults, setLocationResults] = useState([]);
  const [locationOpen, setLocationOpen] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);

  const [mapPosition, setMapPosition] = useState(null);
  const [mapSelected, setMapSelected] = useState(false);
  const [mapCenter, setMapCenter] = useState([44.8, 20.46]);

  useEffect(() => {
    loadFactory();
  }, [id]);

  const loadFactory = async () => {
    setLoadingFactory(true);
    setError('');
    try {
      const response = await factoryService.getFactoryById(id);
      const f = response.data;

      setFormData({
        name: f.name || '',
        address: f.address || '',
        cityId: null, // док не добијеш ID‑еве из DTO‑а
        countryId: null,
        latitude: f.latitude ?? null,
        longitude: f.longitude ?? null,
        productIds: (f.products || []).map((p) => p.id),
        version: f.version
      });

      // текстуални приказ локације из DTO‑а (city, country)
      if (f.city && f.country) {
        setLocationQuery(`${f.city}, ${f.country}`);
      }

      // иницијални center/pin ако имаш координате
      if (f.latitude != null && f.longitude != null) {
        const center = [f.latitude, f.longitude];
        setMapCenter(center);
        setMapPosition({ lat: f.latitude, lng: f.longitude });
      }
    } catch (err) {
      console.error('Failed to load factory:', err);
      setError('Failed to load factory for editing.');
    } finally {
      setLoadingFactory(false);
    }
  };

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

  const handleProductsChange = (e) => {
    const selected = Array.from(e.target.selectedOptions).map((o) =>
      Number(o.value)
    );
    setFormData((prev) => ({
      ...prev,
      productIds: selected,
    }));
  };

  const handleNewImagesChange = (e) => {
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
        setNewImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });

    setNewImages((prev) => [...prev, ...validFiles]);
    setError('');
  };

  const handleRemoveNewImage = (index) => {
    setNewImages((prev) => prev.filter((_, i) => i !== index));
    setNewImagePreviews((prev) => prev.filter((_, i) => i !== index));
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

    if (item.cityLatitude && item.cityLongitude) {
      setMapCenter([item.cityLatitude, item.cityLongitude]);
    }
    setMapSelected(false);
    setMapPosition(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
        let finalLat = null;
        let finalLon = null;

        // --- Logika za koordinate (zadržana tvoja postojeća) ---
        if (mapSelected && formData.latitude && formData.longitude) {
            finalLat = formData.latitude;
            finalLon = formData.longitude;
        } else {
            if (!formData.address.trim()) {
                setError('Please enter street address or click on the map.');
                setSaving(false);
                return;
            }

            if (formData.cityId) {
                const geoRes = await geocodingService.geocodeFactoryAddress(
                    formData.address,
                    formData.cityId
                );
                finalLat = geoRes.data.latitude;
                finalLon = geoRes.data.longitude;
            } else {
                finalLat = formData.latitude;
                finalLon = formData.longitude;
            }
        }

        if (finalLat === null || finalLon === null) {
            setError('Location is missing. Please select a point on the map.');
            setSaving(false);
            return;
        }

        // --- Priprema podataka ---
        const factoryData = {
            name: formData.name.trim(),
            address: formData.address.trim(),
            cityId: formData.cityId,
            countryId: formData.countryId,
            latitude: finalLat,
            longitude: finalLon,
            productIds: formData.productIds || [],
            version: formData.version
        };

        // --- JEDAN poziv za sve (Podaci + Slike) ---
        // Slanjem newImages (koji je niz File objekata), servis će sve spakovati u FormData
        await factoryService.updateFactory(id, factoryData, newImages);
        toast.success('Factory updated successfully!')
        setTimeout(() => navigate(`/factories`), 1500);
    } catch (err) {
        console.error('Failed to update factory:', err);
        
        // Specifičan handling za ConcurrentModificationException
        if (err.response?.status === 409 || err.response?.data?.includes('version')) {
            setError('This factory was recently updated by someone else. Please refresh and try again.');
        } else {
            setError(
                err.response?.data?.message ||
                'Failed to update factory. Check your connection or data and try again.'
            );
        }
    } finally {
        setSaving(false);
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

            if (!prev.cityId && data.cityId) {
              updated.cityId = data.cityId;
            }
            if (!prev.countryId && data.countryId) {
              updated.countryId = data.countryId;
            }

            return updated;
          });

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

  if (loadingFactory) {
    return (
      <div className="factory-form-container">
        <div className="loading">Loading factory...</div>
      </div>
    );
  }

  return (
    <div className="factory-form-container">
      <div className="factory-form-header">
        <h1>Edit Factory</h1>
        <button
          type="button"
          className="btn-back"
          onClick={() => navigate('/factories')}
        >
          Back
        </button>
      </div>

      <div className="factory-form-card">
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Name *</label>
            <input
              type="text"
              name="name"
              className="form-input"
              value={formData.name}
              onChange={handleChange}
              placeholder="Factory name"
            />
          </div>

          {/* Location (City, Country) searchable dropdown */}
          <div className="form-group location-group">
            <label className="form-label">Location (City, Country)</label>
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
              Change city/country here or let the map infer it from pin.
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
              Click on the map to update factory location.
            </small>
          </div>

          {/* New images upload (replace existing) */}
          <div className="form-group">
            <label className="form-label">Replace images</label>

            <div className="image-upload-area secondary">
              <input
                type="file"
                id="factory-new-images-input"
                multiple
                accept="image/*"
                className="file-input"
                onChange={handleNewImagesChange}
              />
              <label
                htmlFor="factory-new-images-input"
                className="file-label small"
              >
                <span className="upload-text">Click to select new images</span>
                <span className="upload-hint">
                  Existing images will be replaced when you save.
                </span>
              </label>
            </div>

            {newImagePreviews.length > 0 && (
              <div className="multi-image-preview-container">
                <div className="image-preview-grid">
                  {newImagePreviews.map((src, index) => (
                    <div className="image-preview-wrapper" key={index}>
                      <img
                        src={src}
                        alt={`New preview ${index + 1}`}
                        className="image-preview"
                      />
                      <button
                        type="button"
                        className="btn-remove-image"
                        onClick={() => handleRemoveNewImage(index)}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={() => navigate(`/factories/${id}`)}
              disabled={saving || replaceImagesLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={saving || replaceImagesLoading}
            >
              {saving ? 'Saving...' : 'Save changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditFactoryPage;
