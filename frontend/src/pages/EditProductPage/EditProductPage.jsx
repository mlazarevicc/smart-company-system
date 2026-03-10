import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import productService from '../../services/productService';
import './EditProductPage.css';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import SecureImage from '../../components/SecureImage';

const EditProductPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  
  const [categories, setCategories] = useState([]);
  const [factories, setFactories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingProduct, setLoadingProduct] = useState(true);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: '',
    price: '',
    weight: '',
    isAvailable: true,
    version: null, // For optimistic locking
    factoryIds: [],
  });

  const [currentImage, setCurrentImage] = useState('');
  const [newImageFile, setNewImageFile] = useState(null);
  const [newImagePreview, setNewImagePreview] = useState(null);

  useEffect(() => {
    loadCategories();
    loadFactories();
    loadProduct();
  }, [id]);

  const loadCategories = async () => {
    try {
      const response = await productService.getCategories();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const loadFactories = async () => {
    try {
      const response = await productService.getAllFactoriesSimple();
      setFactories(response.data);
    } catch (err) {
      console.error('Failed to load factories:', err);
    }
  };

  const loadProduct = async () => {
    setLoadingProduct(true);
    try {
      const response = await productService.getProductById(id);
      const product = response.data;
      
      setFormData({
        name: product.name,
        description: product.description,
        category: product.category,
        price: product.price,
        weight: product.weight,
        isAvailable: product.is_available,
        version: product.version,
        factoryIds: product.factoryIds || [],
      });
      
      setCurrentImage(product.productImage);
    } catch (err) {
      console.error('Failed to load product:', err);
      setError('Failed to load product details.');
    } finally {
      setLoadingProduct(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleFactoryChange = (e) => {
    const selectedOptions = Array.from(e.target.selectedOptions).map(opt => Number(opt.value));
    setFormData({
      ...formData,
      factoryIds: selectedOptions,
    });
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    
    if (file) {
      if (!file.type.startsWith('image/')) {
        setError('Please select a valid image file.');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        setError('Image size must be less than 5MB.');
        return;
      }

      setNewImageFile(file);
      
      const reader = new FileReader();
      reader.onloadend = () => {
        setNewImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
      setError('');
    }
  };

  const handleRemoveNewImage = () => {
    setNewImageFile(null);
    setNewImagePreview(null);
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
        await productService.updateProduct(id, formData, newImageFile);
        toast.success('Product updated successfully!')
        setTimeout(() => navigate(`/products`), 1500);
        
    } catch (err) {
        console.error('Failed to update product:', err);
        
        // Handle optimistic locking conflict
        if (err.response?.status === 409 || err.response?.data?.message?.includes('version')) {
            setError('This product was modified by another user. Please refresh and try again.');
            // Reload product to get latest version
            loadProduct();
        } else {
            setError(
                err.response?.data?.message || 
                'Failed to update product. Please try again.'
            );
        }
    } finally {
        setLoading(false);
    }
  };

  if (loadingProduct) {
    return (
      <div className="edit-product-container">
        <div className="loading">Loading product...</div>
      </div>
    );
  }

  return (
    <div className="edit-product-container">
      <div className="edit-product-header">
        <h1>Edit Product</h1>
        <button 
          className="btn-back" 
          onClick={() => navigate('/products')}
        >
          ← Back to Products
        </button>
      </div>

      <div className="edit-product-card">
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
                step="0.001"
                min="0"
                required
              />
            </div>
          </div>

          {/* Factories Multi-Select */}
          <div className="form-group">
            <label htmlFor="factories" className="form-label">
              Factories (where this product is manufactured)
            </label>
            <select
              id="factories"
              name="factories"
              multiple
              value={formData.factoryIds.map(String)}
              onChange={handleFactoryChange}
              className="form-select-multiple"
              size="5"
            >
              {factories.map(factory => (
                <option key={factory.id} value={factory.id}>
                  {factory.name}
                </option>
              ))}
            </select>
            <small className="form-hint">
              Hold Ctrl (Cmd on Mac) to select multiple factories
            </small>
          </div>

          {/* Image Update */}
          <div className="form-group">
            <label className="form-label">
              Product Image
            </label>
            
            <div className="current-image-section">
              <p className="section-label">Current Image:</p>
              <SecureImage
                imageUrl={currentImage} 
                altText="Current product" 
                className="current-image"
              />
            </div>

            {!newImagePreview ? (
              <div className="image-upload-area">
                <input
                  type="file"
                  id="image"
                  accept="image/*"
                  onChange={handleImageChange}
                  className="file-input"
                />
                <label htmlFor="image" className="file-label">
                  <p className="upload-text">Click to upload new image</p>
                  <p className="upload-hint">PNG, JPG up to 5MB</p>
                </label>
              </div>
            ) : (
              <div className="new-image-section">
                <p className="section-label">New Image:</p>
                <div className="image-preview-container">
                  <img 
                    src={newImagePreview} 
                    alt="New preview" 
                    className="image-preview"
                  />
                  <button 
                    type="button"
                    onClick={handleRemoveNewImage}
                    className="btn-remove-image"
                  >
                    ✕ Remove
                  </button>
                </div>
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
              {loading ? 'Updating...' : 'Update Product'}
            </button>
          </div>
        </form>
      </div>

      <ToastContainer/>
    </div>
  );
};

export default EditProductPage;
