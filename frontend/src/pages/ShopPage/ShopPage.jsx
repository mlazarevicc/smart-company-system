import React, { useState, useEffect } from 'react';
import productService from '../../services/productService';
import { useCart } from '../../context/CartContext';
import { mapProductFromApi } from "../../api/product.mapper";
import { useNavigate, useLocation } from 'react-router-dom';
import './ShopPage.css';
import SecureImage from '../../components/SecureImage';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const ShopPage = () => {
  const { cart, addToCart, getCartItemsCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation(); 

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Filters & Pagination
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [availabilityFilter, setAvailabilityFilter] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(12);

  const [quantities, setQuantities] = useState({});

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      loadProducts();
    }, 500);
    return () => clearTimeout(timeoutId);
  }, [page, pageSize, searchQuery, categoryFilter, availabilityFilter, sortBy, sortDir]);

  useEffect(() => {
    if (location.state?.orderSuccess) {
        toast.success("Order successfully created! Invoice has been sent to your email.", {
            position: "top-center",
            autoClose: 5000,
            theme: "colored"
        });
    
        navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  const loadCategories = async () => {
    try {
      const response = await productService.getCategories();
      setCategories(response.data);
    } catch (err) {
      console.error('Error:', err);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    setError('');
    try {
      const isAvailable = availabilityFilter === '' ? null : availabilityFilter === 'true';
      const response = await productService.getAllProducts(
        page,
        pageSize,
        searchQuery,
        categoryFilter,
        isAvailable,
        sortBy,
        sortDir
      );
      const mappedProducts = response.data.content.map(mapProductFromApi);
      setProducts(mappedProducts);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError('Failed to load products. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = (product, val) => {
    if (val === '') {
        setQuantities(prev => ({ ...prev, [product.id]: '' }));
        return;
    }

    const value = parseInt(val);
    
    if (isNaN(value) || value < 1) {
        setQuantities(prev => ({ ...prev, [product.id]: 1 }));
    } else if (value > product.totalQuantity) {
        setQuantities(prev => ({ ...prev, [product.id]: product.totalQuantity }));
        toast.info(`There's ${product.totalQuantity} units in stock.`, {
            position: "bottom-center",
            autoClose: 2000,
        });
    } else {
        setQuantities(prev => ({ ...prev, [product.id]: value }));
    }
  };


  const handleAddToCart = (product) => {
    const rawVal = quantities[product.id];
    let qty = (rawVal === '' || rawVal === undefined) ? 1 : parseInt(rawVal);
    
    const currentCartItem = cart?.find(item => item.id === product.id);
    const alreadyInCart = currentCartItem ? currentCartItem.quantity : 0;
    
    if (qty + alreadyInCart > product.totalQuantity) {
        toast.warning(`You can't add more! You already have ${alreadyInCart} units in cart, and there are only ${product.totalQuantity} units in stock.`, {
            position: "bottom-center",
            autoClose: 4000,
            theme: "colored"
        });
        setQuantities(prev => ({ ...prev, [product.id]: product.totalQuantity - alreadyInCart }));
        return;
    }

    addToCart(product, qty);
    
    setQuantities(prev => ({ ...prev, [product.id]: 1 }));
    
    toast.success(`Successfully added ${qty} x ${product.name} to cart!`, {
        position: "bottom-right",
        autoClose: 3000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        theme: "colored",
    });
  };

  // Handlers for filters and sorting
  const handleSearch = (e) => {
    setSearchQuery(e.target.value);
    setPage(0);
  };

  const handleCategoryChange = (e) => {
    setCategoryFilter(e.target.value);
    setPage(0);
  };

  const handleAvailabilityChange = (e) => {
    setAvailabilityFilter(e.target.value);
    setPage(0);
  };

  const handlePageSizeChange = (e) => {
    setPageSize(Number(e.target.value));
    setPage(0);
  };

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDir('asc');
    }
  };

  return (
    <div className="shop-page">
      <div className="shop-header">
        <h1>Our Offers</h1>
        <button className="cart-button" onClick={() => navigate('/checkout')}>
          Cart ({getCartItemsCount()})
        </button>
      </div>

      {/* Filters */}
      <div className="shop-filters">
        <div className="filter-group">
          <input
            type="text"
            placeholder="Search products..."
            value={searchQuery}
            onChange={handleSearch}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <select
            value={categoryFilter}
            onChange={handleCategoryChange}
            className="filter-select"
          >
            <option value="">All Categories</option>
            {categories.map(cat => (
              <option key={cat.value} value={cat.value}>{cat.label}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <select
            value={availabilityFilter}
            onChange={handleAvailabilityChange}
            className="filter-select"
          >
            <option value="">All Status</option>
            <option value="true">Available</option>
            <option value="false">Not Available</option>
          </select>
        </div>

        <div className="filter-group">
          <select
            value={pageSize}
            onChange={handlePageSizeChange}
            className="filter-select"
          >
            <option value={12}>12 per page</option>
            <option value={25}>25 per page</option>
            <option value={50}>50 per page</option>
            <option value={100}>100 per page</option>
          </select>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading products...</div>
      ) : (
        <div className="product-grid">
          {products.length === 0 ? (
            <div className="no-data">No products match your search criteria.</div>
          ) : (
            products.map(product => {
              const outOfStock = product.totalQuantity === undefined || product.totalQuantity <= 0;
              const canBuy = product.isAvailable && !outOfStock;

              return (
                <div key={product.id} className={`product-card ${!canBuy ? 'product-disabled' : ''}`}>
                  <div className="product-image">
                    <SecureImage imageUrl={product.productImage || '/static/product.jpg'} altText={product.name} />
                    {!product.isAvailable && <span className="badge badge-unavailable">Unavailable</span>}
                    {product.isAvailable && outOfStock && <span className="badge badge-soldout">Sold Out</span>}
                  </div>
                  <div className="product-details">
                    <h3>{product.name}</h3>
                    <span className="category-tag">{product.category}</span>
                    <p className="price">${Number(product.price).toFixed(2)}</p>

                    <p className="stock-info">
                      <span className="icon">📦</span>
                      {product.totalQuantity > 0 ? `In stock: ${product.totalQuantity} units.` : "Out of stock"}
                    </p>

                    <div className="add-to-cart-section">
                      <input
                        type="number"
                        min="1"
                        max={product.totalQuantity}
                        value={quantities[product.id] || 1}
                        onChange={(e) => handleQuantityChange(product, e.target.value)}
                        className="qty-input"
                        disabled={!canBuy}
                      />
                      <button
                        className="btn-add-cart"
                        onClick={() => handleAddToCart(product)}
                        disabled={!canBuy}
                      >
                        {canBuy ? 'Add to Cart' : 'Unavailable'}
                      </button>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button className="pagination-btn" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span className="pagination-info">Page {page + 1} of {totalPages}</span>
          <button className="pagination-btn" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      )}
      <ToastContainer />
    </div>
  );
};

export default ShopPage;
