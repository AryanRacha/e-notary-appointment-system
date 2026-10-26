import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function SlotView({ onSelectSlot, onNavigate, currentUser }) {
    const [notaries, setNotaries] = useState([]);
    const [selectedNotary, setSelectedNotary] = useState('');
    const [selectedDate, setSelectedDate] = useState(() => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        return tomorrow.toISOString().split('T')[0];
    });
    const [slots, setSlots] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        loadNotaries();
    }, []);

    useEffect(() => {
        if (selectedNotary && selectedDate) {
            loadSlots();
        }
    }, [selectedNotary, selectedDate]);

    async function loadNotaries() {
        try {
            const data = await api.getNotaries();
            setNotaries(data);
            if (data.length > 0) {
                setSelectedNotary(data[0].id);
            }
        } catch (err) {
            setError('Could not load notaries: ' + err.message);
        }
    }

    async function loadSlots() {
        setLoading(true);
        setError('');
        try {
            const data = await api.getSlots(selectedNotary, selectedDate);
            setSlots(data);
        } catch (err) {
            setError(err.message || 'Error loading slots');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="container py-5">
            <div className="text-center mb-4">
                <h2 className="fw-bold">Browse Available Notary Slots</h2>
                <p className="text-muted">Select a certified notary and date to view discrete 30-minute availability windows</p>
            </div>

            {error && (
                <div className="alert alert-danger" role="alert">
                    <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            )}

            {/* Filter Bar */}
            <div className="card shadow-sm border-0 mb-4">
                <div className="card-body p-4">
                    <div className="row g-3">
                        <div className="col-md-6">
                            <label htmlFor="select-notary" className="form-label fw-bold">Select Notary Public</label>
                            <select
                                className="form-select"
                                id="select-notary"
                                value={selectedNotary}
                                onChange={(e) => setSelectedNotary(e.target.value)}
                            >
                                {notaries.map((n) => (
                                    <option key={n.id} value={n.id}>
                                        {n.user.fullName} &mdash; {n.specialization || 'General Notary'} ({n.licenseNumber})
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="col-md-6">
                            <label htmlFor="input-date" className="form-label fw-bold">Appointment Date</label>
                            <input
                                type="date"
                                className="form-control"
                                id="input-date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* Slots Grid */}
            <div className="card shadow-sm border-0">
                <div className="card-body p-4">
                    <h5 className="fw-bold mb-3">Available Slots for {selectedDate}</h5>
                    {loading ? (
                        <div className="text-center py-5">
                            <div className="spinner-border text-primary" role="status"></div>
                            <p className="mt-2 text-muted">Checking notary schedule...</p>
                        </div>
                    ) : slots.length === 0 ? (
                        <div className="text-center py-5 text-muted">
                            <i className="bi bi-calendar-x display-4 mb-2"></i>
                            <p>No available slots found for this notary on this date.</p>
                            {currentUser && currentUser.role === 'NOTARY' && (
                                <button className="btn btn-outline-primary btn-sm" onClick={() => onNavigate('notaryDashboard')}>
                                    Generate Schedule in Notary Portal
                                </button>
                            )}
                        </div>
                    ) : (
                        <div className="row g-3">
                            {slots.map((slot) => (
                                <div className="col-md-4 col-sm-6" key={slot.id}>
                                    <div className={`card h-100 border ${slot.isBooked ? 'bg-light text-muted' : 'border-primary-subtle'}`}>
                                        <div className="card-body text-center p-3">
                                            <div className="fw-bold fs-5 text-dark">
                                                {slot.startTime} &ndash; {slot.endTime}
                                            </div>
                                            <div className="small mb-3">
                                                {slot.isBooked ? (
                                                    <span className="badge bg-secondary">Booked</span>
                                                ) : (
                                                    <span className="badge bg-success">Available</span>
                                                )}
                                            </div>
                                            {!slot.isBooked && (
                                                <button
                                                    className="btn btn-primary btn-sm w-100 fw-semibold"
                                                    id={`btn-book-slot-${slot.id}`}
                                                    onClick={() => onSelectSlot(slot)}
                                                >
                                                    <i className="bi bi-bookmark-plus me-1"></i>Book Slot
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
