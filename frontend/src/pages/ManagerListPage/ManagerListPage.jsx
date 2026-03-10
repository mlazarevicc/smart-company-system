import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { getManagers, toggleManagerBlock } from '../../services/managerService';
import './ManagerList.css';
import { mapManagerFromApi } from "../../api/manager.mapper";
import SecureImage from '../../components/SecureImage';
import { confirm } from '../../components/Dialog/ConfirmDialog';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const ManagerListPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Filteri
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState(''); 
  
  // Sortiranje
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState('desc');
  
  // Paginacija - pageSize sada može da se menja
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10 // Default vrednost
  });
  
  const [success, setSuccess] = useState('');

  // 1. Debounce za pretragu
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(searchQuery.trim());
      setPagination(prev => ({ ...prev, currentPage: 0 }));
    }, 400);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  // 2. Load Effect - reaguje i na promenu pageSize-a
  useEffect(() => {
    fetchManagers();
  }, [pagination.currentPage, pagination.pageSize, sortBy, sortDirection, debouncedQuery, statusFilter]);
  
  useEffect(() => {
    if (searchParams.get('registered') === 'true') {
      setSuccess('Manager registered successfully!');
      setTimeout(() => setSuccess(''), 5000);
    }
  }, [searchParams]);

  const fetchManagers = async () => {
    setLoading(true);
    setError('');
    
    try {
      const params = {
        page: pagination.currentPage,
        size: pagination.pageSize,
        sortBy: sortBy,
        direction: sortDirection,
        query: debouncedQuery || undefined,
        isBlocked: statusFilter === 'BLOCKED' ? true : (statusFilter === 'ACTIVE' ? false : undefined)
      };
      
      const response = await getManagers(params);
      const mappedManagers = response.content.map(mapManagerFromApi);
      
      setManagers(mappedManagers);
      setPagination(prev => ({
        ...prev,
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      }));
    } catch (err) {
      console.error('Error fetching managers:', err);
      setError('Failed to load managers.');
    } finally {
      setLoading(false);
    }
  };

  const handleSortChange = (column) => {
    if (sortBy === column) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDirection('asc');
    }
    setPagination(prev => ({ ...prev, currentPage: 0 }));
  };

  const renderSortIndicator = (column) => {
    if (sortBy === column) {
      return sortDirection === 'asc' ? ' ▲' : ' ▼';
    }
    return <span className="sort-placeholder"> ↕</span>;
  };

  const handlePageSizeChange = (e) => {
    const newSize = parseInt(e.target.value);
    setPagination(prev => ({
      ...prev,
      pageSize: newSize,
      currentPage: 0
    }));
  };

  const handleBlockToggle = async (managerId, currentBlockStatus) => {
    const action = currentBlockStatus ? 'unblock' : 'block';
    const actionCapitalized = currentBlockStatus ? 'Unblock' : 'Block';
    
    const ok = await confirm({
      title: `${actionCapitalized} manager`,
      message: `Are you sure you want to ${action} this manager?`,
      okText: actionCapitalized,
      cancelText: 'Cancel',
    });
  
    if (!ok) return;

    try {
      await toggleManagerBlock(managerId, !currentBlockStatus);
      // setSuccess(`Manager ${action}ed successfully!`);
      toast.success(`Manager ${action}ed successfully!`)
      fetchManagers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      // setError(`Failed to ${action} manager.`);
      toast.error(`Failed to ${action} manager.`)
    }
  };

  return (
    <div className="manager-list-container">
      <div className="page-header">
        <div>
          <h1>Manager Management</h1>
          <p>Real-time overview of platform administrators</p>
        </div>
        <Link to="/managers/register" className="mng-btn-primary">
          + Register New Manager
        </Link>
      </div>

      {success && <div className="alert-success">✓ {success}</div>}
      {error && <div className="alert-error">✕ {error}</div>}

      <div className="filters-container">
        <div className="filter-row">
          <div className="search-wrapper">
            <input
              type="text"
              placeholder="Search by name, username, or email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="search-input"
            />
          </div>
          
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPagination(prev => ({ ...prev, currentPage: 0 }));
            }}
            className="filter-select"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active Only</option>
            <option value="BLOCKED">Blocked Only</option>
          </select>

          <div className="filter-group">
          <select 
            value={pagination.pageSize} 
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

          {(searchQuery || statusFilter) && (
            <button 
              className="btn-clear" 
              onClick={() => {setSearchQuery(''); setStatusFilter('');}}
            >
              Reset Filters
            </button>
          )}
        </div>
      </div>

      {loading ? (
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Synchronizing with database...</p>
        </div>
      ) : managers.length === 0 ? (
        <div className="empty-state">
          <p>No managers match your criteria.</p>
        </div>
      ) : (
        <>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Profile</th>
                  <th className="sortable-header" onClick={() => handleSortChange('firstName')}>
                    Name {renderSortIndicator('firstName')}
                  </th>
                  <th className="sortable-header" onClick={() => handleSortChange('username')}>
                    Username {renderSortIndicator('username')}
                  </th>
                  <th className="sortable-header" onClick={() => handleSortChange('email')}>
                    Email {renderSortIndicator('email')}
                  </th>
                  <th className="sortable-header" onClick={() => handleSortChange('createdAt')}>
                    Joined {renderSortIndicator('createdAt')}
                  </th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {managers.map((manager) => (
                  <tr key={manager.id}>
                    <td>
                      <div className="avatar">
                        {manager.profileImage ? (
                          <SecureImage src={manager.profileImage} alt="profile" />
                        ) : (
                          <div className="avatar-placeholder">
                            {manager.firstName[0]}{manager.lastName[0]}
                          </div>
                        )}
                      </div>
                    </td>
                    <td className="name-cell">
                      <strong>{manager.firstName} {manager.lastName}</strong>
                    </td>
                    <td>{manager.username}</td>
                    <td>{manager.email}</td>
                    <td>{new Date(manager.createdAt).toLocaleDateString()}</td>
                    <td>
                      <span className={`status-badge ${manager.isBlocked ? 'blocked' : 'active'}`}>
                        {manager.isBlocked ? 'Blocked' : 'Active'}
                      </span>
                    </td>
                    <td className="actions-cell">
                      <button
                        className={`btn-icon ${manager.isBlocked ? 'unblock' : 'block'}`}
                        onClick={() => handleBlockToggle(manager.id, manager.isBlocked)}
                      >
                        {manager.isBlocked ? 'Unblock' : 'Block'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="pagination-wrapper">
            <div className="pagination">
              <button
                className="btn-outline"
                onClick={() => setPagination(p => ({...p, currentPage: 0}))}
                disabled={pagination.currentPage === 0}
              >
                First
              </button>
              <button
                className="btn-outline"
                onClick={() => setPagination(p => ({...p, currentPage: p.currentPage - 1}))}
                disabled={pagination.currentPage === 0}
              >
                Previous
              </button>
              
              <span className="pagination-info">
                Page {pagination.currentPage + 1} of {pagination.totalPages}
              </span>
              
              <button
                className="btn-outline"
                onClick={() => setPagination(p => ({...p, currentPage: p.currentPage + 1}))}
                disabled={pagination.currentPage >= pagination.totalPages - 1}
              >
                Next
              </button>
              <button
                className="btn-outline"
                onClick={() => setPagination(p => ({...p, currentPage: pagination.totalPages - 1}))}
                disabled={pagination.currentPage >= pagination.totalPages - 1}
              >
                Last
              </button>
            </div>
          </div>
        </>
      )}
      <ToastContainer />
    </div>
  );
};

export default ManagerListPage;