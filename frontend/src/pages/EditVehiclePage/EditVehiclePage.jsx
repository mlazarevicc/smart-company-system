import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

import '../../services/vehicleService';
import { mapVehicleFromApi } from '../../api/vehicle.mapper';

import './EditVehiclePage.css';
import vehicleService from '../../services/vehicleService';
import SecureImage from '../../components/SecureImage';

const EditVehiclePage = () => {
  const navigate = useNavigate();
  const { id } = useParams();

  const [makes, setMakes] = useState([]);
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingVehicle, setLoadingVehicle] = useState(true);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    registrationNumber: '',
    weightLimit: 0.0,
    make: 0,
    model: 0,
  });

  const [vehicle, setVehicle] = useState(null);
  const [imageUrls, setImageUrls] = useState([]);
  const [newImageFiles, setNewImageFiles] = useState([]);
  const [newImagePreviews, setNewImagePreviews] = useState([]);

  useEffect(() => {
    loadVehicle();
    loadMakes()
  }, [id]);

  const loadVehicle = async () => {
    setLoadingVehicle(true);
    setError('');
    try {
      const response = await vehicleService.getVehicleById(id);
      const mappedVehicle = mapVehicleFromApi(response.data);
      console.log(mappedVehicle)
      setVehicle(mappedVehicle);
      setImageUrls(mappedVehicle.images || []);

      const registrationNumber = mappedVehicle.registrationNumber;
      const weightLimit = mappedVehicle.weightLimit;

      const makeId = mappedVehicle.makeId || '';
      const modelId = mappedVehicle.modelId || '';
      const version = mappedVehicle.version || 0;

      if (makeId) {
        await loadModels(makeId);
      }

      setFormData({
        registrationNumber: registrationNumber || '',
        weightLimit: weightLimit || 0.0,
        make: makeId || 0,
        model: modelId || 0,
        version: version || 0
      });
    } catch (err) {
      console.error('Failed to load vehicle:', err);
      setError('Failed to load vehicle details.');
    } finally {
      setLoadingVehicle(false);
    }
  };

  const loadMakes = async () => {
    try {
      const response = await vehicleService.getMakes();
      setMakes(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const loadModels = async (makeId) => {
    try {
      const response = await vehicleService.getModelsByMakeId(makeId);
      setModels(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleMakeChange = (e) => {
    if (e) {
        loadModels(e.target.value);
    } else {
        setModels([]);
    }
    handleInputChange(e)
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

  const handleRemoveExistingImage = (index) => {
    setImageUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const handleRemoveNewImage = (index) => {
    setNewImageFiles((prev) => prev.filter((_, i) => i !== index));
    setNewImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const validateForm = () => {
    if (!formData.registrationNumber.trim()) {
      setError('Vehicle name is required.');
      return false;
    }
    if (!formData.make) {
      setError('Make is required.');
      return false;
    }
    if (!formData.model) {
      setError('Model is required.');
      return false;
    }
    if (!formData.weightLimit || Number(formData.weightLimit) <= 0) {
      setError('Weight must be greater than 0.');
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
      await vehicleService.updateVehicle(id, formData, newImageFiles);

      toast.success('Vehicle updated successfully!');
      setTimeout(() => navigate(`/vehicles/${id}`), 1500);
    } catch (err) {
      console.error('Failed to update vehicle:', err);
      const errorMsg =
        err.response?.data?.message || 'Failed to update vehicle. Please try again.';
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (loadingVehicle) {
    return (
      <div className="edit-vehicle-container">
        <div className="loading">Loading vehicle details...</div>
      </div>
    );
  }

  if (error && !vehicle) {
    return (
      <div className="edit-vehicle-container">
        <div className="error-message">{error}</div>
        <button className="btn-back" onClick={() => navigate('/vehicles')}>
          Back to Vehicles
        </button>
      </div>
    );
  }

  return (
    <div className="edit-vehicle-container">
      <div className="edit-vehicle-header">
        <h1>Edit Vehicle</h1>
        <button className="btn-back" type="button" onClick={() => navigate(`/vehicles/${id}`)}>
          Back
        </button>
      </div>

      <div className="edit-vehicle-card">
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            {/* Registration Number */}
            <div className="form-group">
                <label htmlFor="registrationNumber" className="form-label">
                Vehicle Registration Number <span className="required">*</span>
                </label>
                <input
                type="text"
                id="registrationNumber"
                name="registrationNumber"
                value={formData.registrationNumber}
                onChange={handleInputChange}
                className="form-input"
                placeholder="e.g., SU-607-EF"
                />
            </div>

            <div className="form-group">
              <label htmlFor="weightLimit" className="form-label">
                Weight limit (kg) <span className="required">*</span>
              </label>
              <input
                type="number"
                id="weightLimit"
                name="weightLimit"
                value={formData.weightLimit}
                onChange={handleInputChange}
                className="form-input"
                step="0.001"
                min="0"
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
                <label htmlFor="make" className="form-label">
                 Make <span className="required">*</span>
                </label>
                <select
                    id="make"
                    name="make"
                    value={formData.make}
                    onChange={handleMakeChange}
                    className="form-select"
                    required
                    >
                    <option value="">Select a make</option>
                    {makes.map(make => (
                        <option key={make.id} value={make.id}>
                            {make.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="model" className="form-label">
                 Model <span className="required">*</span>
                </label>
                <select
                    id="model"
                    name="model"
                    value={formData.model}
                    onChange={handleInputChange}
                    className="form-select"
                    required
                    >
                    <option value="">Select a model</option>
                    {models.map(model => (
                        <option key={model.id} value={model.id}>
                        {model.name}
                        </option>
                    ))}
                </select>
            </div>
          </div>

          
          {/* Existing images */}
          {imageUrls.length > 0 && (
            <div className="form-group">
              <label className="form-label">Current Images</label>
              <div className="image-preview-grid">
                {imageUrls.map((url, index) => (
                  <div key={index} className="image-preview-item">
                    <SecureImage imageUrl={url} altText={`Vehicle ${index + 1}`} className="image-preview" />
                    <button
                      type="button"
                      onClick={() => handleRemoveExistingImage(index)}
                      className="btn-remove-image"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* New images upload */}
          <div className="form-group">
            <label className="form-label">
              {imageUrls.length > 0 ? 'Add New Images' : 'Vehicle Images'}{' '}
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
                  <div className="upload-icon">📷</div>
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
              onClick={() => navigate(`/vehicles/${id}`)}
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

export default EditVehiclePage;
