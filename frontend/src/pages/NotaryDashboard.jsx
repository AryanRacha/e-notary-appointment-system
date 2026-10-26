import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function NotaryDashboard({ currentUser }) {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    // Slot generation form state
    const [genDate, setGenDate] = useState(() => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        return tomorrow.toISOString().split('T')[0];
    });
    const [genStart, setGenStart] = useState('09:00');
    const [genEnd, setGenEnd] = useState('17:00');
    const [generating, setGenerating] = useState(false);

    useEffect(() => {
        loadBookings();
    }, []);

    async function loadBookings() {
        setLoading(true);
        setError('');
        try {
            const data = await api.getNotaryBookings();
            setBookings(data);
        } catch (err) {
            setError(err.message || 'Error loading notary bookings');
        } finally {
            setLoading(false);
        }
    }

    async function handleGenerateSlots(e) {
        e.preventDefault();
        setGenerating(true);
        setError('');
        setSuccessMessage('');
        try {
            const data = await api.generateSlots({
                date: genDate,
                startTime: genStart,
                endTime: genEnd
            });
            setSuccessMessage(`Successfully generated ${data.length} appointment slots for ${genDate}!`);
        } catch (err) {
            setError(err.message || 'Error generating slots');
        } finally {
            setGenerating(false);
        }
    }

    async function handleConfirm(id) {
        try {
            await api.confirmBooking(id);
            setSuccessMessage('Appointment confirmed successfully!');
            loadBookings();
        } catch (err) {
            setError(err.message || 'Error confirming appointment');
        }
    }

    async function handleReject(id) {
        const reason = prompt('Please enter a rejection reason:');
        if (!reason) return;
        try {
            await api.cancelBooking(id, reason);
            setSuccessMessage('Appointment rejected and slot released.');
            loadBookings();
        } catch (err) {
            setError(err.message || 'Error rejecting appointment');
        }
    }

    return (
        <div className="container py-5">
            <div className="mb-4">
                <h2 className="fw-bold mb-1">Notary Public Portal</h2>
                <p className="text-muted">Manage your appointment availability schedule and review citizen booking requests</p>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            )}

            {successMessage && (
                <div className="alert alert-success alert-dismissible fade show" role="alert">
                    <i className="bi bi-check-circle-fill me-2"></i>{successMessage}
                    <button type="button" className="btn-close" onClick={() => setSuccessMessage('')}></button>
                </div>
            )}

            <div className="row g-4">
                {/* Schedule Generator */}
                <div className="col-lg-4">
                    <div className="card shadow-sm border-0">
                        <div className="card-header bg-primary text-white py-3">
                            <h5 className="card-title fw-bold mb-0">
                                <i className="bi bi-calendar-plus me-2"></i>Publish Availability
                            </h5>
                        </div>
                        <div className="card-body p-4">
                            <form onSubmit={handleGenerateSlots} id="form-generate-slots">
                                <div className="mb-3">
                                    <label htmlFor="gen-date" className="form-label fw-bold">Date</label>
                                    <input
                                        type="date"
                                        className="form-control"
                                        id="gen-date"
                                        value={genDate}
                                        onChange={(e) => setGenDate(e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="gen-start" className="form-label fw-bold">Day Start Time</label>
                                    <input
                                        type="time"
                                        className="form-control"
                                        id="gen-start"
                                        value={genStart}
                                        onChange={(e) => setGenStart(e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="mb-4">
                                    <label htmlFor="gen-end" className="form-label fw-bold">Day End Time</label>
                                    <input
                                        type="time"
                                        className="form-control"
                                        id="gen-end"
                                        value={genEnd}
                                        onChange={(e) => setGenEnd(e.target.value)}
                                        required
                                    />
                                </div>
                                <button type="submit" className="btn btn-primary w-100 fw-semibold" id="btn-generate-slots" disabled={generating}>
                                    {generating ? <span className="spinner-border spinner-border-sm me-2"></span> : <i className="bi bi-gear-fill me-2"></i>}
                                    Generate 30-min Slots
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                {/* Incoming Requests */}
                <div className="col-lg-8">
                    <div className="card shadow-sm border-0">
                        <div className="card-header bg-white py-3">
                            <h5 className="card-title fw-bold mb-0">
                                <i className="bi bi-inbox-fill text-primary me-2"></i>Incoming Booking Requests
                            </h5>
                        </div>
                        <div className="card-body p-0">
                            {loading ? (
                                <div className="text-center py-5">
                                    <div className="spinner-border text-primary" role="status"></div>
                                    <p className="mt-2 text-muted">Loading appointments...</p>
                                </div>
                            ) : bookings.length === 0 ? (
                                <div className="text-center py-5 text-muted">
                                    <i className="bi bi-check2-circle display-4 mb-2 text-success"></i>
                                    <p>No incoming appointment requests pending review.</p>
                                </div>
                            ) : (
                                <div className="table-responsive">
                                    <table className="table table-hover align-middle mb-0">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Reference</th>
                                                <th>Client</th>
                                                <th>Date &amp; Time</th>
                                                <th>Document</th>
                                                <th>Status</th>
                                                <th className="text-end">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {bookings.map((b) => (
                                                <tr key={b.id}>
                                                    <td><strong>{b.referenceCode}</strong></td>
                                                    <td>
                                                        <div>{b.client?.fullName}</div>
                                                        <div className="small text-muted">{b.client?.phone}</div>
                                                    </td>
                                                    <td>
                                                        <div>{b.slot?.slotDate}</div>
                                                        <div className="small text-muted">{b.slot?.startTime} &ndash; {b.slot?.endTime}</div>
                                                    </td>
                                                    <td>
                                                        <span className="badge bg-light text-dark border">{b.documentType}</span>
                                                        {b.notes && <div className="small text-muted mt-1">{b.notes}</div>}
                                                    </td>
                                                    <td>
                                                        {b.status === 'CONFIRMED' && <span className="badge bg-success">CONFIRMED</span>}
                                                        {b.status === 'REQUESTED' && <span className="badge bg-warning text-dark">REQUESTED</span>}
                                                        {b.status === 'CANCELLED' && <span className="badge bg-danger">CANCELLED</span>}
                                                    </td>
                                                    <td className="text-end">
                                                        {b.status === 'REQUESTED' && (
                                                            <div className="btn-group btn-group-sm">
                                                                <button
                                                                    className="btn btn-success"
                                                                    id={`btn-confirm-${b.id}`}
                                                                    onClick={() => handleConfirm(b.id)}
                                                                >
                                                                    Confirm
                                                                </button>
                                                                <button
                                                                    className="btn btn-outline-danger"
                                                                    id={`btn-reject-${b.id}`}
                                                                    onClick={() => handleReject(b.id)}
                                                                >
                                                                    Reject
                                                                </button>
                                                            </div>
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
                </div>
            </div>
        </div>
    );
}
