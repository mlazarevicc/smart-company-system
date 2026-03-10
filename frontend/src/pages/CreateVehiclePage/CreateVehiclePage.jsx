import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import vehicleService from '../../services/vehicleService';
import './CreateVehiclePage.css';

const CreateVehiclePage = () => {
  const navigate = useNavigate();
  const [makes, setMakes] = useState([]);
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    registrationNumber: '',
    weightLimit: 0.0,
    make: 0,
    model: 0,
  });

  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);

  useEffect(() => {
    loadMakes();
  }, []);

  const loadMakes = async () => {
    try {
      const response = await vehicleService.getMakes();
      setMakes(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const loadModels = async (id) => {
    try {
      const response = await vehicleService.getModelsByMakeId(id);
      setModels(response.data);
    } catch (err) {
      console.error('Failed to load factories:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
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
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      await vehicleService.createVehicle(formData, imageFiles);
      alert('Vehicle created successfully!');
      navigate('/vehicles');
    } catch (err) {
      console.error('Failed to create vehicle:', err);
      setError(
        err.response?.data?.message || 
        'Failed to create vehicle. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-vehicle-container">
      <div className="create-vehicle-header">
        <h1>Create New Vehicle</h1>
        <button 
          className="btn-back" 
          onClick={() => navigate('/vehicles')}
        >
          ← Back to Vehicles
        </button>
      </div>

      <div className="create-vehicle-card">
        {error && (
          <div className="error-message">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            {/* Vehicle Registration Number */}
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
              required
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
                placeholder="0.000"
                step="0.001"
                min="0"
                required
              />
            </div>
        </div>

          {/* Make */}
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
              Make <span className="required">*</span>
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

          {/* Image Upload */}
          <div className="form-group">
            <label className="form-label">
              Vehicle Images <span className="required">*</span>
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
                  <div className="upload-icon">📷</div>
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

          {/* Submit Buttons */}
          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/vehicles')}
              className="btn-cancel"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Create Vehicle'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateVehiclePage;
