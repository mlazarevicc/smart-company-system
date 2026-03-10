import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import productService from '../../services/productService';
import './ProductDetailPage.css';
import { mapProductFromApi } from "../../api/product.mapper";
import SecureImage from '../../components/SecureImage';
import { ToastContainer, toast } from 'react-toastify';

const ProductDetailPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  
  const [product, setProduct] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadCategories();
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

  const loadProduct = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await productService.getProductById(id);
      const mappedProduct = mapProductFromApi(response.data);
      setProduct(mappedProduct);
    } catch (err) {
      console.error('Failed to load product:', err);
      setError('Failed to load product details.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    const ok = await confirm({
      title: 'Delete product',
      message: 'Are you sure you want to delete this product? This action cannot be undone.',
      okText: 'Delete',
      cancelText: 'Cancel',
    });
  
    if (!ok) return;

    try {
      await productService.deleteProduct(id);
      toast.success('Product deleted successfully!')
      setTimeout(() => navigate('/warehoproductsuses'), 1500);
    } catch (err) {
      console.error('Failed to delete product:', err);
      toast.error(err.response?.data?.message || 'Failed to delete product.')
    }
  };

  const getCategoryLabel = (categoryValue) => {
    const category = categories.find(cat => cat.value === categoryValue);
    return category ? category.label : categoryValue;
  };

  if (loading) {
    return (
      <div className="pdp-container">
        <div className="pdp-loading">Loading product details...</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="pdp-container">
        <div className="pdp-error-message">{error || 'Product not found.'}</div>
        <button className="pdp-btn pdp-btn-back" onClick={() => navigate('/products')}>
          ← Back to Products
        </button>
      </div>
    );
  }

  return (
    <div className="pdp-container">
      {/* HEADER */}
      <div className="pdp-header">
        <div className="pdp-title-section">
          <h1>{product.name}</h1>
          <span className={`pdp-status-badge ${product.isAvailable ? 'pdp-available' : 'pdp-unavailable'}`}>
            {product.isAvailable ? 'AVAILABLE' : 'OUT OF STOCK'}
          </span>
        </div>
        <div className="pdp-header-actions">
          <button className="pdp-btn pdp-btn-back" onClick={() => navigate('/products')}>View List</button>
          <button className="pdp-btn pdp-btn-edit" onClick={() => navigate(`/products/${id}/edit`)}>Edit</button>
          <button className="pdp-btn pdp-btn-delete" onClick={handleDelete}>Delete</button>
        </div>
      </div>

      {/* DASHBOARD GRID */}
      <div className="pdp-dashboard">
        
        {/* LEVA KOLONA: Slika i Kratki Info */}
        <div className="pdp-left-column">
          <div className="pdp-card pdp-image-card">
             <SecureImage 
                imageUrl={product.productImage || '/static/product.jpg'} 
                altText={`${product.name} - Product Image`} 
                className="pdp-main-image" 
              />
          </div>

          <div className="pdp-card">
            <h2 className="pdp-card-title">Quick Details</h2>
            <div className="pdp-info-grid">
              <div className="pdp-info-group">
                <label>SKU</label>
                <p><code>{product.sku}</code></p>
              </div>
              <div className="pdp-info-group">
                <label>Category</label>
                <p><span className="pdp-category-badge">{getCategoryLabel(product.category)}</span></p>
              </div>
              <div className="pdp-info-group">
                <label>Price</label>
                <p className="pdp-price">€{Number(product.price).toFixed(2)}</p>
              </div>
              <div className="pdp-info-group">
                <label>Weight</label>
                <p>{Number(product.weight).toFixed(3)} kg</p>
              </div>
            </div>
          </div>
        </div>

        {/* DESNA KOLONA: Opis i Fabrike */}
        <div className="pdp-right-column">
          <div className="pdp-card">
            <h2 className="pdp-card-title">Description</h2>
            <p className="pdp-description-text">
              {product.description || <span className="pdp-placeholder">No description provided for this product.</span>}
            </p>
          </div>

          <div className="pdp-card pdp-factories-section">
            <div className="pdp-card-header">
              <h2 className="pdp-card-title">Manufactured In</h2>
              <span className="pdp-count-badge">{product.factories?.length || 0} Factories</span>
            </div>
            
            {product.factories && product.factories.length > 0 ? (
              <div className="pdp-factory-grid">
                {product.factories.map(factory => (
                  <Link 
                    key={factory.id} 
                    to={`/factories/${factory.id}`}
                    className="pdp-factory-card"
                  >
                    <div className="pdp-fc-icon-wrap">
                      <span className="pdp-fc-icon">🏭</span>
                    </div>
                    <div className="pdp-fc-details">
                      <span className="pdp-fc-name">{factory.name}</span>
                      <span className="pdp-fc-action">View details →</span>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="pdp-placeholder-box">
                <p>No factories assigned to this product yet.</p>
              </div>
            )}
          </div>
        </div>

      </div>
      <ToastContainer />
    </div>
  );
};

export default ProductDetailPage;