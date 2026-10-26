import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function ClientDashboard({ currentUser, onNavigate, onSelectBooking }) {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [cancelModal, setCancelModal] = useState(null);
    const [cancelReason, setCancelReason] = useState('');

    useEffect(() => {
        loadMyBookings();
    }, []);

    async function loadMyBookings() {
        setLoading(true);
        setError('');
        try {
            const data = await api.getMyBookings();
            setBookings(data);
        } catch (err) {
            setError(err.message || 'Error loading appointments');
        } finally {
            setLoading(false);
        }
    }

    async function handleConfirmCancel() {
        if (!cancelReason.trim()) {
            alert('Please enter a cancellation reason.');
            return;
        }
        try {
            await api.cancelBooking(cancelModal.id, cancelReason.trim());
            setCancelModal(null);
            setCancelReason('');
            loadMyBookings();
        } catch (err) {
            alert('Failed to cancel appointment: ' + err.message);
        }
    }

    function getBadge(status) {
        switch (status) {
            case 'CONFIRMED':
                return <span className="badge bg-success">CONFIRMED</span>;
            case 'REQUESTED':
                return <span className="badge bg-warning text-dark">PENDING REVIEW</span>;
            case 'CANCELLED':
                return <span className="badge bg-danger">CANCELLED</span>;
            case 'COMPLETED':
                return <span className="badge bg-info text-dark">COMPLETED</span>;
            default:
                return <span className="badge bg-secondary">{status}</span>;
        }
    }

    return (
        <div className="container py-5">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">My Appointments Dashboard</h2>
                    <p className="text-muted mb-0">Manage your scheduled notarial sessions and tracking references</p>
                </div>
                <button className="btn btn-primary" onClick={() => onNavigate('slots')}>
                    <i className="bi bi-calendar-plus me-2"></i>Book New Appointment
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            )}

            <div className="card shadow-sm border-0">
                <div className="card-body p-0">
                    {loading ? (
                        <div className="text-center py-5">
                            <div className="spinner-border text-primary" role="status"></div>
                            <p className="mt-2 text-muted">Loading appointments...</p>
                        </div>
                    ) : bookings.length === 0 ? (
                        <div className="text-center py-5 text-muted">
                            <i className="bi bi-inbox display-4 mb-2"></i>
                            <p>You have not booked any notary appointments yet.</p>
                            <button className="btn btn-outline-primary btn-sm" onClick={() => onNavigate('slots')}>
                                Browse Available Slots
                            </button>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>Reference Code</th>
                                        <th>Notary</th>
                                        <th>Date &amp; Time</th>
                                        <th>Document Type</th>
                                        <th>Status</th>
                                        <th className="text-end">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {bookings.map((b) => (
                                        <tr key={b.id}>
                                            <td>
                                                <strong className="text-primary">{b.referenceCode}</strong>
                                            </td>
                                            <td>{b.slot?.notary?.user?.fullName || 'Notary'}</td>
                                            <td>
                                                <div>{b.slot?.slotDate}</div>
                                                <div className="small text-muted">{b.slot?.startTime} &ndash; {b.slot?.endTime}</div>
                                            </td>
                                            <td>
                                                <span className="badge bg-light text-dark border">{b.documentType}</span>
                                            </td>
                                            <td>{getBadge(b.status)}</td>
                                            <td className="text-end">
                                                <button
                                                    className="btn btn-sm btn-outline-secondary me-2"
                                                    onClick={() => onSelectBooking(b.referenceCode)}
                                                >
                                                    <i className="bi bi-search me-1"></i>Track
                                                </button>
                                                {(b.status === 'REQUESTED' || b.status === 'CONFIRMED') && (
                                                    <button
                                                        className="btn btn-sm btn-outline-danger"
                                                        onClick={() => setCancelModal(b)}
                                                    >
                                                        Cancel
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Cancel Modal / Dialog */}
            {cancelModal && (
                <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
                    <div className="modal-dialog modal-dialog-centered">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title text-danger">
                                    <i className="bi bi-x-circle me-2"></i>Cancel Appointment
                                </h5>
                                <button type="button" className="btn-close" onClick={() => setCancelModal(null)}></button>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to cancel booking <strong>{cancelModal.referenceCode}</strong>?</p>
                                <div className="mb-3">
                                    <label className="form-label fw-bold small">Reason for Cancellation</label>
                                    <textarea
                                        className="form-control"
                                        rows="3"
                                        placeholder="Please state why you are cancelling"
                                        value={cancelReason}
                                        onChange={(e) => setCancelReason(e.target.value)}
                                    ></textarea>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setCancelModal(null)}>Back</button>
                                <button type="button" className="btn btn-danger" onClick={handleConfirmCancel}>Confirm Cancellation</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
