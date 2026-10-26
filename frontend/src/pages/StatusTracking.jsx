import React, { useState } from 'react';
import { api } from '../services/api';

export default function StatusTracking({ initialCode }) {
    const [code, setCode] = useState(initialCode || '');
    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [cancelReason, setCancelReason] = useState('');
    const [showCancelBox, setShowCancelBox] = useState(false);
    const [actionMessage, setActionMessage] = useState('');

    async function handleSearch(e) {
        if (e) e.preventDefault();
        if (!code.trim()) return;
        setError('');
        setActionMessage('');
        setLoading(true);
        try {
            const data = await api.trackBooking(code.trim());
            setBooking(data);
        } catch (err) {
            setError(err.message || 'Appointment not found');
            setBooking(null);
        } finally {
            setLoading(false);
        }
    }

    async function handleCancel() {
        if (!cancelReason.trim()) {
            setError('Please provide a reason for cancellation.');
            return;
        }
        setLoading(true);
        setError('');
        try {
            await api.cancelBooking(booking.id, cancelReason.trim());
            setActionMessage('Appointment cancelled successfully.');
            setShowCancelBox(false);
            setCancelReason('');
            // Reload status
            const updated = await api.trackBooking(booking.referenceCode);
            setBooking(updated);
        } catch (err) {
            setError(err.message || 'Error cancelling appointment');
        } finally {
            setLoading(false);
        }
    }

    function getBadge(status) {
        switch (status) {
            case 'CONFIRMED':
                return <span className="badge bg-success fs-6">CONFIRMED</span>;
            case 'REQUESTED':
                return <span className="badge bg-warning text-dark fs-6">PENDING REVIEW</span>;
            case 'CANCELLED':
                return <span className="badge bg-danger fs-6">CANCELLED</span>;
            case 'COMPLETED':
                return <span className="badge bg-info text-dark fs-6">COMPLETED</span>;
            default:
                return <span className="badge bg-secondary fs-6">{status}</span>;
        }
    }

    return (
        <div className="container py-5">
            <div className="text-center mb-4">
                <h2 className="fw-bold">Public Appointment Status Tracker</h2>
                <p className="text-muted">Check the real-time confirmation status of your notarial booking by reference code</p>
            </div>

            <div className="row justify-content-center">
                <div className="col-lg-7 col-md-9">
                    {/* Search Form */}
                    <div className="card shadow-sm border-0 mb-4">
                        <div className="card-body p-4">
                            <form onSubmit={handleSearch} id="form-track">
                                <label htmlFor="input-track-code" className="form-label fw-bold">Enter Booking Reference Code</label>
                                <div className="input-group">
                                    <input
                                        type="text"
                                        className="form-control form-control-lg"
                                        id="input-track-code"
                                        placeholder="e.g. ENOT-2026-A1B2"
                                        value={code}
                                        onChange={(e) => setCode(e.target.value.toUpperCase())}
                                        required
                                    />
                                    <button className="btn btn-primary px-4 fw-semibold" type="submit" id="btn-track-submit" disabled={loading}>
                                        {loading ? <span className="spinner-border spinner-border-sm"></span> : <><i className="bi bi-search me-2"></i>Track</>}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    {error && (
                        <div className="alert alert-danger" role="alert" id="track-error-banner">
                            <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
                        </div>
                    )}

                    {actionMessage && (
                        <div className="alert alert-success" role="alert">
                            <i className="bi bi-check-circle-fill me-2"></i>{actionMessage}
                        </div>
                    )}

                    {/* Booking Details Card */}
                    {booking && (
                        <div className="card shadow-sm border-0" id="tracking-result-card">
                            <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                                <div>
                                    <span className="text-muted small d-block">Reference Code</span>
                                    <strong className="fs-5 text-primary" id="track-result-ref">{booking.referenceCode}</strong>
                                </div>
                                <div id="track-result-status">
                                    {getBadge(booking.status)}
                                </div>
                            </div>
                            <div className="card-body p-4">
                                <div className="row g-3">
                                    <div className="col-sm-6">
                                        <span className="small text-muted d-block">Client Name</span>
                                        <strong>{booking.client?.fullName || 'N/A'}</strong>
                                    </div>
                                    <div className="col-sm-6">
                                        <span className="small text-muted d-block">Notary Public</span>
                                        <strong>{booking.slot?.notary?.user?.fullName || 'N/A'}</strong>
                                    </div>
                                    <div className="col-sm-6">
                                        <span className="small text-muted d-block">Appointment Date</span>
                                        <strong>{booking.slot?.slotDate || 'N/A'}</strong>
                                    </div>
                                    <div className="col-sm-6">
                                        <span className="small text-muted d-block">Time Slot</span>
                                        <strong>{booking.slot?.startTime} &ndash; {booking.slot?.endTime}</strong>
                                    </div>
                                    <div className="col-sm-6">
                                        <span className="small text-muted d-block">Document Service</span>
                                        <span className="badge bg-light text-dark border">{booking.documentType}</span>
                                    </div>
                                    <div className="col-sm-6">
                                        <span className="small text-muted d-block">Created At</span>
                                        <span className="small text-secondary">{new Date(booking.createdAt).toLocaleString()}</span>
                                    </div>
                                    {booking.cancellationReason && (
                                        <div className="col-12">
                                            <div className="alert alert-danger mb-0">
                                                <strong>Cancellation Reason:</strong> {booking.cancellationReason}
                                            </div>
                                        </div>
                                    )}
                                </div>

                                {/* Cancellation trigger if active */}
                                {(booking.status === 'REQUESTED' || booking.status === 'CONFIRMED') && (
                                    <div className="mt-4 pt-3 border-top">
                                        {!showCancelBox ? (
                                            <button
                                                className="btn btn-outline-danger btn-sm"
                                                id="btn-show-cancel"
                                                onClick={() => setShowCancelBox(true)}
                                            >
                                                <i className="bi bi-x-circle me-1"></i>Cancel This Appointment
                                            </button>
                                        ) : (
                                            <div className="p-3 bg-light rounded border">
                                                <h6 className="fw-bold text-danger mb-2">Cancel Appointment</h6>
                                                <p className="small text-muted mb-2">
                                                    Cancelling this appointment will release the time slot back into availability for other clients.
                                                </p>
                                                <div className="mb-3">
                                                    <label htmlFor="input-cancel-reason" className="form-label small fw-bold">Reason for Cancellation</label>
                                                    <textarea
                                                        className="form-control form-control-sm"
                                                        id="input-cancel-reason"
                                                        rows="2"
                                                        placeholder="e.g. Schedule conflict, document already notarized"
                                                        value={cancelReason}
                                                        onChange={(e) => setCancelReason(e.target.value)}
                                                    ></textarea>
                                                </div>
                                                <div className="d-flex gap-2">
                                                    <button
                                                        className="btn btn-danger btn-sm"
                                                        id="btn-confirm-cancel"
                                                        onClick={handleCancel}
                                                        disabled={loading}
                                                    >
                                                        Confirm Cancellation
                                                    </button>
                                                    <button
                                                        className="btn btn-outline-secondary btn-sm"
                                                        onClick={() => setShowCancelBox(false)}
                                                    >
                                                        Back
                                                    </button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
