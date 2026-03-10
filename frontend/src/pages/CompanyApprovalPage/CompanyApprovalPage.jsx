// src/pages/company/CompanyApprovalPage.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import companyService from '../../services/companyService';
import locationService from '../../services/locationService';
import { ToastContainer, toast } from 'react-toastify';
import { mapCompanyFromApi } from '../../api/company.mapper';
import './CompanyApprovalPage.css';
import SecureImage from '../../components/SecureImage';
import { reasonConfirm } from '../../components/Dialog/ReasonDialog';
import ProofDialog from '../../components/Dialog/ProofDialog';

const CompanyApprovalPage = () => {
  const navigate = useNavigate();

  const [companies, setCompanies] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(10);

  const [showProofDialog, setShowProofDialog] = useState(false);
  const [selectedProofs, setSelectedProofs] = useState([]);
  const [selectedCompanyName, setSelectedCompanyName] = useState('');

  useEffect(() => {
    loadCompanies();
  }, [page]);

  const loadCompanies = async () => {
    setLoading(true);
    setError('');

    try {
       const response = await companyService.getPendingCompanies(
        page,
        pageSize,
        'name',
       );

      const data = response.data;
      const mapped = (data.content || []).map(mapCompanyFromApi);
      setCompanies(mapped);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error('Failed to load companies:', err);
      setError('Failed to load companies. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handlePrevPage = () => {
    if (page > 0) setPage((prev) => prev - 1);
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) setPage((prev) => prev + 1);
  };

  const handleView = (id) => {
    const company = companies.find(c => c.id === id);
    if (!company) return;

    console.log(company)
    setSelectedProofs(company.proofOfOwnershipUrls || []);
    setSelectedCompanyName(company.name);
    setShowProofDialog(true);
  };

  const handleApprove = async (id, name) => {
    const { ok, reason } = await reasonConfirm({
        title: 'Approve company',
        message: `You are about to approve the company ${name}. Insert a reason why.`,
        okText: 'Submit',
        cancelText: 'Cancel',
      });

      console.log(ok, reason)

      if (!ok) return;``
    
      try {
        await companyService.approveCompany(id, {reason: reason, approved: true});
        toast.success('Company approved successfully!');
        setTimeout(() => window.location.reload(), 500);
      } catch (err) {
        console.error('Failed to approve company:', err);
        toast.error('Failed to approve company.');
      }
  }

  const handleReject = async (id, name) => {
    const { ok, reason } = await reasonConfirm({
        title: 'Reject company',
        message: `You are about to reject the company ${name}. Insert a reason why.`,
        okText: 'Submit',
        cancelText: 'Cancel',
      });
    
      console.log(ok, reason)

      if (!ok) return;
    
      try {
        await companyService.rejectCompany(id, {reason: reason, approved: false});
        toast.success('Company rejected successfully!');
        setTimeout(() => window.location.reload(), 500);
      } catch (err) {
        console.error('Failed to rejected company:', err);
        toast.error('Failed to rejected company.');
      }
  }

  return (
    <div className="company-list-container">
      <div className="company-list-header">
        <h1>Pending companies</h1>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading pending companies...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="companies-table">
              <thead>
                <tr>
                  <th>Image</th>
                  <th>Owner name</th>
                  <th>Company name</th>
                  <th>City / Country</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {companies.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="no-data">
                      No companies found. Adjust filters or create your first company.
                    </td>
                  </tr>
                ) : (
                  companies.map((company) => (
                    <tr key={company.id}>
                      <td>
                        <SecureImage 
                          imageUrl={company.images[0] || '/static/company.jpg'} 
                          altText={company.name}
                          className="company-thumbnail"
                        />
                      </td>
                      <td>{company.ownerName}</td>
                      <td>{company.name}</td>
                      <td>
                        {company.city}, {company.country}
                      </td>
                      <td>
                        <div className="actions">
                          <button
                            className="btn-view"
                            onClick={() => handleView(company.id)}
                          >
                            View Proof
                          </button>
                          <button 
                          className="btn-approve"
                          onClick={() => handleApprove(company.id, company.name)}
                        >
                          Approve
                        </button>
                        <button 
                          className="btn-reject"
                          onClick={() => handleReject(company.id, company.name)}
                        >
                          Reject
                        </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button
              className="pagination-btn"
              onClick={handlePrevPage}
              disabled={page === 0}
            >
              Previous
            </button>
            <span className="pagination-info">
              Page {page + 1} of {Math.max(totalPages, 1)} ({totalElements} results)
            </span>
            <button
              className="pagination-btn"
              onClick={handleNextPage}
              disabled={page >= totalPages - 1}
            >
              Next
            </button>
          </div>
        </>
      )}

      {showProofDialog && (
        <ProofDialog
          show={showProofDialog}
          title={`Proof of ownership - ${selectedCompanyName}`}
          message=""
          proofs={selectedProofs}
          proceed={() => setShowProofDialog(false)}
        />
      )}
      <ToastContainer />
    </div>
  );
};

export default CompanyApprovalPage;
