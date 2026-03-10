import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import productService from '../../services/productService';
import './ProductListPage.css';
import { mapProductFromApi } from "../../api/product.mapper";
import { confirm } from '../../components/Dialog/ConfirmDialog';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const ProductListPage = () => {
  const navigate = useNavigate();
  
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [availabilityFilter, setAvailabilityFilter] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [page, pageSize, searchQuery, categoryFilter, availabilityFilter, sortBy, sortDir]);

  const loadCategories = async () => {
    try {
      const response = await productService.getCategories();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
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
      setTotalElements(response.data.totalElements);
    } catch (err) {
      console.error('Failed to load products:', err);
      setError('Failed to load products. Please try again.');
    } finally {
      setLoading(false);
    }
  };

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

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDir('asc');
    }
  };
  
  const handleDelete = async (id) => {
    const ok = await confirm({
      title: 'Delete product',
      message: 'Are you sure you want to delete this product?',
      okText: 'Delete',
      cancelText: 'Cancel',
    });
  
    if (!ok) return;

    try {
      await productService.deleteProduct(id);
      loadProducts();
      toast.success('Product deleted successfully!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete product.');
      console.error('Failed to delete product:', err);
    }
  };

  const getCategoryLabel = (categoryValue) => {
    const category = categories.find(cat => cat.value === categoryValue);
    return category ? category.label : categoryValue;
  };

  return (
    <div className="product-list-container">
      <div className="product-list-header">
        <h1>Products</h1>
        <button 
          className="btn-add-new" 
          onClick={() => navigate('/products/create')}
        >
          + Add New Product
        </button>
      </div>

      {/* Filters */}
      <div className="filters-container">
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
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
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
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(0);
            }}
            className="filter-select"
          >
            <option value={10}>10 per page</option>
            <option value={25}>25 per page</option>
            <option value={50}>50 per page</option>
            <option value={100}>100 per page</option>
          </select>
        </div>
      </div>

      {/* Error Message */}
      {error && <div className="error-message">{error}</div>}

      {/* Loading State */}
      {loading ? (
        <div className="loading">Loading products...</div>
      ) : (
        <>
          {/* Products Table */}
          <div className="table-container">
            <table className="products-table">
              <thead>
                <tr>
                  <th onClick={() => handleSort('sku')} className="sortable">
                    SKU {sortBy === 'sku' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('name')} className="sortable">
                    Name {sortBy === 'name' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('category')} className="sortable">
                    Category {sortBy === 'category' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('price')} className="sortable">
                    Price {sortBy === 'price' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('weight')} className="sortable">
                    Weight {sortBy === 'weight' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="no-data">
                      No products found. Create your first product!
                    </td>
                  </tr>
                ) : (
                  products.map(product => (
                    <tr key={product.id}>
                      <td>{product.sku}</td>
                      <td>{product.name}</td>
                      <td>{getCategoryLabel(product.category)}</td>
                      <td>€{Number(product.price).toFixed(2)}</td>
                      <td>{Number(product.weight).toFixed(3)} kg</td>
                      <td>
                        <span className={`status-badge ${product.isAvailable ? 'available' : 'unavailable'}`}>
                          {product.isAvailable ? 'Available' : 'Not Available'}
                        </span>
                      </td>
                      <td className="actions">
                        <button 
                          className="btn-view"
                          onClick={() => navigate(`/products/${product.id}`)}
                        >
                          View
                        </button>
                        <button 
                          className="btn-edit"
                          onClick={() => navigate(`/products/${product.id}/edit`)}
                        >
                          Edit
                        </button>
                        <button 
                          className="btn-delete"
                          onClick={() => handleDelete(product.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button 
                onClick={() => setPage(0)} 
                disabled={page === 0}
                className="pagination-btn"
              >
                First
              </button>
              <button 
                onClick={() => setPage(page - 1)} 
                disabled={page === 0}
                className="pagination-btn"
              >
                Previous
              </button>
              
              <span className="pagination-info">
                Page {page + 1} of {totalPages} ({totalElements} total)
              </span>
              
              <button 
                onClick={() => setPage(page + 1)} 
                disabled={page >= totalPages - 1}
                className="pagination-btn"
              >
                Next
              </button>
              <button 
                onClick={() => setPage(totalPages - 1)} 
                disabled={page >= totalPages - 1}
                className="pagination-btn"
              >
                Last
              </button>
            </div>
          )}
        </>
      )}

<ToastContainer />
    </div>
  );
};

export default ProductListPage;
