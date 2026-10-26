import React, { useState } from 'react';
import { api } from '../services/api';

export default function BookingRequest({ slot, currentUser, onNavigate, onBookingSuccess }) {
    const [documentType, setDocumentType] = useState('AFFIDAVIT');
    const [notes, setNotes] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [confirmation, setConfirmation] = useState(null);

    if (!slot) {
        return (
            <div className="container py-5 text-center">
                <h4>No slot selected</h4>
                <button className="btn btn-primary mt-3" onClick={() => onNavigate('slots')}>
                    Go to Slot Finder
                </button>
            </div>
        );
    }

    if (!currentUser) {
        return (
            <div className="container py-5 text-center">
                <div className="card max-w-md mx-auto p-4 shadow-sm" style={{ maxWidth: '500px', margin: 'auto' }}>
                    <i className="bi bi-person-lock text-warning display-4 mb-3"></i>
                    <h4>Sign In Required</h4>
                    <p className="text-muted">You must be logged in as a Client to reserve an appointment slot.</p>
                    <div className="d-flex justify-content-center gap-2 mt-3">
                        <button className="btn btn-primary" onClick={() => onNavigate('login')}>
                            Sign In
                        </button>
                        <button className="btn btn-outline-secondary" onClick={() => onNavigate('register')}>
                            Register
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const data = await api.createBooking({
                slotId: slot.id,
                documentType,
                notes
            });
            setConfirmation(data);
        } catch (err) {
            setError(err.message || 'Error creating booking');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="container py-5">
            <div className="row justify-content-center">
                <div className="col-lg-6 col-md-8">
                    <div className="card shadow-sm border-0">
                        <div className="card-body p-4 p-md-5">
                            {confirmation ? (
                                <div className="text-center py-4">
                                    <i className="bi bi-check-circle-fill text-success display-3 mb-3"></i>
                                    <h3 className="fw-bold">Booking Request Submitted!</h3>
                                    <p className="text-muted">Your appointment request has been recorded and is pending notary confirmation.</p>

                                    <div className="bg-light border rounded p-3 my-4">
                                        <div className="small text-muted text-uppercase fw-bold">Booking Reference Code</div>
                                        <div className="fs-3 fw-bold text-primary" id="booking-reference-code">
                                            {confirmation.referenceCode}
                                        </div>
                                        <div className="small text-secondary mt-1">
                                            Save this reference code to track your appointment status.
                                        </div>
                                    </div>

                                    <div className="d-flex justify-content-center gap-3">
                                        <button className="btn btn-primary" onClick={() => onNavigate('clientDashboard')}>
                                            View My Bookings
                                        </button>
                                        <button className="btn btn-outline-secondary" onClick={() => onNavigate('track')}>
                                            Track Status
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <>
                                    <div className="mb-4">
                                        <h3 className="fw-bold">Confirm Appointment Booking</h3>
                                        <p className="text-muted">Review slot details and submit your notarial service request</p>
                                    </div>

                                    {error && (
                                        <div className="alert alert-danger" role="alert" id="booking-error-banner">
                                            <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
                                        </div>
                                    )}

                                    {/* Slot Summary Card */}
                                    <div className="bg-light border rounded p-3 mb-4">
                                        <div className="row">
                                            <div className="col-6 mb-2">
                                                <span className="small text-muted d-block">Date</span>
                                                <strong>{slot.slotDate}</strong>
                                            </div>
                                            <div className="col-6 mb-2">
                                                <span className="small text-muted d-block">Time Window</span>
                                                <strong>{slot.startTime} &ndash; {slot.endTime}</strong>
                                            </div>
                                            <div className="col-12">
                                                <span className="small text-muted d-block">Notary</span>
                                                <strong>{slot.notary?.user?.fullName || 'Certified Notary'}</strong>
                                                <span className="text-muted small ms-2">({slot.notary?.licenseNumber})</span>
                                            </div>
                                        </div>
                                    </div>

                                    <form onSubmit={handleSubmit} id="form-booking">
                                        <div className="mb-3">
                                            <label htmlFor="select-doc-type" className="form-label fw-bold">Document Type</label>
                                            <select
                                                className="form-select"
                                                id="select-doc-type"
                                                value={documentType}
                                                onChange={(e) => setDocumentType(e.target.value)}
                                            >
                                                <option value="AFFIDAVIT">Affidavit / Sworn Statement</option>
                                                <option value="POWER_OF_ATTORNEY">Power of Attorney (POA)</option>
                                                <option value="PROPERTY_DEED">Property Deed / Title Conveyance</option>
                                                <option value="STATUTORY_DECLARATION">Statutory Declaration</option>
                                                <option value="OTHER">Other Legal Document</option>
                                            </select>
                                        </div>

                                        <div className="mb-4">
                                            <label htmlFor="input-notes" className="form-label fw-bold">Additional Notes / Instructions</label>
                                            <textarea
                                                className="form-control"
                                                id="input-notes"
                                                rows="3"
                                                placeholder="e.g. 2 original copies requiring witness signature"
                                                value={notes}
                                                onChange={(e) => setNotes(e.target.value)}
                                            ></textarea>
                                        </div>

                                        <button type="submit" className="btn btn-primary w-100 py-2 fw-semibold" id="btn-submit-booking" disabled={loading}>
                                            {loading ? (
                                                <span><span className="spinner-border spinner-border-sm me-2"></span>Confirming...</span>
                                            ) : (
                                                <span><i className="bi bi-check2-circle me-2"></i>Submit Booking Request</span>
                                            )}
                                        </button>
                                    </form>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
