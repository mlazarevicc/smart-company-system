import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AsyncSelect from 'react-select/async';
import productService from '../../services/productService';
import './CreateProductPage.css';

const CreateProductPage = () => {
  const navigate = useNavigate();
  
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: '',
    price: '',
    weight: '',
    isAvailable: true,
    factoryIds: [],
  });

  // State za vizuelni prikaz selektovanih fabrika u AsyncSelect-u
  const [selectedFactories, setSelectedFactories] = useState([]);

  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);

  useEffect(() => {
    loadCategories();
    // Uklonili smo loadFactories() jer sada pretražujemo fabrike asinhrono
  }, []);

  const loadCategories = async () => {
    try {
      const response = await productService.getCategories();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  // Funkcija koja poziva API svaki put kada korisnik kuca u select polje
  const loadFactoryOptions = async (inputValue) => {
    if (!inputValue) {
      return [];
    }
    
    try {
      const response = await productService.searchFactories(inputValue);
      
      // Pošto Spring Boot vraća PaginatedResponse, lista je najverovatnije u response.data.content
      // Ako tvoj backend vraća direktno niz, koristi samo response.data
      const factoriesData = response.data.content || response.data; 
      
      // react-select očekuje niz objekata u formatu { value, label }
      return factoriesData.map(factory => ({
        value: factory.id,
        label: factory.name
      }));
    } catch (err) {
      console.error('Failed to search factories:', err);
      return [];
    }
  };

  // Funkcija koja se okida kada korisnik izabere ili obriše fabriku iz menija
  const handleFactoryChange = (selectedOptions) => {
    // Čuvamo cele objekte za vizuelni prikaz (čipove)
    setSelectedFactories(selectedOptions || []);
    
    // U formData čuvamo samo niz ID-jeva koji se šalje na backend
    setFormData({
      ...formData,
      factoryIds: selectedOptions ? selectedOptions.map(opt => opt.value) : [],
    });
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        setError('Please select a valid image file.');
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        setError('Image size must be less than 5MB.');
        return;
      }

      setImageFile(file);
      
      // Create preview
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
      setError('');
    }
  };

  const handleRemoveImage = () => {
    setImageFile(null);
    setImagePreview(null);
  };

  const validateForm = () => {
    if (!formData.name.trim()) {
      setError('Product name is required.');
      return false;
    }
    if (!formData.description.trim()) {
      setError('Description is required.');
      return false;
    }
    if (!formData.category) {
      setError('Category is required.');
      return false;
    }
    if (!formData.price || Number(formData.price) <= 0) {
      setError('Price must be greater than 0.');
      return false;
    }
    if (!formData.weight || Number(formData.weight) <= 0) {
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
      await productService.createProduct(formData, imageFile);
      alert('Product created successfully!');
      navigate('/products');
    } catch (err) {
      console.error('Failed to create product:', err);
      setError(
        err.response?.data?.message || 
        'Failed to create product. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  // Prilagođavanje stilova za react-select kako bi se uklopio sa tvojim CSS-om
  const customSelectStyles = {
    control: (provided, state) => ({
      ...provided,
      padding: '2px',
      borderRadius: '8px',
      borderColor: state.isFocused ? '#00A8E8' : '#e2e8f0',
      boxShadow: state.isFocused ? '0 0 0 1px #00A8E8' : 'none',
      '&:hover': {
        borderColor: state.isFocused ? '#00A8E8' : '#cbd5e1'
      }
    }),
    multiValue: (provided) => ({
      ...provided,
      backgroundColor: '#e0f2fe',
      borderRadius: '4px',
    }),
    multiValueLabel: (provided) => ({
      ...provided,
      color: '#0369a1',
      fontWeight: '500',
    }),
    multiValueRemove: (provided) => ({
      ...provided,
      color: '#0369a1',
      ':hover': {
        backgroundColor: '#bae6fd',
        color: '#0c4a6e',
      },
    }),
  };

  return (
    <div className="create-product-container">
      <div className="create-product-header">
        <h1>Create New Product</h1>
        <button 
          className="btn-back" 
          onClick={() => navigate('/products')}
        >
          ← Back to Products
        </button>
      </div>

      <div className="create-product-card">
        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Product Name */}
          <div className="form-group">
            <label htmlFor="name" className="form-label">
              Product Name <span className="required">*</span>
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              className="form-input"
              placeholder="e.g., Coca-Cola 330ml"
              required
            />
          </div>

          {/* Description */}
          <div className="form-group">
            <label htmlFor="description" className="form-label">
              Description <span className="required">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              className="form-textarea"
              placeholder="Enter product description..."
              rows="4"
              required
            />
          </div>

          {/* Category */}
          <div className="form-group">
            <label htmlFor="category" className="form-label">
              Category <span className="required">*</span>
            </label>
            <select
              id="category"
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              className="form-select"
              required
            >
              <option value="">Select a category</option>
              {categories.map(cat => (
                <option key={cat.value} value={cat.value}>
                  {cat.label}
                </option>
              ))}
            </select>
          </div>

          {/* Price & Weight */}
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="price" className="form-label">
                Price (€) <span className="required">*</span>
              </label>
              <input
                type="number"
                id="price"
                name="price"
                value={formData.price}
                onChange={handleInputChange}
                className="form-input"
                placeholder="0.00"
                step="0.01"
                min="0"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="weight" className="form-label">
                Weight (kg) <span className="required">*</span>
              </label>
              <input
                type="number"
                id="weight"
                name="weight"
                value={formData.weight}
                onChange={handleInputChange}
                className="form-input"
                placeholder="0.000"
                step="0.001"
                min="0"
                required
              />
            </div>
          </div>

          {/* Factories Async Multi-Select umesto obicnog select-a */}
          <div className="form-group">
            <label htmlFor="factories" className="form-label">
              Factories (where this product is manufactured)
            </label>
            <AsyncSelect
              isMulti
              cacheOptions
              defaultOptions={false}
              loadOptions={loadFactoryOptions}
              onChange={handleFactoryChange}
              value={selectedFactories}
              placeholder="Enter a name to search for factories..."
              noOptionsMessage={({ inputValue }) => 
                !inputValue ? "Start typing..." : "No factories found"
              }
              styles={customSelectStyles}
              classNamePrefix="react-select"
            />
            <small className="form-hint">
              Search and select one or more factories from the drop-down menu
            </small>
          </div>

          {/* Image Upload */}
          <div className="form-group">
            <label className="form-label">
              Product Image
            </label>
            
            {!imagePreview ? (
              <div className="image-upload-area">
                <input
                  type="file"
                  id="image"
                  accept="image/*"
                  onChange={handleImageChange}
                  className="file-input"
                />
                <label htmlFor="image" className="file-label">
                  <p className="upload-text">Click to upload image</p>
                  <p className="upload-hint">PNG, JPG up to 5MB</p>
                </label>
              </div>
            ) : (
              <div className="image-preview-container">
                <img 
                  src={imagePreview} 
                  alt="Preview" 
                  className="image-preview"
                />
                <button 
                  type="button"
                  onClick={handleRemoveImage}
                  className="btn-remove-image"
                >
                  ✕ Remove
                </button>
              </div>
            )}
          </div>

          {/* Availability */}
          <div className="form-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="isAvailable"
                checked={formData.isAvailable}
                onChange={handleInputChange}
                className="checkbox-input"
              />
              <span className="checkbox-text">Available for sale</span>
            </label>
          </div>

          {/* Submit Buttons */}
          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/products')}
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
              {loading ? 'Creating...' : 'Create Product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateProductPage;